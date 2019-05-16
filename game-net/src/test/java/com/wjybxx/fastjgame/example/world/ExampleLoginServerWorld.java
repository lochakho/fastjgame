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

package com.wjybxx.fastjgame.example.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.example.bean.ServerInfo;
import com.wjybxx.fastjgame.example.jsonmsg.ExampleJsonMsg;
import com.wjybxx.fastjgame.example.jsonmsg.ExampleMappingStrategy;
import com.wjybxx.fastjgame.example.mrg.ExampleLoginServerInfoMrg;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.net.async.C2SSession;
import com.wjybxx.fastjgame.net.async.OkHttpResponseHandler;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.async.initializer.TCPClientChannelInitializer;
import com.wjybxx.fastjgame.net.async.initializer.WsClientChannelInitializer;
import com.wjybxx.fastjgame.net.common.*;
import com.wjybxx.fastjgame.net.sync.SyncC2SSession;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.net.sync.initializer.ClientSyncRpcInitializer;
import com.wjybxx.fastjgame.world.World;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import okhttp3.Call;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 示例游戏世界
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 0:33
 * @github - https://github.com/hl845740757
 */
public class ExampleLoginServerWorld extends World {

    private static final Logger logger= LoggerFactory.getLogger(ExampleLoginServerWorld.class);

    private final ExampleLoginServerInfoMrg loginServerInfoMrg;

    @Inject
    public ExampleLoginServerWorld(WorldWrapper worldWrapper) {
        super(worldWrapper);
        this.loginServerInfoMrg= (ExampleLoginServerInfoMrg) worldWrapper.getWorldInfoMrg();
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        // 测试时为了简单，使用json
        registerCodecHelper("json", new ExampleMappingStrategy(), new JsonMessageSerializer());
    }

