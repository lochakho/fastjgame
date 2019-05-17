package com.wjybxx.fastjgame.component;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.WorldCoreWrapper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.world.WorldCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:46
 * @github - https://github.com/hl845740757
 */
public class EgWorld extends WorldCore {

    private static final Logger logger= LoggerFactory.getLogger(EgWorld.class);

    @Inject
    public EgWorld(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
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
