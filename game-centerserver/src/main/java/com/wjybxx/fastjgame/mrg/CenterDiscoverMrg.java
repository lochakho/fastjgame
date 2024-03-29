/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.onlinenode.*;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;

/**
 * CenterServer端的节点发现逻辑，类似服务发现，但不一样。
 *
 * CenterServer需要探测所有的scene和warzone，并派发事件与之建立链接
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:06
 * @github - https://github.com/hl845740757
 */
public class CenterDiscoverMrg extends AbstractThreadLifeCycleHelper {

    private static final Logger logger= LoggerFactory.getLogger(CenterDiscoverMrg.class);

    private final CuratorMrg curatorMrg;
    private final CenterWorldInfoMrg centerWorldInfoMrg;
    private final WarzoneInCenterInfoMrg warzoneInCenterInfoMrg;
    private final SceneInCenterInfoMrg sceneInCenterInfoMrg;

    /**
     * zk事件队列，由逻辑线程和watcher线程共同使用
     */
    private final ConcurrentLinkedQueue<PathChildrenCacheEvent> eventQueue=new ConcurrentLinkedQueue<>();
    /**
     * 当前在先节点信息，只在逻辑线程使用
     */
    private Map<String, ChildData> onlineNodeInfoMap=new HashMap<>();

    @Inject
    public CenterDiscoverMrg(CuratorMrg curatorMrg,CenterWorldInfoMrg centerWorldInfoMrg,
                             WarzoneInCenterInfoMrg warzoneInCenterInfoMrg, SceneInCenterInfoMrg sceneInCenterInfoMrg) {
        this.curatorMrg = curatorMrg;
        this.centerWorldInfoMrg = centerWorldInfoMrg;
        this.warzoneInCenterInfoMrg = warzoneInCenterInfoMrg;
        this.sceneInCenterInfoMrg = sceneInCenterInfoMrg;
    }

    @Override
    protected void startImp() throws Exception {
        String watchPath = ZKPathUtils.onlineParentPath(centerWorldInfoMrg.getWarzoneId());
        List<ChildData> childrenData = curatorMrg.watchChildren(watchPath, (client, event) -> eventQueue.offer(event));

        // 初始监听
        childrenData.forEach(e->onEvent(Type.CHILD_ADDED,e));
    }

    @Override
    protected void shutdownImp() {

    }

    public void tick(){
        PathChildrenCacheEvent e;
        while ((e=eventQueue.poll())!=null){
            onEvent(e.getType(),e.getData());
        }
    }

    private void onEvent(Type type,ChildData childData){
        // 只处理节点增加和移除两件事情
        if (type != Type.CHILD_ADDED && type != Type.CHILD_REMOVED){
            return;
        }
        String nodeName= ZKPathUtils.findNodeName(childData.getPath());
        RoleType roleType= ZKPathUtils.parseServerType(nodeName);
        // 只处理战区和scene信息
        if (roleType != RoleType.SCENE && roleType != RoleType.WARZONE){
            return;
        }

        // 更新缓存(方便debug跟踪)
        if (type == Type.CHILD_ADDED){
            onlineNodeInfoMap.put(childData.getPath(),childData);
        } else {
            onlineNodeInfoMap.remove(childData.getPath());
        }

        if (roleType==RoleType.SCENE){
            onSceneEvent(type, childData);
        }else {
            onWarzoneEvent(type, childData);
        }
    }

    /**
     * 该节点下会有我的私有场景、其它服的场景和跨服场景
     * @param type 事件类型
     * @param childData 场景数据
     */
    private void onSceneEvent(Type type, ChildData childData) {
        SceneProcessType sceneProcessType = ZKPathUtils.parseSceneType(childData.getPath());
        SceneNodeData sceneNodeData =GameUtils.parseFromJsonBytes(childData.getData(), SceneNodeData.class);
        if (sceneProcessType==SceneProcessType.SINGLE){
            // 单服场景
            SingleSceneNodeName singleSceneNodeName = ZKPathUtils.parseSingleSceneNodeName(childData.getPath());
            if (singleSceneNodeName.getPlatformType() != centerWorldInfoMrg.getPlatformType()
                    || singleSceneNodeName.getServerId() != centerWorldInfoMrg.getServerId()){
                // 不是我的场景
                return;
            }
            if (type==Type.CHILD_ADDED) {
                sceneInCenterInfoMrg.onDiscoverSingleScene(singleSceneNodeName, sceneNodeData);
                logger.debug("discover single scene {}-{}-{}", singleSceneNodeName.getPlatformType(),singleSceneNodeName.getServerId(),sceneNodeData.getChannelId());
            } else {
                // remove
                sceneInCenterInfoMrg.onSingleSceneNodeRemoved(singleSceneNodeName);
                logger.debug("child remove single scene {}-{}-{}", singleSceneNodeName.getPlatformType(),singleSceneNodeName.getServerId(),sceneNodeData.getChannelId());
            }
        }else {
            // 跨服场景
            CrossSceneNodeName crossSceneNodeName = ZKPathUtils.parseCrossSceneNodeName(childData.getPath());
            if (type==Type.CHILD_ADDED){
                sceneInCenterInfoMrg.onDiscoverCrossScene(crossSceneNodeName, sceneNodeData);
                logger.debug("discover cross scene {}", sceneNodeData.getChannelId());
            }else {
                // remove
                sceneInCenterInfoMrg.onCrossSceneNodeRemoved(crossSceneNodeName);
                logger.debug("child remove,cross scene {}", sceneNodeData.getChannelId());
            }
        }
    }

    /**
     * 监测的路径下只会有一个战区节点，且节点名字是不会变的。
     *
     * 由于节点名字不会变，那么只能保证 有一次add必然有一次remove，但是remove对应的数据可能和add并不一致！
     * 见测试用例中的 WatcherTest说明
     *
     * @param type 事件类型
     * @param childData 战区数据
     */
    private void onWarzoneEvent(Type type, ChildData childData) {
        WarzoneNodeName warzoneNodeName = ZKPathUtils.parseWarzoneNodeName(childData.getPath());
        WarzoneNodeData warzoneNodeData =GameUtils.parseFromJsonBytes(childData.getData(), WarzoneNodeData.class);
        if (type== Type.CHILD_ADDED){
            warzoneInCenterInfoMrg.onDiscoverWarzone(warzoneNodeName, warzoneNodeData);
            logger.debug("discover warzone {}", warzoneNodeName.getWarzoneId());
        }else {
            // child remove
            warzoneInCenterInfoMrg.onWarzoneNodeRemoved(warzoneNodeName, warzoneNodeData);
            logger.debug("child remove,warzone {}", warzoneNodeName.getWarzoneId());
        }
    }
}
