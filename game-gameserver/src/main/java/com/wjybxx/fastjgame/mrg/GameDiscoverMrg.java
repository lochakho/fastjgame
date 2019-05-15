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
import com.wjybxx.fastjgame.core.ZKOnlineSceneNode;
import com.wjybxx.fastjgame.core.ZKOnlineWarzoneNode;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.GameUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.*;

/**
 * GameServer端的节点发现逻辑，类似服务发现，但不一样。
 *
 * GameServer需要探测所有的scene和warzone，并派发事件与之建立链接
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:06
 * @github - https://github.com/hl845740757
 */
public class GameDiscoverMrg extends AbstractThreadLifeCycleHelper {

    private final CuratorMrg curatorMrg;
    private final ZkPathMrg zkPathMrg;
    private final GameWorldInfoMrg gameWorldInfoMrg;
    private final WarzoneInGameInfoMrg warzoneInGameInfoMrg;
    private final SceneInGameInfoMrg sceneInGameInfoMrg;

    /**
     * zk事件队列，由逻辑线程和watcher线程共同使用
     */
    private final ConcurrentLinkedQueue<PathChildrenCacheEvent> eventQueue=new ConcurrentLinkedQueue<>();
    /**
     * 当前在先节点信息，只在逻辑线程使用
     */
    private Map<String, ZKOnlineSceneNode> onlineNodeInfoMap=new HashMap<>();

    @Inject
    public GameDiscoverMrg(CuratorMrg curatorMrg, ZkPathMrg zkPathMrg, GameWorldInfoMrg gameWorldInfoMrg,
                           WarzoneInGameInfoMrg warzoneInGameInfoMrg, SceneInGameInfoMrg sceneInGameInfoMrg) {
        this.curatorMrg = curatorMrg;
        this.zkPathMrg = zkPathMrg;
        this.gameWorldInfoMrg = gameWorldInfoMrg;
        this.warzoneInGameInfoMrg = warzoneInGameInfoMrg;
        this.sceneInGameInfoMrg = sceneInGameInfoMrg;
    }

    @Override
    protected void startImp() throws Exception {
        String watchPath = zkPathMrg.onlineParentPath(gameWorldInfoMrg.getWarzoneId());
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

        String nodeName=zkPathMrg.findNodeName(childData.getPath());
        RoleType roleType= GameUtils.parseServerType(nodeName);

        switch (roleType){
            case SCENE_SERVER:
                if (type==Type.CHILD_ADDED){
                    ZKOnlineSceneNode zkOnlineSceneNode=GameUtils.parseFromJson(childData.getData(),ZKOnlineSceneNode.class);
                    sceneInGameInfoMrg.onDiscoverScene(zkOnlineSceneNode);
                }else {

                }
                break;
            case WARZONE_SERVER:
                if (type==Type.CHILD_ADDED){
                    ZKOnlineWarzoneNode zkOnlineWarzoneNode=GameUtils.parseFromJson(childData.getData(),ZKOnlineWarzoneNode.class);
                    warzoneInGameInfoMrg.onDiscoverWarzone(zkOnlineWarzoneNode);
                }else {
                    int warzoneId=GameUtils.parseWarzoneIdFromWarzoneNode(childData.getPath());
                }
                break;
        }
    }
}
