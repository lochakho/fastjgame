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
import com.wjybxx.fastjgame.core.SceneInCenterInfo;
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.core.node.ZKOnlineSceneNode;
import com.wjybxx.fastjgame.core.nodename.CrossSceneNodeName;
import com.wjybxx.fastjgame.core.nodename.SingleSceneNodeName;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.net.async.C2SSession;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncC2SSession;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.EnumSet;
import java.util.Set;

import static com.wjybxx.fastjgame.protobuffer.p_center_scene.*;

/**
 * SceneServer在CenterServer中的连接等控制器。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:11
 * @github - https://github.com/hl845740757
 */
public class SceneInCenterInfoMrg {
    /**
     * game主动连接scene，因此scene是服务器端，center是会话的客户端。
     */
    private final C2SSessionMrg c2SSessionMrg;
    private final SyncC2SSessionMrg syncC2SSessionMrg;
    private final CenterWorldInfoMrg centerWorldInfoMrg;
    private final InnerAcceptorMrg innerAcceptorMrg;
    /**
     * scene信息集合
     * sceneGuid->sceneInfo
     */
    private final Long2ObjectMap<SceneInCenterInfo> guid2InfoMap=new Long2ObjectOpenHashMap<>();
    /**
     * channelId->sceneInfo
     */
    private final Int2ObjectMap<SceneInCenterInfo> channelId2InfoMap=new Int2ObjectOpenHashMap<>();

    @Inject
    public SceneInCenterInfoMrg(C2SSessionMrg c2SSessionMrg, SyncC2SSessionMrg syncC2SSessionMrg,
                                CenterWorldInfoMrg centerWorldInfoMrg, InnerAcceptorMrg innerAcceptorMrg) {

        this.c2SSessionMrg = c2SSessionMrg;
        this.syncC2SSessionMrg = syncC2SSessionMrg;
        this.centerWorldInfoMrg = centerWorldInfoMrg;
        this.innerAcceptorMrg = innerAcceptorMrg;
    }

    private void addSceneInfo(SceneInCenterInfo sceneInCenterInfo) {
        guid2InfoMap.put(sceneInCenterInfo.getSceneProcessGuid(), sceneInCenterInfo);
        channelId2InfoMap.put(sceneInCenterInfo.getChanelId(), sceneInCenterInfo);
    }

    private void removeSceneInfo(SceneInCenterInfo sceneInCenterInfo) {
        guid2InfoMap.remove(sceneInCenterInfo.getSceneProcessGuid());
        channelId2InfoMap.remove(sceneInCenterInfo.getChanelId());
    }

    /**
     * 当在zk上发现单服scene节点
     * @param singleSceneNodeName 本服scene节点名字信息
     * @param onlineSceneNode 本服scene节点其它信息
     */
    public void onDiscoverSingleScene(SingleSceneNodeName singleSceneNodeName, ZKOnlineSceneNode onlineSceneNode){
        // 注册异步tcp会话
        HostAndPort tcpHostAndPort=HostAndPort.parseHostAndPort(onlineSceneNode.getInnerTcpAddress());
        innerAcceptorMrg.registerAsyncTcpSession(singleSceneNodeName.getSceneProcessGuid(),RoleType.SCENE_SERVER,
                tcpHostAndPort,new SingleSceneAware());

        // 注册同步rpc会话
        HostAndPort syncRpcHostAndPort=HostAndPort.parseHostAndPort(onlineSceneNode.getInnerRpcAddress());
        innerAcceptorMrg.registerSyncRpcSession(singleSceneNodeName.getSceneProcessGuid(), RoleType.SCENE_SERVER,
                syncRpcHostAndPort,new SingleSceneSyncAware());
    }

    /**
     * 当本服scene节点移除
     * @param singleSceneNodeName 单服节点名字
     */
    public void onSingleSceneNodeRemoved(SingleSceneNodeName singleSceneNodeName){
        onSceneDisconnect(singleSceneNodeName.getSceneProcessGuid());
    }

    /**
     * 当在zk上发现跨服scene节点
     * @param crossSceneNodeName 跨服场景名字信息
     * @param onlineSceneNode  跨服场景其它信息
     */
    public void onDiscoverCrossScene(CrossSceneNodeName crossSceneNodeName, ZKOnlineSceneNode onlineSceneNode){
        // 注册异步会话
        HostAndPort tcpHostAndPort=HostAndPort.parseHostAndPort(onlineSceneNode.getInnerTcpAddress());
        innerAcceptorMrg.registerAsyncTcpSession(crossSceneNodeName.getSceneProcessGuid(), RoleType.SCENE_SERVER,
                tcpHostAndPort,
                new CrossSceneAware());

        // 注册同步rpc会话
        HostAndPort syncRpcHostAndPort=HostAndPort.parseHostAndPort(onlineSceneNode.getInnerRpcAddress());
        innerAcceptorMrg.registerSyncRpcSession(crossSceneNodeName.getSceneProcessGuid(), RoleType.SCENE_SERVER,
                syncRpcHostAndPort,
                new CrossSceneSyncAware());
    }

    /**
     * 当跨服scene节点移除
     * @param crossSceneNodeName 跨服节点名字
     */
    public void onCrossSceneNodeRemoved(CrossSceneNodeName crossSceneNodeName){
        onSceneDisconnect(crossSceneNodeName.getSceneProcessGuid());
    }

