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

package com.wjybxx.fastjgame.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.wjybxx.fastjgame.mrg.*;

/**
 * CoreModule
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 12:06
 * @github - https://github.com/hl845740757
 */
public abstract class CoreModule extends AbstractModule {

    // 这样改造之后为典型的模板方法
    @Override
    protected final void configure() {
        binder().requireExplicitBindings();
        configCore();

        bindWorldAndWorldInfoMrg();

        bindOthers();
    }

    private void configCore() {
        bind(CuratorMrg.class).in(Singleton.class);
        bind(GameConfigMrg.class).in(Singleton.class);
        bind(GuidMrg.class).to(ZkGuidMrg.class).in(Singleton.class);
        bind(WorldCoreWrapper.class).in(Singleton.class);
        bind(InnerAcceptorMrg.class).in(Singleton.class);
        bind(MongoDBMrg.class).in(Singleton.class);
    }

    /**
     * 请注意绑定{@link com.wjybxx.fastjgame.world.World}类和
     * {@link WorldInfoMrg}
     */
    protected abstract void bindWorldAndWorldInfoMrg();

    /**
     * 绑定其它的类
     */
    protected abstract void bindOthers();
}
