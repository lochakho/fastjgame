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
import com.wjybxx.fastjgame.core.onlinenode.LoginNodeData;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.initializer.HttpServerInitializer;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 20:11
 * @github - https://github.com/hl845740757
 */
public class LoginWorld extends WorldCore{

    private final LoginDiscoverMrg loginDiscoverMrg;
    private final LoginWorldInfoMrg loginWorldInfoMrg;
    private final CenterInLoginInfoMrg centerInLoginInfoMrg;

    @Inject
    public LoginWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper, LoginDiscoverMrg loginDiscoverMrg,
                      CenterInLoginInfoMrg centerInLoginInfoMrg) {
        super(worldWrapper, coreWrapper);
        this.loginDiscoverMrg = loginDiscoverMrg;
        this.loginWorldInfoMrg = (LoginWorldInfoMrg) worldWrapper.getWorldInfoMrg();
        this.centerInLoginInfoMrg = centerInLoginInfoMrg;
    }

    @Override
    protected void registerMessageHandlers() {

    }

    @Override
    protected void registerHttpRequestHandlers() {

    }

    @Override
    protected void registerSyncRequestHandlers() {

    }

    @Override
    protected void registerAsyncSessionLifeAware(S2CSessionMrg s2CSessionMrg) {

    }

    @Override
    protected void registerSyncSessionLifeAware(SyncS2CSessionMrg syncS2CSessionMrg) {

    }

    @Override
    protected void startHook() throws Exception {
        bindAndregisterToZK();

        loginDiscoverMrg.start();
    }

    private void bindAndregisterToZK() throws Exception {
        HostAndPort innerHttpAddress = innerAcceptorMrg.bindInnerHttpPort();
        HostAndPort outerHttpAddress = httpClientMrg.bind(true, loginWorldInfoMrg.getPort(), new HttpServerInitializer(disruptorMrg));

        String parentPath= ZKPathUtils.onlineRootPath();
        String nodeName = ZKPathUtils.buildLoginNodeName(loginWorldInfoMrg.getPort(),loginWorldInfoMrg.getProcessGuid());

        LoginNodeData loginNodeData =new LoginNodeData(innerHttpAddress.toString(),
                outerHttpAddress.toString());

        final String path = ZKPaths.makePath(parentPath, nodeName);
        final byte[] initData = GameUtils.serializeToJsonBytes(loginNodeData);
        curatorMrg.createNode(path, CreateMode.EPHEMERAL,initData);
    }

    @Override
    protected void tickHook() {
        loginDiscoverMrg.tick();
    }

    @Override
    protected void shutdownHook() {
        loginDiscoverMrg.shutdown();
    }
}