    /**
     * 当与scene断开连接(异步tcp会话断掉，或zk节点消失)
     */
    private void onSceneDisconnect(long sceneProcessGuid){
        // 移除会话
        c2SSessionMrg.removeSession(sceneProcessGuid,"node removed");
        syncC2SSessionMrg.removeSession(sceneProcessGuid,"node removed");
        // 移除存储的信息
        SceneInCenterInfo sceneInCenterInfo = guid2InfoMap.remove(sceneProcessGuid);
        if (null== sceneInCenterInfo){
            return;
        }
        offlinePlayer(sceneInCenterInfo);

        // 跨服场景就此打住
        if (sceneInCenterInfo.getProcessType()== SceneProcessType.CROSS){
            return;
        }
        // 本服场景
        // TODO 如果该场景启动的区域消失，需要让别的场景进程启动这些区域
    }

    private void offlinePlayer(SceneInCenterInfo sceneInCenterInfo){
        // TODO 需要帮本服在这些进程的玩家下线处理
    }

    /**
     * 本服scene会话信息
     */
    private class SingleSceneAware implements SessionLifecycleAware<C2SSession>{

        @Override
        public void onSessionConnected(C2SSession session) {
            SceneInCenterInfo sceneInCenterInfo = guid2InfoMap.get(session.getServerGuid());
            // 消息返回时，会话可能不在(触发的时机不确定，异步消息需要多注意点)
            if (null== sceneInCenterInfo){
                return;
            }
            p_center_single_scene_hello hello = p_center_single_scene_hello
                    .newBuilder()
                    .setServerId(centerWorldInfoMrg.getServerId())
                    .build();
            c2SSessionMrg.send(session.getServerGuid(), hello);
        }

        @Override
        public void onSessionDisconnected(C2SSession session) {
            onSceneDisconnect(session.getServerGuid());
        }
    }

    private class SingleSceneSyncAware implements SessionLifecycleAware<SyncC2SSession> {

        @Override
        public void onSessionConnected(SyncC2SSession session) {

        }

        @Override
        public void onSessionDisconnected(SyncC2SSession session) {
            onSceneDisconnect(session.getServerGuid());
        }
    }


    /**
     * 跨服会话信息
     */
    private class CrossSceneAware implements SessionLifecycleAware<C2SSession>{

        @Override
        public void onSessionConnected(C2SSession session) {
            p_center_cross_scene_hello hello = p_center_cross_scene_hello
                    .newBuilder()
                    .setServerId(centerWorldInfoMrg.getServerId())
                    .build();
            c2SSessionMrg.send(session.getServerGuid(), hello);
        }

        @Override
        public void onSessionDisconnected(C2SSession session) {
            onSceneDisconnect(session.getServerGuid());
        }
    }

    private class CrossSceneSyncAware implements SessionLifecycleAware<SyncC2SSession>{

        @Override
        public void onSessionConnected(SyncC2SSession session) {

        }

        @Override
        public void onSessionDisconnected(SyncC2SSession session) {
            onSceneDisconnect(session.getServerGuid());
        }
    }

    /**
     * 收到单服场景的响应信息
     * @param session scene会话信息
     * @param result
     */
    public void p_center_single_scene_hello_result_handler(C2SSession session, p_center_single_scene_hello_result result) {
        Set<SceneRegion> configuredRegions = EnumSet.noneOf(SceneRegion.class);
        Set<SceneRegion> activeRegions = EnumSet.noneOf(SceneRegion.class);
        for (int regionId:result.getConfiguredRegionsList()){
            SceneRegion sceneRegion=SceneRegion.forNumber(regionId);
            configuredRegions.add(sceneRegion);
            // 非互斥的区域已经启动了
            if (!sceneRegion.isMutex()){
                activeRegions.add(sceneRegion);
            }
        }

        // 创建信息
        SceneInCenterInfo sceneInCenterInfo = new SceneInCenterInfo(session.getServerGuid(),
                result.getChannelId(),
                SceneProcessType.SINGLE,
                configuredRegions,
                activeRegions);

        // 需要保存到两个缓存中
        addSceneInfo(sceneInCenterInfo);

        // TODO 检查该场景可以启动哪些互斥场景
    }

    /**
     * 收到跨服场景的连接响应
     * @param session 与跨服场景的会话
     * @param result 响应结果
     */
    public void p_center_cross_scene_hello_result_handler(C2SSession session, p_center_cross_scene_hello_result result) {
        // 配置的区域
        Set<SceneRegion> configuredRegions = EnumSet.noneOf(SceneRegion.class);
        for (int regionId:result.getConfiguredRegionsList()){
            configuredRegions.add(SceneRegion.forNumber(regionId));
        }
        // 成功启动的区域
        Set<SceneRegion> activeRegions = EnumSet.noneOf(SceneRegion.class);
        for (int regionId:result.getActiveRegionsList()){
            activeRegions.add(SceneRegion.forNumber(regionId));
        }
        // 创建信息
        SceneInCenterInfo sceneInCenterInfo = new SceneInCenterInfo(session.getServerGuid(),
                result.getChannelId(),
                SceneProcessType.CROSS,
                configuredRegions,
                activeRegions);

        // 需要保存到两个缓存中
        addSceneInfo(sceneInCenterInfo);

    }

}
