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
import com.wjybxx.fastjgame.mrg.async.AsyncNettyThreadMrg;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncNettyThreadMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;

/**
 * 网络层Module
 * 其他module需要提供以下类的实现：
 * {@link com.wjybxx.fastjgame.world.World}
 * {@link WorldInfoMrg}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/26 23:11
 */
public final class NetModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();
        binder().requireExplicitBindings();

        bind(SystemTimeMrg.class).in(Singleton.class);
        bind(CodecHelperMrg.class).in(Singleton.class);
        bind(NetConfigMrg.class).in(Singleton.class);
        bind(AsyncNettyThreadMrg.class).in(Singleton.class);
        bind(C2SSessionMrg.class).in(Singleton.class);
        bind(S2CSessionMrg.class).in(Singleton.class);
        bind(MessageDispatcherMrg.class).in(Singleton.class);
        bind(DisruptorMrg.class).in(Singleton.class);
        bind(WorldWrapper.class).in(Singleton.class);
        bind(TokenMrg.class).in(Singleton.class);
        bind(TimerMrg.class).in(Singleton.class);
        bind(HttpDispatcherMrg.class).in(Singleton.class);
        bind(HttpClientMrg.class).in(Singleton.class);
        bind(SyncNettyThreadMrg.class).in(Singleton.class);
        bind(SyncS2CSessionMrg.class).in(Singleton.class);
        bind(SyncC2SSessionMrg.class).in(Singleton.class);
        bind(SyncRequestDispatcherMrg.class).in(Singleton.class);
        bind(GlobalExecutorMrg.class).in(Singleton.class);
        bind(AcceptorMrg.class).in(Singleton.class);
    }

}
