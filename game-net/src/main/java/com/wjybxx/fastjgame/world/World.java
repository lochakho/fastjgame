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

package com.wjybxx.fastjgame.world;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.mrg.async.AsyncNetServiceMrg;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncNetServiceMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.ClientMessageHandler;
import com.wjybxx.fastjgame.net.async.HttpRequestHandler;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.async.ServerMessageHandler;
import com.wjybxx.fastjgame.net.async.event.AckPingPongEventParam;
import com.wjybxx.fastjgame.net.async.event.ConnectResponseEventParam;
import com.wjybxx.fastjgame.net.async.event.LogicMessageEventParam;
import com.wjybxx.fastjgame.net.async.event.NetEvent;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.HttpRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.OkHttpResponseTO;
import com.wjybxx.fastjgame.net.common.*;
import com.wjybxx.fastjgame.net.sync.SyncRequestHandler;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * 游戏世界顶层类(World)
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 23:00
 * @github - https://github.com/hl845740757
 */
public abstract class World {

    private static final Logger logger= LoggerFactory.getLogger(World.class);

    protected final MessageDispatcherMrg messageDispatcherMrg;
    protected final C2SSessionMrg c2SSessionMrg;
    protected final S2CSessionMrg s2CSessionMrg;
    protected final SystemTimeMrg systemTimeMrg;
    protected final DisruptorMrg disruptorMrg;
    protected final CodecHelperMrg codecHelperMrg;
    protected final AsyncNetServiceMrg asyncNetServiceMrg;
    protected final SyncNetServiceMrg syncNetServiceMrg;
    protected final NetConfigMrg netConfigMrg;
    protected final TokenMrg tokenMrg;
    protected final TimerMrg timerMrg;
    protected final HttpDispatcherMrg httpDispatcherMrg;
    protected final HttpClientMrg httpClientMrg;
    protected final WorldInfoMrg worldInfoMrg;
    protected final SyncC2SSessionMrg syncC2SSessionMrg;
    protected final SyncS2CSessionMrg syncS2CSessionMrg;
    protected final SyncRequestDispatcherMrg syncRequestDispatcherMrg;
    protected final GlobalExecutorMrg globalExecutorMrg;

    @Inject
    public World(WorldWrapper worldWrapper) {
        messageDispatcherMrg=worldWrapper.getMessageDispatcherMrg();
        c2SSessionMrg = worldWrapper.getC2SSessionMrg();
        s2CSessionMrg = worldWrapper.getS2CSessionMrg();
        systemTimeMrg= worldWrapper.getSystemTimeMrg();
        disruptorMrg = worldWrapper.getDisruptorMrg();
        codecHelperMrg = worldWrapper.getCodecHelperMrg();
        asyncNetServiceMrg = worldWrapper.getAsyncNetServiceMrg();
        syncNetServiceMrg = worldWrapper.getSyncNetServiceMrg();
        netConfigMrg= worldWrapper.getNetConfigMrg();
        tokenMrg= worldWrapper.getTokenMrg();
        timerMrg= worldWrapper.getTimerMrg();
        httpDispatcherMrg =worldWrapper.getHttpDispatcherMrg();
        httpClientMrg =worldWrapper.getHttpClientMrg();
        worldInfoMrg = worldWrapper.getWorldInfoMrg();
        syncC2SSessionMrg=worldWrapper.getSyncC2SSessionMrg();
        syncS2CSessionMrg=worldWrapper.getSyncS2CSessionMrg();
        syncRequestDispatcherMrg=worldWrapper.getSyncRequestDispatcherMrg();
        globalExecutorMrg=worldWrapper.getGlobalExecutorMrg();
    }

