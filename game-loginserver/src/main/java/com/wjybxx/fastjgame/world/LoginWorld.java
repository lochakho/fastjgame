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
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 20:11
 * @github - https://github.com/hl845740757
 */
public class LoginWorld extends WorldCore{

    @Inject
    public LoginWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
        super(worldWrapper, coreWrapper);
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

    }

    @Override
    protected void tickHook() {

    }

    @Override
    protected void shutdownHook() {

    }
}
