
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
import com.wjybxx.fastjgame.misc.ProtoBufHashMappingStrategy;
import com.wjybxx.fastjgame.mrg.CenterDiscoverMrg;
import com.wjybxx.fastjgame.mrg.SceneInCenterInfoMrg;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.ProtoBufMessageSerializer;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.protobuffer.p_center_scene;
import com.wjybxx.fastjgame.utils.GameUtils;

import javax.annotation.Nonnull;

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

    @Inject
    public CenterWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper,
                       CenterDiscoverMrg centerDiscoverMrg, SceneInCenterInfoMrg sceneInCenterInfoMrg) {
        super(worldWrapper, coreWrapper);
        this.centerDiscoverMrg = centerDiscoverMrg;
        this.sceneInCenterInfoMrg = sceneInCenterInfoMrg;
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        registerCodecHelper(GameUtils.INNER_CODEC_NAME,
                new ProtoBufHashMappingStrategy(),
                new ProtoBufMessageSerializer());
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
        return null;
    }

    @Nonnull
    @Override
    protected SessionLifecycleAware<SyncS2CSession> newSyncSessionLifeCycleAware() {
        return null;
    }

    @Override
    protected void startHook() throws Exception {
        centerDiscoverMrg.start();
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