    public final void onStart() throws Exception{
        // 初始化网络层需要的组件(codec帮助类)
        registerCodecHelper();

        // 注册要处理的异步普通消息和http请求和同步rpc请求
        registerMessageHandlers();
        registerHttpRequestHandlers();
        registerSyncRequestHandlers();

        // 注册会话处理器
        s2CSessionMrg.setSessionLifecycleAware(newAsyncSessionLifecycleAware());
        syncS2CSessionMrg.setLifecycleAware(newSyncSessionLifeCycleAware());

        globalExecutorMrg.start();
        // 启动netty线程(可保证线程安全性，netty线程可看见当前线程设置的值)
        asyncNetServiceMrg.start();
        syncNetServiceMrg.start();

        // 子类自己的其它启动逻辑
        worldStartImp();

        // 启动成功，时间切换到缓存策略
        systemTimeMrg.changeToCacheStrategy();
    }

    /**
     * 注册需要的编解码辅助类(序列化类，消息映射的初始化)
     * use {@link #registerCodecHelper(String, MessageMappingStrategy, MessageSerializer)} to register.
     */
    protected abstract void registerCodecHelper() throws Exception;

    /**
     * 注册codec的模板方法
     * @param name codec的名字
     * @param mappingStrategy 消息id到消息映射策略
     * @param messageSerializer 消息序列化反序列化实现类
     */
    protected final void registerCodecHelper(String name, MessageMappingStrategy mappingStrategy, MessageSerializer messageSerializer) throws Exception {
        Object2IntMap<Class<?>> mapper = mappingStrategy.mapping();
        MessageMapper messageMapper = new MessageMapper(mapper);
        messageSerializer.init(messageMapper);
        codecHelperMrg.registerCodecHelper(name,new CodecHelper(messageMapper,messageSerializer));
    }

    /**
     * 注册自己要处理的消息。也可以在自己的类中使用messageDispatcherMrg自己注册，不一定需要在world中注册。
     * use {@link #registerClientMessageHandler(Class, ClientMessageHandler)} and
     * {@link #registerServerMessageHandler(Class, ServerMessageHandler)}
     * to register
     */
    protected abstract void registerMessageHandlers();

    /**
     * 注册客户端发来的消息的处理器
     * @param messageClazz 消息类
     * @param clientMessageHandler 消息处理器
     * @param <T> 消息类型
     */
    protected final <T> void registerClientMessageHandler(Class<T> messageClazz, ClientMessageHandler<? super T> clientMessageHandler){
        messageDispatcherMrg.registerClientMessageHandler(messageClazz,clientMessageHandler);
    }

    /**
     * 注册服务器发来的消息的处理器
     * @param messageClazz 消息类型
     * @param serverMessageHandler 消息对应的处理器
     * @param <T> 消息类型
     */
    protected final <T> void registerServerMessageHandler(Class<T> messageClazz, ServerMessageHandler<? super T> serverMessageHandler){
        messageDispatcherMrg.registerServerMessageHandler(messageClazz,serverMessageHandler);
    }

    /**
     * 注册自己要处理的http请求
     * use {@link #registerHttpRequestHandler(String, HttpRequestHandler)} to register
     */
    protected abstract void registerHttpRequestHandlers();

    /**
     * 注册http请求处理器
     * @param path http请求路径
     * @param httpRequestHandler 对应的处理器
     */
    protected final void registerHttpRequestHandler(String path, HttpRequestHandler httpRequestHandler){
        httpDispatcherMrg.registerHandler(path,httpRequestHandler);
    }

    /**
     * 注册要处理的同步请求
     */
    protected abstract void registerSyncRequestHandlers();

    /**
     * 注册客户端的同步调用处理器
     * @param messageClass 消息类
     * @param handler 消息类对应的处理器
     * @param <T> 消息的类型
     * @param <R> 结果类型
     */
    protected final <T,R> void registerSyncRequestHandler(Class<T> messageClass, SyncRequestHandler<T,R> handler){
        syncRequestDispatcherMrg.registerHandler(messageClass,handler);
    }

    /**
     * 工厂方法，获取异步rpc会话生命周期处理器
     * @return
     */
    @Nonnull
    protected abstract SessionLifecycleAware<S2CSession> newAsyncSessionLifecycleAware();

    /**
     * 工厂方法，获取同步rpc会话生命周期处理器
     * @return
     */
    @Nonnull
    protected abstract SessionLifecycleAware<SyncS2CSession> newSyncSessionLifeCycleAware();


