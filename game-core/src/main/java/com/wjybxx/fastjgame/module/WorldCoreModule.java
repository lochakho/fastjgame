package com.wjybxx.fastjgame.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.wjybxx.fastjgame.mrg.*;

/**
 * WorldCoreModule
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:06
 * @github - https://github.com/hl845740757
 */
public class WorldCoreModule extends AbstractModule {

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        bind(ZkPathMrg.class).in(Singleton.class);
        bind(CuratorMrg.class).in(Singleton.class);
        bind(GameConfigMrg.class).in(Singleton.class);
        bind(GuidMrg.class).to(ZkGuidMrg.class).in(Singleton.class);
        bind(WorldCoreWrapper.class).in(Singleton.class);
    }
}
