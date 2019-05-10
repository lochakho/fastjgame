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

package com.wjybxx.fastjgame.example.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.wjybxx.fastjgame.example.mrg.ExampleLoginServerInfoMrg;
import com.wjybxx.fastjgame.example.mrg.ExampleWorldInfoMrg;
import com.wjybxx.fastjgame.example.world.ExampleLoginServerWorld;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.world.World;

/**
 * 登录服module
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 15:35
 * @github - https://github.com/hl845740757
 */
public class ExampleLoginServerModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        binder().requireExplicitBindings();
        bind(WorldInfoMrg.class).to(ExampleLoginServerInfoMrg.class).in(Singleton.class);
        bind(World.class).to(ExampleLoginServerWorld.class).in(Singleton.class);
    }
}