    @Override
    protected void registerMessageHandlers() {
        registerServerMessageHandler(ExampleJsonMsg.LoginResponse.class,((session, message) -> {
            logger.info("rcv {}-{} {}",session.getRoleType(),session.getServerGuid(),message);
        }));
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
        // 该示例不会走到这里
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
        // 该示例不会走到这里
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
    protected void worldStartImp() {
        // 注册服务器
        for (ServerInfo serverInfo : loginServerInfoMrg.getServerMap().values()){
            registerTcpOrWsServer(serverInfo);
            registerSyncRpcServer(serverInfo);
        }
    }

    private void registerTcpOrWsServer(ServerInfo serverInfo) {
        if (serverInfo.getTcpAddress() == null && serverInfo.getWsAddress()==null){
            return;
        }
        if (serverInfo.getTcpAddress() !=null && serverInfo.getWsAddress()!=null){
            logger.info("websocket will be ignored");
        }

        CodecHelper codecHelper=codecHelperMrg.getCodecHelper("json");
        Token token= tokenMrg.newLoginToken(worldInfoMrg.getProcessGuid(), worldInfoMrg.getProcessType(),
                serverInfo.getServerGuid(), RoleType.CENTER_SERVER);
        byte[] encryptToken= tokenMrg.encryptToken(token);

        Supplier<ChannelInitializer<SocketChannel>> initializerSupplier;
        String address;
        // 连接tcp或ws端口的其中一个
        if (serverInfo.getTcpAddress() != null){
            address=serverInfo.getTcpAddress();
            initializerSupplier=()-> new TCPClientChannelInitializer(netConfigMrg.maxFrameLength(), disruptorMrg,codecHelper);
        }else {
            address=serverInfo.getWsAddress();
            String url="http://"+ address + "/ws";
            initializerSupplier=()-> new WsClientChannelInitializer(url,netConfigMrg.maxFrameLength(), disruptorMrg,codecHelper);
        }
        assert null!=address;
        HostAndPort hostAndPort= HostAndPort.parseHostAndPort(address);

        c2SSessionMrg.register(serverInfo.getServerGuid(), RoleType.CENTER_SERVER, hostAndPort,
                initializerSupplier,
                new ExampleSessionLifecycleAware(), encryptToken);
        serverInfo.setTcpOrWsRegistered(true);
    }

    /**
     * 注册rpc服务
     */
    private void registerSyncRpcServer(ServerInfo serverInfo) {
        String syncRpcAddress = serverInfo.getSyncRpcAddress();
        if (syncRpcAddress == null){
            return;
        }
        CodecHelper codecHelper=codecHelperMrg.getCodecHelper("json");
        HostAndPort hostAndPort= HostAndPort.parseHostAndPort(syncRpcAddress);

        syncC2SSessionMrg.registerServer(serverInfo.getServerGuid(), RoleType.CENTER_SERVER, hostAndPort,
                () -> new ClientSyncRpcInitializer(netConfigMrg.maxFrameLength(),
                        netConfigMrg.syncRpcPingInterval(),
                        codecHelper,
                        syncC2SSessionMrg),
                new ExampleSyncSessionLifeCycleAware());
        serverInfo.setSyncRpcRegistered(true);
    }

    @Override
    protected void tickImp(long curMillTime) {
        for (ServerInfo serverInfo : loginServerInfoMrg.getServerMap().values()){
            trySndTcpOrWsMessage(serverInfo);

            trySndSyncRpcRequest(serverInfo);

            trySndHttpRequest(serverInfo);
        }
    }

    private void trySndHttpRequest(ServerInfo serverInfo){
        if (null==serverInfo.getHttpAddress()){
            return;
        }
        Map<String,String> params=new LinkedHashMap<>();
        params.put("time",String.valueOf(systemTimeMrg.getSystemSecTime()));

        httpClientMrg.asyncGet("http://"+ serverInfo.getHttpAddress() + "/text?", params, new OkHttpResponseHandler() {
            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                logger.info("onFailed",e);
            }

            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response) throws IOException {
                logger.info("onResponse serverGuid {} body {}",serverInfo.getServerGuid(),response.body().string());
            }
        });
    }

    private void trySndSyncRpcRequest(ServerInfo serverInfo) {
        if (serverInfo.isSyncRpcRegistered()){
            ExampleJsonMsg.LoginRequest syncRequest = new ExampleJsonMsg.LoginRequest("syncRequest",
                    serverInfo.getSyncRpcSequencer().incAndGet());
            long startTime=System.currentTimeMillis();
            Optional<ExampleJsonMsg.LoginResponse> response = syncC2SSessionMrg.request(serverInfo.getServerGuid(), syncRequest,
                    ExampleJsonMsg.LoginResponse.class);
            if (response.isPresent()){
                long costTime=System.currentTimeMillis()-startTime;
                logger.info("serverGuid {} syncRpc request success,cost {} millTimes, response {}",serverInfo.getServerGuid(),costTime,response.get());
            }else {
                logger.info("serverGuid {} syncRpc request timeout",serverInfo.getServerGuid());
            }
        }
    }

    private void trySndTcpOrWsMessage(ServerInfo serverInfo) {
        if (serverInfo.isTcpOrWsRegistered()) {
            ExampleJsonMsg.LoginRequest loginRequest=new ExampleJsonMsg.LoginRequest("wjybxx",
                    serverInfo.getAccountSequencer().incAndGet());
            c2SSessionMrg.send(serverInfo.getServerGuid(),loginRequest);
        }
    }

    @Override
    protected void beforeShutdown() {

    }

    private class ExampleSessionLifecycleAware implements SessionLifecycleAware<C2SSession> {

        @Override
        public void onSessionConnected(C2SSession session) {
            logger.info("onSessionConnected {}",session);
        }

        @Override
        public void onSessionDisconnected(C2SSession session) {
            ServerInfo serverInfo = loginServerInfoMrg.getServerInfo(session.getServerGuid());
            serverInfo.setTcpOrWsRegistered(false);
            serverInfo.getAccountSequencer().set(0);
            logger.info("onSessionDisconnected {}",session);
        }
    }

    private class ExampleSyncSessionLifeCycleAware implements SessionLifecycleAware<SyncC2SSession>{

        @Override
        public void onSessionConnected(SyncC2SSession session) {
            logger.info("onSessionConnected {}",session);
        }

        @Override
        public void onSessionDisconnected(SyncC2SSession session) {
            ServerInfo serverInfo = loginServerInfoMrg.getServerInfo(session.getServerGuid());
            serverInfo.setSyncRpcRegistered(false);
            serverInfo.getSyncRpcSequencer().set(0);
            logger.info("onSessionDisconnected {}",session);
        }
    }

}
