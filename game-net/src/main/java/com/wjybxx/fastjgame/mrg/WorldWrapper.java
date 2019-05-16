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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.async.AsyncNettyThreadMrg;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncNettyThreadMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;

/**
 * world需要的控制器的包装类，避免子类的构造方法出现大量对象
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:12
 * @github - https://github.com/hl845740757
 */
public class WorldWrapper {

    private final WorldInfoMrg worldInfoMrg;
    private final SystemTimeMrg systemTimeMrg;
    private final NetConfigMrg netConfigMrg;
    private final MessageDispatcherMrg messageDispatcherMrg;
    private final C2SSessionMrg c2SSessionMrg;
    private final S2CSessionMrg s2CSessionMrg;
    private final DisruptorMrg disruptorMrg;
    private final CodecHelperMrg codecHelperMrg;
    private final AsyncNettyThreadMrg asyncNettyThreadMrg;
    private final SyncNettyThreadMrg syncNettyThreadMrg;
    private final TokenMrg tokenMrg;
    private final TimerMrg timerMrg;
    private final HttpDispatcherMrg httpDispatcherMrg;
    private final HttpClientMrg httpClientMrg;
    private final SyncC2SSessionMrg syncC2SSessionMrg;
    private final SyncS2CSessionMrg syncS2CSessionMrg;
    private final SyncRequestDispatcherMrg syncRequestDispatcherMrg;
    private final GlobalExecutorMrg globalExecutorMrg;
    private final AcceptorMrg acceptorMrg;

    @Inject
    public WorldWrapper(WorldInfoMrg worldInfoMrg, MessageDispatcherMrg messageDispatcherMrg,
                        C2SSessionMrg c2SSessionMrg, S2CSessionMrg s2CSessionMrg,
                        SystemTimeMrg systemTimeMrg, DisruptorMrg disruptorMrg, NetConfigMrg netConfigMrg,
                        CodecHelperMrg codecHelperMrg, AsyncNettyThreadMrg asyncNettyThreadMrg, SyncNettyThreadMrg syncNettyThreadMrg,
                        TokenMrg tokenMrg, TimerMrg timerMrg, HttpDispatcherMrg httpDispatcherMrg, HttpClientMrg httpClientMrg,
                        SyncC2SSessionMrg syncC2SSessionMrg, SyncS2CSessionMrg syncS2CSessionMrg,
                        SyncRequestDispatcherMrg syncRequestDispatcherMrg, GlobalExecutorMrg globalExecutorMrg, AcceptorMrg acceptorMrg) {
        this.worldInfoMrg = worldInfoMrg;
        this.messageDispatcherMrg = messageDispatcherMrg;
        this.c2SSessionMrg = c2SSessionMrg;
        this.s2CSessionMrg = s2CSessionMrg;
        this.systemTimeMrg = systemTimeMrg;
        this.disruptorMrg = disruptorMrg;
        this.netConfigMrg = netConfigMrg;
        this.codecHelperMrg = codecHelperMrg;
        this.asyncNettyThreadMrg = asyncNettyThreadMrg;
        this.syncNettyThreadMrg = syncNettyThreadMrg;
        this.tokenMrg = tokenMrg;
        this.timerMrg = timerMrg;
        this.httpDispatcherMrg = httpDispatcherMrg;
        this.httpClientMrg = httpClientMrg;
        this.syncC2SSessionMrg = syncC2SSessionMrg;
        this.syncS2CSessionMrg = syncS2CSessionMrg;
        this.syncRequestDispatcherMrg = syncRequestDispatcherMrg;
        this.globalExecutorMrg = globalExecutorMrg;
        this.acceptorMrg = acceptorMrg;
    }

    public WorldInfoMrg getWorldInfoMrg() {
        return worldInfoMrg;
    }

    public MessageDispatcherMrg getMessageDispatcherMrg() {
        return messageDispatcherMrg;
    }

    public C2SSessionMrg getC2SSessionMrg() {
        return c2SSessionMrg;
    }

    public S2CSessionMrg getS2CSessionMrg() {
        return s2CSessionMrg;
    }

    public SystemTimeMrg getSystemTimeMrg() {
        return systemTimeMrg;
    }

    public DisruptorMrg getDisruptorMrg() {
        return disruptorMrg;
    }

    public NetConfigMrg getNetConfigMrg() {
        return netConfigMrg;
    }

    public CodecHelperMrg getCodecHelperMrg() {
        return codecHelperMrg;
    }

    public AsyncNettyThreadMrg getAsyncNettyThreadMrg() {
        return asyncNettyThreadMrg;
    }

    public SyncNettyThreadMrg getSyncNettyThreadMrg() {
        return syncNettyThreadMrg;
    }

    public TokenMrg getTokenMrg() {
        return tokenMrg;
    }

    public TimerMrg getTimerMrg() {
        return timerMrg;
    }

    public HttpDispatcherMrg getHttpDispatcherMrg() {
        return httpDispatcherMrg;
    }

    public HttpClientMrg getHttpClientMrg() {
        return httpClientMrg;
    }

    public SyncC2SSessionMrg getSyncC2SSessionMrg() {
        return syncC2SSessionMrg;
    }

    public SyncS2CSessionMrg getSyncS2CSessionMrg() {
        return syncS2CSessionMrg;
    }

    public SyncRequestDispatcherMrg getSyncRequestDispatcherMrg() {
        return syncRequestDispatcherMrg;
    }

    public GlobalExecutorMrg getGlobalExecutorMrg() {
        return globalExecutorMrg;
    }

    public AcceptorMrg getAcceptorMrg() {
        return acceptorMrg;
    }
}
