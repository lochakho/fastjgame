package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.*;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:25
 * @github - https://github.com/hl845740757
 */
public abstract class WorldCore extends World{

    protected final ZkPathMrg zkPathMrg;
    protected final CuratorMrg curatorMrg;
    protected final GameConfigMrg gameConfigMrg;
    protected final GuidMrg guidMrg;

    @Inject
    public WorldCore(WorldWrapper worldWrapper, WorldCoreWrapper coreWrapper) {
        super(worldWrapper);
        zkPathMrg=coreWrapper.getZkPathMrg();
        curatorMrg=coreWrapper.getCuratorMrg();
        gameConfigMrg=coreWrapper.getGameConfigMrg();
        guidMrg=coreWrapper.getGuidMrg();
    }

    @Override
    protected void worldStartImp() throws Exception {
        curatorMrg.start();
    }

    @Override
    protected void tickImp(long curMillTime) {

    }

    @Override
    protected void beforeShutdown() throws Exception {
        curatorMrg.shutdown();
    }
}
