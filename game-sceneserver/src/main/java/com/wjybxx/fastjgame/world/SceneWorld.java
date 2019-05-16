package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.misc.ProtoBufHashMappingStrategy;
import com.wjybxx.fastjgame.mrg.CenterInSceneInfoMrg;
import com.wjybxx.fastjgame.mrg.SceneRegionMrg;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.ProtoBufMessageSerializer;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.utils.GameUtils;

import javax.annotation.Nonnull;

import static com.wjybxx.fastjgame.protobuffer.p_sync_center_scene.*;

/**
 * SceneServer
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 21:45
 * @github - https://github.com/hl845740757
 */
public class SceneWorld extends WorldCore {

    private final CenterInSceneInfoMrg centerInSceneInfoMrg;
    private final SceneRegionMrg sceneRegionMrg;

    @Inject
    public SceneWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper, CenterInSceneInfoMrg centerInSceneInfoMrg, SceneRegionMrg sceneRegionMrg) {
        super(worldWrapper, coreWrapper);
        this.centerInSceneInfoMrg = centerInSceneInfoMrg;
        this.sceneRegionMrg = sceneRegionMrg;
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        registerCodecHelper(GameUtils.INNER_CODEC_NAME,
                new ProtoBufHashMappingStrategy(),
                new ProtoBufMessageSerializer());
    }

    @Override
    protected void registerMessageHandlers() {
    }

    @Override
    protected void registerHttpRequestHandlers() {

    }

    @Override
    protected void registerSyncRequestHandlers() {
        registerSyncRequestHandler(p_center_command_single_scene_start.class,sceneRegionMrg::p_center_command_single_scene_start_handler);
        registerSyncRequestHandler(p_center_command_single_scene_active_regions.class,sceneRegionMrg::p_center_command_scene_active_regions_handler);
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
        sceneRegionMrg.onWorldStart();
    }

    @Override
    protected void tickHook() {

    }

    @Override
    protected void shutdownHook() {

    }
}
