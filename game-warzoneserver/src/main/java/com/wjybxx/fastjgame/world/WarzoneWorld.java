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
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;

import javax.annotation.Nonnull;

/**
 * WarzoneServer
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 18:31
 * @github - https://github.com/hl845740757
 */
public class WarzoneWorld extends WorldCore {

    @Inject
    public WarzoneWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
        super(worldWrapper, coreWrapper);
    }

    @Override
    protected void registerCodecHelper() throws Exception {

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

    @Nonnull
    @Override
    protected SessionLifecycleAware<S2CSession> newAsyncSessionLifecycleAware() {
        return null;
    }

    @Nonnull
    @Override
    protected SessionLifecycleAware<SyncS2CSession> newSyncSessionLifeCycleAware() {
        return null;
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
