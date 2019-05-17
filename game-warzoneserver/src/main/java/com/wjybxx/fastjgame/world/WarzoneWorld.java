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
import com.wjybxx.fastjgame.mrg.CenterInWarzoneInfoMrg;
import com.wjybxx.fastjgame.mrg.WarzoneWorldInfoMrg;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.TimeUnit;

import static com.wjybxx.fastjgame.protobuffer.p_center_warzone.*;

/**
 * WarzoneServer
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 18:31
 * @github - https://github.com/hl845740757
 */
public class WarzoneWorld extends WorldCore {

    private final WarzoneWorldInfoMrg warzoneWorldInfoMrg;
    private final CenterInWarzoneInfoMrg centerInWarzoneInfoMrg;

    @Inject
    public WarzoneWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper, WarzoneWorldInfoMrg warzoneWorldInfoMrg,
                        CenterInWarzoneInfoMrg centerInWarzoneInfoMrg) {
        super(worldWrapper, coreWrapper);
        this.warzoneWorldInfoMrg = warzoneWorldInfoMrg;
        this.centerInWarzoneInfoMrg = centerInWarzoneInfoMrg;
    }

    @Override
    protected void registerMessageHandlers() {
        registerRequestMessageHandler(p_center_warzone_hello.class,centerInWarzoneInfoMrg::p_center_warzone_hello_handler);
    }

    @Override
    protected void registerHttpRequestHandlers() {

    }

    @Override
    protected void registerSyncRequestHandlers() {

    }

    @Override
    protected void registerAsyncSessionLifeAware(S2CSessionMrg s2CSessionMrg) {
        this.s2CSessionMrg.registerLifeCycleAware(RoleType.CENTER_SERVER, new SessionLifecycleAware<S2CSession>() {
            @Override
            public void onSessionConnected(S2CSession session) {

            }

            @Override
            public void onSessionDisconnected(S2CSession session) {
                centerInWarzoneInfoMrg.onCenterServerDisconnect(session);
            }
        });
    }

    @Override
    protected void registerSyncSessionLifeAware(SyncS2CSessionMrg syncS2CSessionMrg) {

    }

    @Override
    protected void startHook() throws Exception {
        bindAndRegisterToZK();
    }

    private void bindAndRegisterToZK() throws Exception {
        // 绑定3个内部交互的端口
        HostAndPort tcpHostAndPort = innerAcceptorMrg.bindInnerTcpPort(true);
        HostAndPort syncRpcHostAndPort = innerAcceptorMrg.bindInnerSyncRpcPort(true);
        HostAndPort httpHostAndPort = innerAcceptorMrg.bindInnerHttpPort();

        // 注册到zk
        String parentPath= ZKUtils.onlineParentPath(warzoneWorldInfoMrg.getWarzoneId());
        String nodeName= ZKUtils.buildWarzoneNodeName(warzoneWorldInfoMrg.getWarzoneId());

        ZKOnlineCenterNode zkOnlineCenterNode=new ZKOnlineCenterNode(tcpHostAndPort.toString(),
                syncRpcHostAndPort.toString(),
                httpHostAndPort.toString(),
                warzoneWorldInfoMrg.getProcessGuid());


        final String path = ZKPaths.makePath(parentPath, nodeName);
        curatorMrg.waitForNodeDelete(path);

        final byte[] initData = GameUtils.serializeToJsonBytes(zkOnlineCenterNode);
        ConcurrentUtils.awaitRemoteWithSleepingRetry(path, resource -> {
            return curatorMrg.createNodeIfAbsent(path, CreateMode.EPHEMERAL,initData);
        },3, TimeUnit.SECONDS);
    }

    @Override
    protected void tickHook() {

    }

    @Override
    protected void shutdownHook() {

    }
}