    /**
     * world子类自己的启动逻辑，不要轻易的在构造方法中写太多逻辑
     */
    protected abstract void worldStartImp() throws Exception;

    /**
     * 分发同步Rpc事件
     */
    public final void dispatchSyncRpcEvent(){
        syncS2CSessionMrg.dispatchSyncRpcRequest();
    }

    /**
     * 游戏世界帧
     * @param curMillTime
     */
    public final void tick(long curMillTime){
        // 优先更新系统时间缓存
        systemTimeMrg.tick(curMillTime);
        timerMrg.tickTrigger(curMillTime);
        c2SSessionMrg.tick();
        tickImp(curMillTime);
    }

    /**
     * 游戏世界的tick实现
     * @param curMillTime
     */
    protected abstract void tickImp(long curMillTime);

    /**
     * 网络事件
     * @param netEvent
     */
    public final void onNetEvent(NetEvent netEvent){
        switch (netEvent.getEventType()){
                // connect request response
            case CLIENT_CONNECT_REQUEST:
                s2CSessionMrg.onRcvConnectRequest(netEvent.getChannel(),(ConnectRequestTO) netEvent.getNetEventParam());
                break;
            case SERVER_CONNECT_RESPONSE:
                c2SSessionMrg.onRcvConnectResponse(netEvent.getChannel(), (ConnectResponseEventParam) netEvent.getNetEventParam());
                break;
                // ping-pong message
            case CLIENT_ACK_PING:
                s2CSessionMrg.onRcvClientAckPing(netEvent.getChannel(), (AckPingPongEventParam) netEvent.getNetEventParam());
                break;
            case SERVER_ACK_PONG:
                c2SSessionMrg.onRevServerAckPong(netEvent.getChannel(), (AckPingPongEventParam) netEvent.getNetEventParam());
                break;
                // logic message
            case CLIENT_LOGIC_MSG:
                s2CSessionMrg.onRcvClientLogicMsg(netEvent.getChannel(),(LogicMessageEventParam) netEvent.getNetEventParam());
                break;
            case SERVER_LOGIC_MSG:
                c2SSessionMrg.onRevServerLogicMsg(netEvent.getChannel(), (LogicMessageEventParam) netEvent.getNetEventParam());
                break;
                // http request
            case HTTP_REQUEST:
                httpDispatcherMrg.handleRequest(netEvent.getChannel(), (HttpRequestTO) netEvent.getNetEventParam());
                break;
                // ok http response
            case OK_HTTP_RESPONSE:
                httpDispatcherMrg.handleOkHttpResponse((OkHttpResponseTO) netEvent.getNetEventParam());
                break;
            case CHILD_CUSTOM_EVENTS:
                onChildEvent(netEvent);
                break;
            default:
                throw new IllegalArgumentException("unexpected event type " + netEvent.getEventType());
        }
    }

    /**
     * 如果子类定义了新的事件类型，必须覆盖该方法。
     * (即：允许子类将事件发布到RingBuffer)
     * @param netEvent 事件信息
     */
    protected void onChildEvent(NetEvent netEvent){
        throw new UnsupportedOperationException("unimplemented event handler " + netEvent.getEventType());
    }

    /**
     * 请求关闭，这是真正请求关闭游戏进程的方法。
     */
    public final void requestShutdown(){
        // 关闭期间可能较为耗时，切换到实时策略
        systemTimeMrg.changeToRealTimeStrategy();
        try {
            beforeShutdown();
        } catch (Exception e) {
            // 关闭操作和启动操作都是重要操作尽量不要产生异常
            logger.error("shutdown caught exception",e);
        } finally {
            shutdownNetWorld();
        }
    }

    /**
     * 子类需要实现的关闭钩子
     */
    protected abstract void beforeShutdown() throws Exception;

    /**
     * net world需要关闭的资源
     */
    private void shutdownNetWorld(){
        try {
            globalExecutorMrg.shutdown();
            asyncNetServiceMrg.shutdown();
            syncNetServiceMrg.shutdown();
            httpClientMrg.shutdown();
        }finally {
            disruptorMrg.shutdown();
        }
    }
}
