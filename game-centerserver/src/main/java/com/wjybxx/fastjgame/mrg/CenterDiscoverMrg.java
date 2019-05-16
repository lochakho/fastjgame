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
import com.wjybxx.fastjgame.core.ZKOnlineSceneNode;
import com.wjybxx.fastjgame.core.ZKOnlineWarzoneNode;
import com.wjybxx.fastjgame.core.parserresult.CrossSceneNodeName;
import com.wjybxx.fastjgame.core.parserresult.SingleSceneNodeName;
import com.wjybxx.fastjgame.core.parserresult.WarzoneNodeName;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.GameUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

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

    private final CuratorMrg curatorMrg;
    private final ZkPathMrg zkPathMrg;
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
    public CenterDiscoverMrg(CuratorMrg curatorMrg, ZkPathMrg zkPathMrg, CenterWorldInfoMrg centerWorldInfoMrg,
                             WarzoneInCenterInfoMrg warzoneInCenterInfoMrg, SceneInCenterInfoMrg sceneInCenterInfoMrg) {
        this.curatorMrg = curatorMrg;
        this.zkPathMrg = zkPathMrg;
        this.centerWorldInfoMrg = centerWorldInfoMrg;
        this.warzoneInCenterInfoMrg = warzoneInCenterInfoMrg;
        this.sceneInCenterInfoMrg = sceneInCenterInfoMrg;
    }

    @Override
    protected void startImp() throws Exception {
        String watchPath = zkPathMrg.onlineParentPath(centerWorldInfoMrg.getWarzoneId());
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
        String nodeName= GameUtils.findNodeName(childData.getPath());
        RoleType roleType= GameUtils.parseServerType(nodeName);
        // 只处理战区和scene信息
        if (roleType != RoleType.SCENE_SERVER && roleType != RoleType.WARZONE_SERVER){
            return;
        }

        // 更新缓存(方便debug跟踪)
        if (type == Type.CHILD_ADDED){
            onlineNodeInfoMap.put(childData.getPath(),childData);
        } else {
            onlineNodeInfoMap.remove(childData.getPath());
        }

        if (roleType==RoleType.SCENE_SERVER){
            onSceneEvent(type, childData);
        }else {
            onWarzoneEvent(type, childData);
        }
    }

    private void onSceneEvent(Type type, ChildData childData) {
        SceneProcessType sceneProcessType = GameUtils.parseSceneType(childData.getPath());
        ZKOnlineSceneNode zkOnlineSceneNode=GameUtils.parseFromJson(childData.getData(),ZKOnlineSceneNode.class);
        if (sceneProcessType==SceneProcessType.SINGLE){
            // 单服场景
            SingleSceneNodeName singleSceneNodeName =GameUtils.parseSingleSceneNodeName(childData.getPath());
            if (singleSceneNodeName.getWarzoneId() != centerWorldInfoMrg.getWarzoneId()
                    || singleSceneNodeName.getServerId() != centerWorldInfoMrg.getServerId()){
                // 不是我的场景
                return;
            }
            if (type==Type.CHILD_ADDED) {
                sceneInCenterInfoMrg.onDiscoverSingleScene(singleSceneNodeName,zkOnlineSceneNode);
            } else {
                // remove
                sceneInCenterInfoMrg.onSingleSceneNodeRemoved(singleSceneNodeName);
            }
        }else {
            // 跨服场景
            CrossSceneNodeName crossSceneNodeName=GameUtils.parseCrossSceneNodeName(childData.getPath());
            if (type==Type.CHILD_ADDED){
                sceneInCenterInfoMrg.onDiscoverCrossScene(crossSceneNodeName,zkOnlineSceneNode);
            }else {
                // remove
                sceneInCenterInfoMrg.onCrossSceneNodeRemoved(crossSceneNodeName);
            }
        }
    }

    private void onWarzoneEvent(Type type, ChildData childData) {
        WarzoneNodeName warzoneNodeName= GameUtils.parseWarzoneNodeNode(childData.getPath());
        if (warzoneNodeName.getWarzoneId()!=centerWorldInfoMrg.getWarzoneId()){
            // 不是我的战区，这里不应该走到，因为该节点下的进程都是同一个进程的
            return;
        }
        if (type== Type.CHILD_ADDED){
            ZKOnlineWarzoneNode zkOnlineWarzoneNode=GameUtils.parseFromJson(childData.getData(),ZKOnlineWarzoneNode.class);
            warzoneInCenterInfoMrg.onDiscoverWarzone(warzoneNodeName,zkOnlineWarzoneNode);
        }else {
            // child remove
            warzoneInCenterInfoMrg.onWarzoneNodeRemoved(warzoneNodeName);
        }
    }
}
