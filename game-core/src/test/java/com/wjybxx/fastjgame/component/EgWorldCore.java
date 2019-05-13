package com.wjybxx.fastjgame.component;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.world.WorldCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:46
 * @github - https://github.com/hl845740757
 */
public class EgWorldCore extends WorldCore {

    private static final Logger logger= LoggerFactory.getLogger(EgWorldCore.class);

    @Inject
    public EgWorldCore(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
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
    protected void tickImp(long curMillTime) {

    }
}
