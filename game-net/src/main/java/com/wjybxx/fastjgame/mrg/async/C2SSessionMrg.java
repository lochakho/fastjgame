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

package com.wjybxx.fastjgame.mrg.async;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.misc.IntSequencer;
import com.wjybxx.fastjgame.mrg.MessageDispatcherMrg;
import com.wjybxx.fastjgame.mrg.NetConfigMrg;
import com.wjybxx.fastjgame.mrg.SystemTimeMrg;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.net.async.*;
import com.wjybxx.fastjgame.net.async.event.AckPingPongEventParam;
import com.wjybxx.fastjgame.net.async.event.ConnectResponseEventParam;
import com.wjybxx.fastjgame.net.async.event.LogicMessageEventParam;
import com.wjybxx.fastjgame.net.async.event.NetEventParam;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 客户端到服务器的会话控制器
 * (我发起的连接)
 *
 * 客户端什么时候断开连接？
 * 1.服务器通知验证失败(服务器让我移除)
 * 2.外部调用{@link #removeSession(long, String)}
 * 3.消息缓存数超过限制
 * 4.限定时间内无法连接到服务器
 * 5.验证结果表明服务器的sequence和ack异常时。
 *
 * 什么时候会关闭channel？
 * {@link #removeSession(long, String)}
 * {@link C2SSessionState#closeChannel()}
 * {@link ConnectedState#reconnect(String)}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:10
 * @github - https://github.com/hl845740757
 */
public class C2SSessionMrg {

    private static final Logger logger= LoggerFactory.getLogger(C2SSessionMrg.class);

    private final WorldInfoMrg worldInfoMrg;
    private final NetConfigMrg netConfigMrg;
    private final AsyncNetServiceMrg asyncNetServiceMrg;
    private final SystemTimeMrg systemTimeMrg;
    private final MessageDispatcherMrg dispatcherMrg;

    /**
     * 客户端发起的所有会话,注册时加入，close时删除
     * serverGuid --> session
     */
    private final Long2ObjectMap<SessionWrapper> sessionWrapperMap =new Long2ObjectOpenHashMap<>();

    @Inject
    public C2SSessionMrg(WorldInfoMrg worldInfoMrg, NetConfigMrg netConfigMrg, AsyncNetServiceMrg asyncNetServiceMrg,
                         SystemTimeMrg systemTimeMrg, MessageDispatcherMrg dispatcherMrg) {
        this.worldInfoMrg = worldInfoMrg;
        this.netConfigMrg = netConfigMrg;
        this.asyncNetServiceMrg = asyncNetServiceMrg;
        this.systemTimeMrg = systemTimeMrg;
        this.dispatcherMrg = dispatcherMrg;
    }

    public void tick(){
        for (SessionWrapper sessionWrapper: sessionWrapperMap.values()){
            if (sessionWrapper.getState()==null){
                continue;
            }
            sessionWrapper.getState().execute();
        }
    }

    /**
     * 获取session
     * @param serverGuid 服务器guid
     * @return 如果存在则返回对应的session，否则返回null
     */
    @Nullable
    public C2SSession getSession(long serverGuid){
        SessionWrapper wrapper = sessionWrapperMap.get(serverGuid);
        return null==wrapper?null:wrapper.getSession();
    }

    /**
     * 注册一个服务器
     * @param serverGuid 在登录服或别处获得的serverGuid
     * @param roleType 服务器类型
     * @param host 服务器ip
     * @param port 服务器监听的端口号
     * @param initializerSupplier 初始化器提供者，如果initializer是线程安全的，可以始终返回同一个对象
     * @param lifecycleHandler 作为客户端，链接不同的服务器时，可能有不同的生命周期事件处理
     * @param encryptedLoginToken 在登录服或别处获得的登录用的token
     */
    public C2SSession register(long serverGuid, RoleType roleType, String host, int port,
                         Supplier<ChannelInitializer<SocketChannel>> initializerSupplier,
                         SessionLifecycleAware<C2SSession> lifecycleHandler, byte[] encryptedLoginToken){
        // 已注册
        if (sessionWrapperMap.containsKey(serverGuid)){
            throw new IllegalArgumentException("serverGuid " + serverGuid+ " registered before.");
        }
        // 创建会话，尚未激活
        C2SSession session = new C2SSession(serverGuid, roleType, host,port, initializerSupplier, lifecycleHandler);
        SessionWrapper sessionWrapper=new SessionWrapper(session, encryptedLoginToken);
        sessionWrapperMap.put(session.getServerGuid(), sessionWrapper);
        logger.debug("register session {}",session);
        // 初始为连接状态
        changeState(sessionWrapper,new ConnectingState(sessionWrapper));
        return session;
    }

    /**
     * 向服务器发送一个消息,不保证立即发送，因为会话状态不确定，只保证最后一定会按顺序发送出去
     */
    public void send(long serverGuid,@Nonnull Object message){
        SessionWrapper wrapper = sessionWrapperMap.get(serverGuid);
        if (null== wrapper){
            logger.warn("server {} is removed, but try send message.",serverGuid);
            return;
        }
        MessageQueue messageQueue =wrapper.getMessageQueue();
        if (messageQueue.getCacheMessageNum() >= netConfigMrg.clientMaxCacheNum()){
            // 缓存过多，删除会话
            removeSession(serverGuid,"cacheMessageNum is too much!");
        }else {
            // 添加到待发送队列
            LogicMessage logicMessage=new LogicMessage(messageQueue.nextSequence(),message);
            messageQueue.getNeedSendQueue().addLast(logicMessage);
        }
    }

    /**
     * 关闭一个会话，如果注册了的话
     * @param serverGuid sessionId
     */
    public @Nullable C2SSession removeSession(long serverGuid, String reason){
        SessionWrapper sessionWrapper = sessionWrapperMap.remove(serverGuid);
        if (null==sessionWrapper){
            return null;
        }
        C2SSession session=sessionWrapper.getSession();
        try{
            // 验证成功过才会执行断开回调操作(调用过onSessionConnected方法)
            if (session.getLifecycleAware()!=null && sessionWrapper.getVerifiedSequencer().get() > 0){
                session.getLifecycleAware().onSessionDisconnected(session);
            }
        } catch (Exception e){
            logger.warn("disconnected callback caught exception.",reason,session);
        }finally {
            // 移除之前进行必要的清理
            if (sessionWrapper.getState()!=null){
                sessionWrapper.getState().closeChannel();
                sessionWrapper.setState(null);
            }
        }
        logger.info("remove session by reason of {}, session info={}.",reason,session);
        return session;
    }

    // region 事件处理

    private <T extends NetEventParam> void ifEventChannelOK(Channel eventChannel, T eventParam, Consumer<C2SSessionState> then){
        SessionWrapper sessionWrapper = sessionWrapperMap.get(eventParam.sessionGuid());
        // 非法的channel
        if (sessionWrapper==null){
            NetUtils.closeQuietly(eventChannel);
            return;
        }
        // 校验收到消息的channel是否合法
        C2SSessionState sessionState = sessionWrapper.getState();
        if (!sessionState.isEventChannelOK(eventChannel)){
            NetUtils.closeQuietly(eventChannel);
            return;
        }
        then.accept(sessionState);
    }

    /**
     * 当收到服务器的Token验证结果
     * @param eventChannel 产生事件的channel
     * @param responseParam 连接响应结果
     */
    public void onRcvConnectResponse(Channel eventChannel, ConnectResponseEventParam responseParam){
        ifEventChannelOK(eventChannel,responseParam, sessionState -> {
            // 无论什么状态，只要当前channel收到token验证失败，都关闭session(移除会话)，它意味着服务器通知关闭。
            if (!responseParam.isSuccess()){
                NetUtils.closeQuietly(eventChannel);
                removeSession(responseParam.getServerGuid(),"token check failed.");
                return;
            }
            // token验证成功
            sessionState.onTokenCheckSuccess(eventChannel, responseParam);
        });
    }

    /**
     * 当收到服务器的ping包返回时
     * @param eventChannel 产生事件的channel
     * @param ackPongParam 服务器返回的pong包
     */
    public void onRevServerAckPong(Channel eventChannel, AckPingPongEventParam ackPongParam){
        ifEventChannelOK(eventChannel,ackPongParam, sessionState -> {
            sessionState.onRcvServerAckPong(eventChannel, ackPongParam);
        });
    }

    /**
     * 当收到服务器的消息时
     * @param eventChannel 产生事件的channel
     * @param logicMessageParam 服务器发来的业务逻辑包
     */
    public void onRevServerLogicMsg(Channel eventChannel, LogicMessageEventParam logicMessageParam){
        ifEventChannelOK(eventChannel,logicMessageParam, sessionState -> {
            sessionState.onRcvServerMessage(eventChannel, logicMessageParam);
        });
    }

    // endregion

    // ------------------------------------------------状态机------------------------------------------------

    /**
     * 切换session的状态
     */
    private void changeState(SessionWrapper sessionWrapper, C2SSessionState newState){
        sessionWrapper.setState(newState);;
        if (sessionWrapper.getState()!=null){
            sessionWrapper.getState().enter();
        }
    }

    /**
     * 客户端只会有三个网络事件(三种类型协议)，
     * 1.token验证结果
     * 2.服务器ack-ping返回协议(ack-pong)
     * 3.服务器发送的正式消息
     *
     * 同时也只有三种状态：
     * 1.尝试连接状态
     * 2.正在验证状态
     * 3.已验证状态
     */
    private abstract class C2SSessionState{

        final SessionWrapper sessionWrapper;

        final C2SSession session;

        C2SSessionState(SessionWrapper sessionWrapper) {
            this.sessionWrapper = sessionWrapper;
            this.session = sessionWrapper.session;
        }

        MessageQueue getMessageQueue(){
            return sessionWrapper.messageQueue;
        }

        IntSequencer getVerifiedSequencer(){
            return sessionWrapper.getVerifiedSequencer();
        }

        IntSequencer getSndTokenSequencer(){
            return sessionWrapper.getSndTokenSequencer();
        }

        protected abstract void enter();

        protected abstract void execute();

        // 为何不要exit 因为根本不保证exit能走到,此外导致退出状态的原因太多，要做的事情并不一致，因此重要逻辑不能依赖exit

        /**
         * 在session关闭之前进行资源的清理，清理该状态自身持有的资源
         * (主要是channel)
         */
        public abstract void closeChannel();

        /**
         * 产生事件的channel是否OK，客户端只有当前持有的channel是合法的，因此很多地方是比较简单的。
         * @param eventChannel 产生事件的channel
         * @return 当产生事件的channel是期望的channel时返回true
         */
        protected abstract boolean isEventChannelOK(Channel eventChannel);

        /**
         * 当收到服务器的token验证成功消息
         * @param eventChannel 产生事件的channel
         * @param resultParam 返回信息
         */
        protected void onTokenCheckSuccess(Channel eventChannel, ConnectResponseEventParam resultParam){
            throw new IllegalStateException(this.getClass().getSimpleName());
        }

        /**
         * 当收到服务器的ack-pong消息包
         * @param eventChannel 产生事件的channel
         * @param ackPongParam 服务器返回的pong包
         */
        protected void onRcvServerAckPong(Channel eventChannel, AckPingPongEventParam ackPongParam){
            throw new IllegalStateException(this.getClass().getSimpleName());
        }

        /**
         * 当收到服务器的逻辑消息包
         * @param eventChannel 产生事件的channel
         * @param messageParam 服务器发来的业务逻辑包
         */
        protected void onRcvServerMessage(Channel eventChannel, LogicMessageEventParam messageParam){
            throw new IllegalStateException(this.getClass().getSimpleName());
        }
    }

    /**
     * 连接状态
     */
    private class ConnectingState extends C2SSessionState {

        private ChannelFuture channelFuture;
        /**
         * 已尝试连接次数
         */
        private int tryTimes=0;
        /**
         * 连接开始时间
         */
        private long connectStartTime=0;

        ConnectingState(SessionWrapper sessionWrapper) {
            super(sessionWrapper);
        }

        @Override
        protected void enter() {
            tryConnect();
        }

        private void tryConnect(){
            tryTimes++;
            connectStartTime=systemTimeMrg.getSystemMillTime();
            channelFuture = asyncNetServiceMrg.connectAsyn(session.getHost(), session.getPort(), session.getInitializerSupplier().get());
            logger.debug("tryConnect remote {}:{} ,tryTimes {}.", session.getHost(), session.getPort(),tryTimes);
        }

        @Override
        protected void execute() {
            // 建立连接成功
            if (channelFuture.isSuccess() && channelFuture.channel().isActive()){
                logger.debug("connect remote {}:{} success,tryTimes {}.", session.getHost(), session.getPort(),tryTimes);
                changeState(sessionWrapper,new VerifyingState(sessionWrapper,channelFuture.channel()));
                return;
            }
            // 还未超时
            if (systemTimeMrg.getSystemMillTime()-connectStartTime< netConfigMrg.connectTimeout()){
                return;
            }
            // 本次建立连接超时，关闭当前future,并再次尝试
            closeFuture();

            if (tryTimes < netConfigMrg.connectMaxTryTimes()){
                // 还可以继续尝试
                tryConnect();
            }else {
                // 无法连接到服务器，移除会话，结束
                removeSession(session.getServerGuid(),"can't connect to server.");
            }
        }

        private void closeFuture() {
            NetUtils.closeQuietly(channelFuture);
            channelFuture=null;
        }

        @Override
        public void closeChannel() {
            closeFuture();
        }

        @Override
        protected boolean isEventChannelOK(Channel eventChannel) {
            // 永远返回false，当前状态下不会响应其它事件
            return false;
        }
    }

    /**
     * 已建立链接状态，可重连状态
     */
    private abstract class ConnectedState extends C2SSessionState{
        /**
         * 已建立连接的channel
         * (已连接的意思是：socket已连接)
         */
        protected final Channel channel;

        ConnectedState(SessionWrapper sessionWrapper, Channel channel) {
            super(sessionWrapper);
            this.channel=channel;
        }

        @Override
        public final void closeChannel() {
            NetUtils.closeQuietly(channel);
        }

        /**
         * 只会响应当前channel的消息事件
         */
        @Override
        protected final boolean isEventChannelOK(Channel eventChannel) {
            return this.channel == eventChannel;
        }

        /**
         * 重连
         * @param reason 重连的原因
         */
        final void reconnect(String reason){
            NetUtils.closeQuietly(channel);
            changeState(sessionWrapper,new ConnectingState(sessionWrapper));
            logger.info("reconnect by reason of {}",reason);
        }

    }

    /**
     * 正在验证状态。
     *
     * 1.如果限定时间内未收到任何消息，则尝试重新连接。
     * 2.收到其它消息，但未收到token验证结果时：，则会再次进行验证。
     * 3.收到验证结果：
     * <li>任何状态下收到验证失败都会关闭session</li>
     * <li>验证成功且判定服务器的ack正确时，验证完成</li>
     * <li>验证成功但判定服务器的ack错误时，关闭session</li>
     */
    private class VerifyingState extends ConnectedState{
        /**
         * 进入状态机的时间戳，用于计算token响应超时
         */
        private long enterStateMillTime;

        VerifyingState(SessionWrapper sessionWrapper, Channel channel) {
            super(sessionWrapper,channel);
        }

        /**
         * 发送token
         */
        @Override
        protected void enter() {
            enterStateMillTime=systemTimeMrg.getSystemMillTime();

            int sndTokenTimes=getSndTokenSequencer().incAndGet();
            // 创建验证请求
            ConnectRequestTO connectRequest = new ConnectRequestTO(worldInfoMrg.getWorldGuid(), session.getServerGuid(),
                    sndTokenTimes, getMessageQueue().getAck(), sessionWrapper.getEncryptedToken());
            channel.writeAndFlush(connectRequest);
            logger.debug("{} times send verify msg to server {}",sndTokenTimes,session);
        }

        @Override
        protected void execute() {
            if (systemTimeMrg.getSystemMillTime()-enterStateMillTime> netConfigMrg.waitTokenResultTimeout()){
                // 获取token结果超时，重连
                reconnect("wait token result timeout.");
            }
        }

        @Override
        protected void onTokenCheckSuccess(Channel eventChannel, ConnectResponseEventParam resultParam) {
            // 不是等待的结果
            if (resultParam.getSndTokenTimes() != getSndTokenSequencer().get()){
                return;
            }
            MessageQueue messageQueue = getMessageQueue();
            // 收到的ack有误(有丢包)，这里重连已没有意义(始终有消息漏掉了，无法恢复)
            if (!messageQueue.isAckOK(resultParam.getAck())){
                removeSession(resultParam.getServerGuid(),"server ack is error. ackInfo="+messageQueue.generateAckErrorInfo(resultParam.getAck()));
                return;
            }
            // 更新消息队列
            sessionWrapper.getMessageQueue().updateSentQueue(resultParam.getAck());
            // 保存新的token
            sessionWrapper.setEncryptedToken(resultParam.getEncrytedToken());
            changeState(sessionWrapper,new VerifiedState(sessionWrapper,channel));
        }

        @Override
        protected void onRcvServerAckPong(Channel eventChannel, AckPingPongEventParam ackPongParam) {
            reconnect("onRcvServerAckPong,but missing token result");
        }

        @Override
        protected void onRcvServerMessage(Channel eventChannel, LogicMessageEventParam messageParam) {
            reconnect("onRcvServerMessage,but missing token result");
        }
    }

    /**
     * token验证成功状态
     */
    private class VerifiedState extends ConnectedState{
        /**
         * 当前队列是否有ping包，避免遍历
         */
        private boolean hasPingMessage;
        /**
         * 上次向服务器发送消息的时间。
         * 它的重要作用是避免双方缓存队列过大，尤其是降低服务器压力。
         */
        private int lastSendMessageTime;

        VerifiedState(SessionWrapper sessionWrapper, Channel channel) {
            super(sessionWrapper,channel);
        }

        @Override
        protected void enter() {
            hasPingMessage=false;
            lastSendMessageTime=systemTimeMrg.getSystemSecTime();

            int verifiedTimes = getVerifiedSequencer().incAndGet();
            // 增加验证次数
            if (verifiedTimes==1){
                logger.info("first verified success, sessionInfo={}",session);

                if (session.getLifecycleAware()!=null){
                    session.getLifecycleAware().onSessionConnected(session);
                }
            }else {
                logger.info("reconnect verified success, verifiedTimes={},sessionInfo={}",verifiedTimes,session);

                // 重发未确认接受到的消息
                MessageQueue messageQueue= getMessageQueue();
                if (messageQueue.getSentQueue().size()>0){
                    for (Message message:messageQueue.getSentQueue()){
                        channel.write(message.build(messageQueue.getAck()));
                    }
                    channel.flush();
                }
            }
        }

        @Override
        protected void execute() {
            MessageQueue messageQueue= getMessageQueue();
            // 检查消息超时
            if (messageQueue.getSentQueue().size()>0){
                long firstMessageTimeout=messageQueue.getSentQueue().getFirst().getTimeout();
                // 超时未收到第一条消息的ack
                if (systemTimeMrg.getSystemMillTime()>=firstMessageTimeout){
                    reconnect("first msg of sentQueue messageTimeout.");
                    return;
                }
            }

            // 是否需要发送ack-ping包，ping包服务器收到一定是会返回的，而普通消息则不一定。
            if (isNeedSendAckPing()){
                AckPingPongMessage ackPingMessage=new AckPingPongMessage(messageQueue.nextSequence());
                messageQueue.getNeedSendQueue().add(ackPingMessage);
                hasPingMessage=true;
            }

            // 有待发送的消息则发送
            if (messageQueue.getNeedSendQueue().size()>0){
                // 发送消息
                while (messageQueue.getNeedSendQueue().size()>0){
                    // 添加到已发送队列
                    Message message = messageQueue.getNeedSendQueue().removeFirst();
                    messageQueue.getSentQueue().addLast(message);
                    // 更新ack超时时间
                    message.setTimeout(nextAckTimeout());
                    channel.write(message.build(messageQueue.getAck()));
                }
                channel.flush();
                lastSendMessageTime=systemTimeMrg.getSystemSecTime();
            }
        }

        /**
         * 是否需要发送ack-ping包
         * 什么时候需要发？？？
         * 需要同时满足以下条件：
         * 1.当前无ping消息等待结果
         * 2.当前无待发送消息
         * 3.距离最后一条消息发送过去了超时时长的一半 或 长时间未收到服务器消息
         * @return 满足以上条件时返回true，否则返回false。
         */
        private boolean isNeedSendAckPing(){
            // 有ping包还未返回
            if (hasPingMessage){
                return false;
            }
            MessageQueue messageQueue= getMessageQueue();
            // 有待发送的逻辑包
            if (messageQueue.getNeedSendQueue().size()>0){
                return false;
            }
            // 判断发送的最后一条消息的的等待确认时长是否过去了一半(降低都是无返回的消息时导致的超时概率)
            // 如果每次发的都是无返回的协议也太极限了，我们在游戏中不考虑这种情况,通过重连解决该问题
            if (messageQueue.getSentQueue().size()>0){
                long ackTimeout=messageQueue.getSentQueue().getLast().getTimeout();
                return ackTimeout - systemTimeMrg.getSystemMillTime() <= netConfigMrg.ackTimeout()/2;
            }
            // 已经有一段时间没有向服务器发送消息了(session超时时间过去1/3)，保活和降低服务器内存压力
            return systemTimeMrg.getSystemSecTime() - lastSendMessageTime >=netConfigMrg.sessionTimeout()/3;
        }

        private long nextAckTimeout(){
            return systemTimeMrg.getSystemMillTime()+ netConfigMrg.ackTimeout();
        }

        @Override
        protected void onRcvServerAckPong(Channel eventChannel, AckPingPongEventParam ackPongParam) {
            hasPingMessage=false;
            tryUpdateMessageQueue(ackPongParam.getAckPingPongMessage());
        }

        @Override
        protected void onRcvServerMessage(Channel eventChannel, LogicMessageEventParam messageParam) {
            boolean success = tryUpdateMessageQueue(messageParam.messageTO());
            if (success){
                dispatcherMrg.handleServerMessage(session,messageParam.messageTO().getMessage());
            }
        }

        /**
         * 尝试更新消息队列
         * @param message 服务器发来的消息(pong包或业务逻辑包)
         * @return 当服务器发来的消息是期望的下一个消息，且ack正确时返回true
         */
        final boolean tryUpdateMessageQueue(MessageTO message){
            MessageQueue messageQueue = getMessageQueue();
            // 不是期望的下一个消息,请求重传
            if (message.getSequence() != messageQueue.getAck()+1){
                reconnect("serverSequence != ack()+1, serverSequence=" + message.getSequence() + ", ack="+messageQueue.getAck());
                return false;
            }
            // 服务器ack不对，尝试矫正
            if (!messageQueue.isAckOK(message.getAck())){
                reconnect("server ack error,ackInfo="+messageQueue.generateAckErrorInfo(message.getAck()));
                return false;
            }
            messageQueue.setAck(message.getSequence());
            messageQueue.updateSentQueue(message.getAck());
            return true;
        }
    }

    /**
     * session包装对象
     * 不将额外信息暴露给应用层
     */
    private static class SessionWrapper {
        /**
         * 客户端与服务器之间的会话信息
         */
        private final C2SSession session;
        /**
         * 客户端是消息队列
         */
        private final MessageQueue messageQueue=new MessageQueue();
        /**
         * 发送token次数
         */
        private final IntSequencer sndTokenSequencer =new IntSequencer(0);
        /**
         * 验证成功的次数
         * (也等于收到token结果的次数，因为验证失败，就会删除session)
         */
        private final IntSequencer verifiedSequencer =new IntSequencer(0);
        /**
         * 被加密的Token，客户端并不关心具体内容，只是保存用于建立链接
         */
        private byte[] encryptedToken;
        /**
         * 会话当前状态
         */
        private C2SSessionState state;

        private SessionWrapper(C2SSession session, byte[] encryptedToken) {
            this.session = session;
            this.encryptedToken = encryptedToken;
        }

        public C2SSession getSession() {
            return session;
        }

        MessageQueue getMessageQueue() {
            return messageQueue;
        }

        C2SSessionState getState() {
            return state;
        }

        void setEncryptedToken(byte[] encryptedToken) {
            this.encryptedToken = encryptedToken;
        }

        void setState(C2SSessionState state) {
            this.state = state;
        }

        byte[] getEncryptedToken() {
            return encryptedToken;
        }

        IntSequencer getSndTokenSequencer() {
            return sndTokenSequencer;
        }

        IntSequencer getVerifiedSequencer() {
            return verifiedSequencer;
        }
    }

}
