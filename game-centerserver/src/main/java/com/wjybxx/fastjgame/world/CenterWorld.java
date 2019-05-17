
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

package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.core.node.ZKOnlineCenterNode;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import javax.annotation.Nonnull;

import java.util.concurrent.TimeUnit;

import static com.wjybxx.fastjgame.protobuffer.p_center_scene.*;
import static com.wjybxx.fastjgame.protobuffer.p_center_scene.p_center_single_scene_hello_result;

/**
 * CENTER_SERVER
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 22:43
 * @github - https://github.com/hl845740757
 */
public class CenterWorld extends WorldCore {

    private final CenterDiscoverMrg centerDiscoverMrg;
    private final SceneInCenterInfoMrg sceneInCenterInfoMrg;
    private final CenterWorldInfoMrg centerWorldInfoMrg;

    @Inject
    public CenterWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper,
                       CenterDiscoverMrg centerDiscoverMrg, SceneInCenterInfoMrg sceneInCenterInfoMrg) {
        super(worldWrapper, coreWrapper);
        this.centerDiscoverMrg = centerDiscoverMrg;
        this.sceneInCenterInfoMrg = sceneInCenterInfoMrg;
        centerWorldInfoMrg = (CenterWorldInfoMrg) worldWrapper.getWorldInfoMrg();
    }

    @Override
    protected void registerMessageHandlers() {
        registerServerMessageHandler(p_center_single_scene_hello_result.class, sceneInCenterInfoMrg::p_center_single_scene_hello_result_handler);
        registerServerMessageHandler(p_center_cross_scene_hello_result.class, sceneInCenterInfoMrg::p_center_cross_scene_hello_result_handler);

    }


    @Override
    protected void registerHttpRequestHandlers() {

    }

    @Override
    protected void registerSyncRequestHandlers() {

    }

    @Nonnull
    @Override
    protected SessionLifecycleAware<S2CSession> newAsyncSessionLifecycleAware() {
        return new SessionLifecycleAware<S2CSession>() {
            @Override
            public void onSessionConnected(S2CSession session) {

            }

            @Override
            public void onSessionDisconnected(S2CSession session) {

            }
        };
    }

    @Nonnull
    @Override
    protected SessionLifecycleAware<SyncS2CSession> newSyncSessionLifeCycleAware() {
        return new SessionLifecycleAware<SyncS2CSession>() {
            @Override
            public void onSessionConnected(SyncS2CSession syncS2CSession) {

            }

            @Override
            public void onSessionDisconnected(SyncS2CSession syncS2CSession) {

            }
        };
    }

    @Override
    protected void startHook() throws Exception {
        // 绑定端口并注册到zookeeper
        bindAndRegisterToZK();

        // 注册成功再启动服务发现
        centerDiscoverMrg.start();
    }

    private void bindAndRegisterToZK() throws Exception {
        // 绑定3个内部交互的端口
        HostAndPort tcpHostAndPort = innerAcceptorMrg.bindInnerTcpPort(true);
        HostAndPort syncRpcHostAndPort = innerAcceptorMrg.bindInnerSyncRpcPort(true);
        HostAndPort httpHostAndPort = innerAcceptorMrg.bindInnerHttpPort();

        // 注册到zk
        String parentPath= ZKUtils.onlineParentPath(centerWorldInfoMrg.getWarzoneId());
        String nodeName= ZKUtils.buildCenterNodeName(centerWorldInfoMrg.getWarzoneId(), centerWorldInfoMrg.getServerId());

        ZKOnlineCenterNode zkOnlineCenterNode=new ZKOnlineCenterNode(tcpHostAndPort.toString(),
                syncRpcHostAndPort.toString(),
                httpHostAndPort.toString(),
                centerWorldInfoMrg.getProcessGuid());


        final String path = ZKPaths.makePath(parentPath, nodeName);
        curatorMrg.waitForNodeDelete(path);

        final byte[] initData = GameUtils.serializeToJsonBytes(zkOnlineCenterNode);
        ConcurrentUtils.awaitRemoteWithSleepingRetry(path,resource -> {
            return curatorMrg.createNodeIfAbsent(path,CreateMode.EPHEMERAL,initData);
        },3, TimeUnit.SECONDS);
    }

    @Override
    protected void tickHook() {
        centerDiscoverMrg.tick();
    }

    @Override
    protected void shutdownHook() {
        centerDiscoverMrg.shutdown();
    }
}
