package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.misc.ProtoBufHashMappingStrategy;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.ProtoBufMessageSerializer;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;

import javax.annotation.Nonnull;

/**
 * SceneServer
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 21:45
 * @github - https://github.com/hl845740757
 */
public class SceneWorld extends WorldCore {

    @Inject
    public SceneWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
        super(worldWrapper, coreWrapper);
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        registerCodecHelper("protoBuf",new ProtoBufHashMappingStrategy(),
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
}
