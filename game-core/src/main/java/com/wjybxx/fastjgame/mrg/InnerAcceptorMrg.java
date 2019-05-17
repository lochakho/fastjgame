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
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.misc.ProtoBufHashMappingStrategy;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.C2SSession;
import com.wjybxx.fastjgame.net.async.initializer.HttpServerInitializer;
import com.wjybxx.fastjgame.net.async.initializer.TCPClientChannelInitializer;
import com.wjybxx.fastjgame.net.async.initializer.TCPServerChannelInitializer;
import com.wjybxx.fastjgame.net.common.*;
import com.wjybxx.fastjgame.net.sync.SyncC2SSession;
import com.wjybxx.fastjgame.net.sync.initializer.ClientSyncRpcInitializer;
import com.wjybxx.fastjgame.net.sync.initializer.ServerSyncRpcInitializer;
import com.wjybxx.fastjgame.utils.GameUtils;

import java.util.function.Predicate;

/**
 * 服务器之间建立链接的帮助类,对{@link AcceptorMrg}的封装。
 * codec不可构造的时候缓存，因为构造的时候能还未注册。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 11:00
 * @github - https://github.com/hl845740757
 */
public class InnerAcceptorMrg {

    private final CodecHelperMrg codecHelperMrg;
    private final NetConfigMrg netConfigMrg;
    private final DisruptorMrg disruptorMrg;
    private final WorldInfoMrg worldInfoMrg;
    private final TokenMrg tokenMrg;

    private final C2SSessionMrg c2SSessionMrg;
    private final S2CSessionMrg s2CSessionMrg;
    private final SyncC2SSessionMrg syncC2SSessionMrg;
    private final SyncS2CSessionMrg syncS2CSessionMrg;
    private final HttpClientMrg httpClientMrg;

    @Inject
    public InnerAcceptorMrg(CodecHelperMrg codecHelperMrg, NetConfigMrg netConfigMrg,
                            DisruptorMrg disruptorMrg, S2CSessionMrg s2CSessionMrg, C2SSessionMrg c2SSessionMrg,
                            WorldInfoMrg worldInfoMrg, TokenMrg tokenMrg, SyncC2SSessionMrg syncC2SSessionMrg,
                            SyncS2CSessionMrg syncS2CSessionMrg, HttpClientMrg httpClientMrg) {
        this.codecHelperMrg = codecHelperMrg;
        this.netConfigMrg = netConfigMrg;
        this.disruptorMrg = disruptorMrg;
        this.s2CSessionMrg = s2CSessionMrg;
        this.c2SSessionMrg = c2SSessionMrg;
        this.worldInfoMrg = worldInfoMrg;
        this.tokenMrg = tokenMrg;
        this.syncC2SSessionMrg = syncC2SSessionMrg;
        this.syncS2CSessionMrg = syncS2CSessionMrg;
        this.httpClientMrg = httpClientMrg;
    }

    /**
     * 在worldCore创建时调用
     * @throws Exception
     */
    public void registerInnerCodecHelper() throws Exception {
        codecHelperMrg.registerCodecHelper(GameUtils.INNER_CODEC_NAME,
                new ProtoBufHashMappingStrategy(),
                new ProtoBufMessageSerializer());
    }

    private CodecHelper getInnerCodecHelper() {
        return codecHelperMrg.getCodecHelper(GameUtils.INNER_CODEC_NAME);
    }

    /**
     * 绑定异步tcp请求
     * @param outer 是否外网
     * @return
     */
    public HostAndPort bindInnerTcpPort(boolean outer){
        TCPServerChannelInitializer tcpServerChannelInitializer = new TCPServerChannelInitializer(netConfigMrg.maxFrameLength(),
                getInnerCodecHelper(),
                disruptorMrg);

        return s2CSessionMrg.bindRange(outer,GameUtils.INNER_TCP_PORT_RANGE,tcpServerChannelInitializer);
    }

