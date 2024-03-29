package com.wjybxx.fastjgame.component;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.wjybxx.fastjgame.module.CoreModule;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.world.World;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:52
 * @github - https://github.com/hl845740757
 */
public class EGWorldCoreModule extends CoreModule {

    @Override
    protected void bindWorldAndWorldInfoMrg() {
        bind(WorldInfoMrg.class).to(EGWorldCoreInfoMrg.class).in(Singleton.class);
        bind(World.class).to(EgWorld.class).in(Singleton.class);
    }

    @Override
    protected void bindOthers() {

    }
}
