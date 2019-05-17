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
import com.wjybxx.fastjgame.example.jsonmsg.ExampleJsonMsg;
import com.wjybxx.fastjgame.example.jsonmsg.ExampleMappingStrategy;
import com.wjybxx.fastjgame.example.mrg.ExampleGameServerInfoMrg;
import com.wjybxx.fastjgame.misc.HttpResponseHelper;
import com.wjybxx.fastjgame.mrg.WorldWrapper;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.async.initializer.HttpServerInitializer;
import com.wjybxx.fastjgame.net.async.initializer.TCPServerChannelInitializer;
import com.wjybxx.fastjgame.net.async.initializer.WsServerChannelInitializer;
import com.wjybxx.fastjgame.net.common.JsonMessageSerializer;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.sync.initializer.ServerSyncRpcInitializer;
import com.wjybxx.fastjgame.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.BindException;

/**
 * 示例游戏世界服务器
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 0:33
 * @github - https://github.com/hl845740757
 */
public class ExampleGameServerWorld extends World {

    private static final Logger logger= LoggerFactory.getLogger(ExampleGameServerWorld.class);

    private final ExampleGameServerInfoMrg gameServerInfoMrg;

    @Inject
    public ExampleGameServerWorld(WorldWrapper worldWrapper) {
        super(worldWrapper);
        this.gameServerInfoMrg= (ExampleGameServerInfoMrg) worldWrapper.getWorldInfoMrg();
    }

    @Override
    protected void registerCodecHelper() throws Exception {
        registerCodecHelper("json",new ExampleMappingStrategy(),new JsonMessageSerializer());
    }

    @Override
    protected void registerMessageHandlers() {
        registerRequestMessageHandler(ExampleJsonMsg.LoginRequest.class, ((session, message) -> {
            logger.info("rcv {}-{} {}.",session.getRoleType(),session.getClientGuid(),message);
            // 返回一个成功消息
            s2CSessionMrg.send(session.getClientGuid(), new ExampleJsonMsg.LoginResponse(message.getName(), message.getAccountId(),true));
        }));
    }

    @Override
    protected void registerHttpRequestHandlers() {
        registerHttpRequestHandler("/text",((httpSession, path, requestParams) -> {
            httpSession.writeAndFlush(HttpResponseHelper.newStringResponse(systemTimeMrg.toString()));
            logger.info("path={},{}",path,requestParams);
        }));

        registerHttpRequestHandler("/html",((httpSession, path, requestParams) -> {
            httpSession.writeAndFlush(HttpResponseHelper.newHtmlResponse(systemTimeMrg.toString()));
            logger.info("path={},{}",path,requestParams);
        }));

        registerHttpRequestHandler("/json",((httpSession, path, requestParams) -> {
            httpSession.writeAndFlush(HttpResponseHelper.newJsonResponse(systemTimeMrg));
            logger.info("path={},{}",path,requestParams);
        }));

        registerHttpRequestHandler("/seeother",((httpSession, path, requestParams) ->{
            httpSession.writeAndFlush(HttpResponseHelper.newRelocationResponse("http://www.baidu.com"));
            logger.info("path={},{}",path,requestParams);
        }));

        registerHttpRequestHandler("/shutdown",((httpSession, path, requestParams) ->{
            // 先同步返回已收到请求
            httpSession.writeAndFlush(HttpResponseHelper.newStringResponse("start shutdown"))
                        .syncUninterruptibly();
            logger.info("request shutdown!");
            requestShutdown();
        }));
    }

    @Override
    protected void registerSyncRequestHandlers() {
        registerSyncRequestHandler(ExampleJsonMsg.LoginRequest.class,((session, request) -> {
            logger.info("receive syncRpcRequest {}",request);
            ExampleJsonMsg.LoginResponse response = new ExampleJsonMsg.LoginResponse(request.getName(), request.getAccountId(), true);
            logger.info("send response {}",response);
            return response;
        }));
    }

    @Override
    protected void registerAsyncSessionLifeAware(S2CSessionMrg s2CSessionMrg) {
        this.s2CSessionMrg.registerLifeCycleAware(RoleType.LOGIN_SERVER,new ExampleS2CSessionLifecycleAware());
    }

    @Override
    protected void registerSyncSessionLifeAware(SyncS2CSessionMrg syncS2CSessionMrg) {

    }

    @Override
    protected void worldStartImp() throws BindException {
        listenOrConnect();
    }

    protected void listenOrConnect() throws BindException {
        boolean outer=true;
        // tcp监听
        int tcpPort=gameServerInfoMrg.getTcpPort();
        if (tcpPort>0){
            s2CSessionMrg.bind(outer, tcpPort,new TCPServerChannelInitializer(
                    netConfigMrg.maxFrameLength(),
                    codecHelperMrg.getCodecHelper("json"),
                    disruptorMrg
            ));
        }

        // websocket监听
        int websocketPort=gameServerInfoMrg.getWsPort();
        if (websocketPort>0){
            s2CSessionMrg.bind(outer, websocketPort,new WsServerChannelInitializer(
                    "/ws",
                    netConfigMrg.maxFrameLength(),
                    codecHelperMrg.getCodecHelper("json"),
                    disruptorMrg
            ));
        }
        // http监听
        int httpPort=gameServerInfoMrg.getHttpPort();
        if (httpPort>0){
            httpClientMrg.bind(outer,httpPort,new HttpServerInitializer(disruptorMrg));
        }
        // 同步rpc调用监听
        int syncRpcPort=gameServerInfoMrg.getSyncRpcPort();
        if (syncRpcPort>0){
            syncS2CSessionMrg.bind(outer,syncRpcPort,new ServerSyncRpcInitializer(
                    netConfigMrg.maxFrameLength(),
                    codecHelperMrg.getCodecHelper("json"),
                    syncS2CSessionMrg
            ));
        }
    }

    @Override
    protected void tickImp(long curMillTime) {

    }

    @Override
    protected void beforeShutdown() {

    }

    private class ExampleS2CSessionLifecycleAware implements SessionLifecycleAware<S2CSession> {

        @Override
        public void onSessionConnected(S2CSession session) {
            logger.info("sessionConnect {}",session);
        }

        @Override
        public void onSessionDisconnected(S2CSession session) {
            logger.info("sessionDisconnect {}", session);
        }
    }
}