    /**
     * 注册异步tcp会话
     * @param serverGuid 服务器guid
     * @param serverRoleType 服务器类型
     * @param tcpHostAndPort 服务器地址
     * @param lifecycleAware 会话生命周期通知
     */
    public void registerAsyncTcpSession(long serverGuid, RoleType serverRoleType, HostAndPort tcpHostAndPort, SessionLifecycleAware<C2SSession> lifecycleAware){
        Token loginToken = tokenMrg.newLoginToken(worldInfoMrg.getProcessGuid(),worldInfoMrg.getProcessType(),
                serverGuid,serverRoleType);
        byte[] tokenBytes = tokenMrg.encryptToken(loginToken);

        TCPClientChannelInitializer tcpClientChannelInitializer = new TCPClientChannelInitializer(netConfigMrg.maxFrameLength(),
                disruptorMrg, getInnerCodecHelper());

        c2SSessionMrg.register(serverGuid,serverRoleType,
                tcpHostAndPort,
                () -> tcpClientChannelInitializer,
                lifecycleAware,
                tokenBytes);
    }

    /**
     * 绑定一个同步会话断开
     * @param outer 是否外网
     * @return
     */
    public HostAndPort bindInnerSyncRpcPort(boolean outer){
        ServerSyncRpcInitializer serverSyncRpcInitializer = new ServerSyncRpcInitializer(netConfigMrg.maxFrameLength(),
                getInnerCodecHelper(), syncS2CSessionMrg);
        return syncS2CSessionMrg.bindRange(outer,GameUtils.INNER_SYNC_PORT_RANGE, serverSyncRpcInitializer);
    }

    /**
     * 注册同步会话信息。
     * 内部通信全部以异步会话为主，同步为辅，只要异步会话未删除，同步会话就可以保持重试。
     * @param serverGuid 服务器guid
     * @param serverRoleType 服务器角色类型
     * @param syncRpcHostAndPort syncRpc端口信息
     * @param reRegisterMatcher 重新注册的条件
     */
    public void registerSyncRpcSession(long serverGuid,RoleType serverRoleType, HostAndPort syncRpcHostAndPort,Predicate<SyncC2SSession> reRegisterMatcher){
        ClientSyncRpcInitializer syncRpcInitializer = new ClientSyncRpcInitializer(netConfigMrg.maxFrameLength(),
                netConfigMrg.syncRpcPingInterval(),
                getInnerCodecHelper(),
                syncC2SSessionMrg);

        syncC2SSessionMrg.registerServer(serverGuid,serverRoleType,
                syncRpcHostAndPort,
                () -> syncRpcInitializer,
                new ReRegisterAware(reRegisterMatcher));
    }

    public HostAndPort bindInnerHttpPort(){
        HttpServerInitializer httpServerInitializer = new HttpServerInitializer(disruptorMrg);
        return httpClientMrg.bindRange(true,GameUtils.INNER_HTTP_PORT_RANGE,httpServerInitializer);
    }

    /**
     * 创建一个只是简单重新注册的通知器
     * @param matcher 满足重新注册的条件
     */
    public SessionLifecycleAware<SyncC2SSession> newReRegisterAware(Predicate<SyncC2SSession> matcher){
        return new ReRegisterAware(matcher);
    }

    /**
     * 当会话断开时，如果符合条件，则重新注册的通知器
     */
    private class ReRegisterAware implements SessionLifecycleAware<SyncC2SSession>{

        private final Predicate<SyncC2SSession> matcher;

        private ReRegisterAware(Predicate<SyncC2SSession> matcher) {
            this.matcher = matcher;
        }

        @Override
        public void onSessionConnected(SyncC2SSession syncC2SSession) {

        }

        @Override
        public void onSessionDisconnected(SyncC2SSession syncC2SSession) {
            if (matcher.test(syncC2SSession)){
                syncC2SSessionMrg.registerServer(syncC2SSession.getServerGuid(), syncC2SSession.getRoleType(),
                        syncC2SSession.getHostAndPort(),
                        syncC2SSession.getInitializerSupplier(),
                        syncC2SSession.getLifeCycleAware());
            }
        }
    }
}
