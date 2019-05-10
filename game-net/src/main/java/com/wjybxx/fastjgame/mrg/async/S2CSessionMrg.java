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
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.net.async.*;
import com.wjybxx.fastjgame.net.async.event.AckPingPongEventParam;
import com.wjybxx.fastjgame.net.async.event.LogicMessageEventParam;
import com.wjybxx.fastjgame.net.async.event.MessageEventParam;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectResponseTO;
import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;
import com.wjybxx.fastjgame.net.common.FailReason;
import com.wjybxx.fastjgame.net.common.ForbiddenTokenHelper;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.common.Token;
import com.wjybxx.fastjgame.trigger.Timer;
import com.wjybxx.fastjgame.utils.FastCollectionsUtils;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * 服务器到客户端会话管理器。
 * (我接收到的连接)
 *
 * token相关说明请查看文档 关于token.txt
 *
 * 注意：请求登录、重连时，验证失败不能对当前session做任何操作，因为不能证明表示当前session有异常，
 * 只有连接成功时才能操作session。
 * 同理，也不能更新{@link #forbiddenTokenHelper}。
 *
 * 换句话说：有新的channel请求建立连接，不能代表旧的channel和会话有异常，有可能是新的channel是非法的。
 *
 * 什么时候应该删除session？
 * 1.主动调用{@link #removeSession(long, String)}
 * 2.会话超时
 * 3.缓存过多
 * 4.客户端重新登录
 *
 * 什么时候会关闭channel？
 *  {@link #removeSession(long, String)} 或者说 {@link #notifyClientExit(Channel, SessionWrapper)}
 * {@link #notifyTokenCheckFailed(Channel, ConnectRequestTO, FailReason)}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:14
 * @github - https://github.com/hl845740757
 */
public class S2CSessionMrg {

    private static final Logger logger= LoggerFactory.getLogger(S2CSessionMrg.class);

    private final WorldInfoMrg worldInfoMrg;
    private final SystemTimeMrg systemTimeMrg;
    private final NetConfigMrg netConfigMrg;
    private final TokenMrg tokenMrg;
    private final MessageDispatcherMrg dispatcherMrg;
    private final ForbiddenTokenHelper forbiddenTokenHelper;

    /**
     * 客户端guid到会话的映射
     */
    private final Long2ObjectMap<SessionWrapper> sessionWrapperMap =new Long2ObjectOpenHashMap<>();
    /**
     * 会话生命周期handler
     * 作为服务器，认为所有客户端都是一样的
     */
    private SessionLifecycleAware<S2CSession> sessionLifecycleAware;

    @Inject
    public S2CSessionMrg(SystemTimeMrg systemTimeMrg, NetConfigMrg netConfigMrg, TimerMrg timerMrg, WorldInfoMrg worldInfoMrg,
                         TokenMrg tokenMrg, MessageDispatcherMrg dispatcherMrg) {
        this.systemTimeMrg = systemTimeMrg;
        this.netConfigMrg = netConfigMrg;
        this.worldInfoMrg = worldInfoMrg;
        this.tokenMrg = tokenMrg;
        this.dispatcherMrg = dispatcherMrg;
        this.forbiddenTokenHelper=new ForbiddenTokenHelper(systemTimeMrg,timerMrg,netConfigMrg.tokenForbiddenTimeout());

        // 定时检查会话超时的timer(1/3个周期检测一次)
        Timer checkTimeOutTimer = Timer.newInfiniteTimer(netConfigMrg.sessionTimeout()/3 * 1000,this::checkSessionTimeout);
        timerMrg.addTimer(checkTimeOutTimer,systemTimeMrg.getSystemMillTime());
    }

    public void setSessionLifecycleAware(SessionLifecycleAware<S2CSession> sessionLifecycleAware) {
        this.sessionLifecycleAware = sessionLifecycleAware;
    }

    /**
     * 定时检查会话超时时间
     */
    private void checkSessionTimeout(Timer timer){
        FastCollectionsUtils.removeIfAndThen(sessionWrapperMap,
                (k, sessionWrapper) -> systemTimeMrg.getSystemSecTime() > sessionWrapper.getSessionTimeout(),
                (k, sessionWrapper) -> afterRemoved(sessionWrapper,"session time out!"));
    }

    /**
     * 向客户端发送一条消息
     * @param clientGuid
     * @param message
     */
    public void send(long clientGuid,@Nonnull Object message){
        SessionWrapper sessionWrapper = sessionWrapperMap.get(clientGuid);
        if (null== sessionWrapper){
            logger.warn("client {} is removed, but try send message.",clientGuid);
            return;
        }

        if (sessionWrapper.getCacheMessageNum() >= netConfigMrg.serverMaxCacheNum()){
            removeSession(clientGuid,"cacheMessageNum is too much! cacheMessageNum="+sessionWrapper.getCacheMessageNum());
        }else {
            LogicMessage logicMessage=new LogicMessage(sessionWrapper.getMessageQueue().nextSequence(),message);
            sessionWrapper.writeAndFlush(logicMessage);
        }
    }

    /**
     * 请求移除一个会话
     * @param clientGuid sessionId
     * @param reason 要是可扩展的，好像只有字符串最合适
     */
    public S2CSession removeSession(long clientGuid,String reason){
        SessionWrapper sessionWrapper = sessionWrapperMap.remove(clientGuid);
        if (null == sessionWrapper){
            return null;
        }
        afterRemoved(sessionWrapper, reason);
        return sessionWrapper.getSession();
    }

    /**
     * 会话删除之后
     */
    private void afterRemoved(SessionWrapper sessionWrapper, String reason) {
        // 禁用该token及之前的token
        forbiddenTokenHelper.forbiddenCurToken(sessionWrapper.getToken());

        S2CSession session=sessionWrapper.getSession();
        notifyClientExit(sessionWrapper.getChannel(),sessionWrapper);
        logger.info("remove session by reason of {}, session info={}.",reason, session);

        // 回调通知
        if (null!=sessionLifecycleAware){
            try {
                sessionLifecycleAware.onSessionDisconnected(session);
            }catch (Exception e){
                logger.warn("disconnect callback caught exception",e);
            }
        }
    }

    /**
     * 收到客户端的链接请求(请求验证token)
     * @param channel
     * @param requestParam
     */
    public void onRcvConnectRequest(Channel channel, ConnectRequestTO requestParam){
        Token clientToken=tokenMrg.decryptToken(requestParam.getTokenBytes());
        // 客户端token不合法(解析错误)
        if (null==clientToken){
            notifyTokenCheckFailed(channel, requestParam, FailReason.NULL);
            return;
        }
        // 无效token
        if (tokenMrg.isFailToken(clientToken)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.INVALID);
            return;
        }
        // token与请求不匹配
        if (!isRequestMatchToken(requestParam,clientToken)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.TOKEN_NOT_MATCH_REQUEST);
            return;
        }
        // 被禁用的旧token
        if (forbiddenTokenHelper.isForbiddenToken(clientToken)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.OLD_REQUEST);
            return;
        }
        // 为什么不能在这里更新forbiddenToken? 因为走到这里还不能证明是一个合法的客户端，不能影响合法客户端的数据。
        SessionWrapper sessionWrapper = sessionWrapperMap.get(requestParam.getClientGuid());
        // 不论如何必须是新的channel
        if (isSameChannel(sessionWrapper, channel)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.SAME_CHANNEL);
            return;
        }
        // 新的token
        if (isNewerToken(sessionWrapper,clientToken)){
            login(channel,requestParam,clientToken);
            return;
        }
        // 当前使用的token
        if (isUsingToken(sessionWrapper,clientToken)){
            reconnect(channel,requestParam,clientToken);
            return;
        }
        // 其它(介于使用的两个token之间的token)
        notifyTokenCheckFailed(channel,requestParam,FailReason.OLD_REQUEST);
    }

    /**
     * 是否是同一个channel
     */
    private boolean isSameChannel(SessionWrapper sessionWrapper, Channel channel) {
        return null!=sessionWrapper && sessionWrapper.getChannel()==channel;
    }

    /**
     * 是否是更新的token
     */
    private boolean isNewerToken(@Nullable SessionWrapper sessionWrapper, Token clientToken) {
        return null == sessionWrapper || clientToken.getCreateSecTime() > sessionWrapper.getToken().getCreateSecTime();
    }

    /**
     * 是否是当前会话使用的token
     */
    private boolean isUsingToken(@Nullable SessionWrapper sessionWrapper, Token clientToken) {
        if (null == sessionWrapper){
            return false;
        }
        // 前一个token为null ==> 客户端一定收到了新的token
        if (sessionWrapper.getPreToken()==null){
            return tokenMrg.isSameToken(sessionWrapper.getToken(),clientToken);
        }
        // 可能收到了，也可能没收到最新的token，但只能是两者之一
        return tokenMrg.isSameToken(sessionWrapper.getToken(),clientToken)
                || tokenMrg.isSameToken(sessionWrapper.getPreToken(),clientToken);
    }

    /**
     * 请求与是token否匹配。校验token基本信息,校验token是否被修改过
     * @param requestParam 客户端请求参数
     * @param token 客户端携带的token信息
     */
    private boolean isRequestMatchToken(ConnectRequestTO requestParam, @Nonnull Token token){
        // token不是用于该客户端的
        if (requestParam.getClientGuid() != token.getClientGuid()
                || requestParam.getServerGuid() != token.getServerGuid()){
            return false;
        }
        // token不是用于该服务器的
        if (token.getServerGuid() != worldInfoMrg.getWorldGuid()
                || token.getServerRoleType() != worldInfoMrg.getWorldType()){
            return false;
        }
        return true;
    }

    /**
     * 使用更加新的token请求登录
     * @param channel 产生事件的channel
     * @param requestParam 客户端发来的请求参数
     * @param clientToken 客户端携带的token信息 更加新的token，newerToken
     */
    private boolean login(Channel channel, ConnectRequestTO requestParam, @Nonnull Token clientToken) {
        // 不是登录token
        if (!tokenMrg.isLoginToken(clientToken)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.NOT_LOGIN_TOKEN);
            return false;
        }
        // 登录token超时了
        if (tokenMrg.isLoginTokenTimeout(clientToken)){
            notifyTokenCheckFailed(channel, requestParam, FailReason.TOKEN_TIMEOUT);
            return false;
        }
        // 客户端已收到消息数必须为0
        if (requestParam.getAck() != MessageQueue.INIT_ACK){
            notifyTokenCheckFailed(channel, requestParam, FailReason.ACK);
            return false;
        }

        // 为何要删除旧会话？
        // 1.会话是有状态的，无法基于旧状态与新状态的客户端通信(ack,sequence)
        // 2.旧的token需要被禁用
        removeSession(requestParam.getClientGuid(),"reLogin");

        // 禁用验证成功的token之前的token(不能放到removeSession之前，会导致覆盖)
        forbiddenTokenHelper.forbiddenPreToken(clientToken);

        // 登录成功
        S2CSession session=new S2CSession(requestParam.getClientGuid(), clientToken.getClientRoleType());
        SessionWrapper sessionWrapper=new SessionWrapper(session);
        sessionWrapperMap.put(requestParam.getClientGuid(),sessionWrapper);

        // 分配新的token并进入等待状态
        Token nextToken= tokenMrg.newLoginSuccessToken(clientToken);
        sessionWrapper.changeToWaitState(channel, requestParam.getSndTokenTimes(), clientToken, nextToken, nextSessionTimeout());

        notifyTokenCheckSuccess(channel, requestParam, MessageQueue.INIT_ACK,nextToken);
        logger.info("client login success, sessionInfo={}",session);

        // 连接建立回调(通知)
        if (sessionLifecycleAware != null){
            try {
                sessionLifecycleAware.onSessionConnected(session);
            }catch (Exception e){
                logger.warn("sessionConnected callback caught exception",e);
            }
        }
        return true;
    }

    private int nextSessionTimeout() {
        return systemTimeMrg.getSystemSecTime()+ netConfigMrg.sessionTimeout();
    }

    /**
     * 客户端尝试断线重连，token是服务器保存的两个token之一。
     * @param channel 产生事件的channel
     * @param requestParam 客户端发来的请求参数
     * @param clientToken 客户端携带的token信息，等于服务器使用的token (usingToken)
     */
    private boolean reconnect(Channel channel, ConnectRequestTO requestParam, @Nonnull Token clientToken) {
        SessionWrapper sessionWrapper = sessionWrapperMap.get(requestParam.getClientGuid());
        // 这是一个旧请求
        if (requestParam.getSndTokenTimes() <= sessionWrapper.getSndTokenTimes()){
            notifyTokenCheckFailed(channel, requestParam, FailReason.OLD_REQUEST);
            return false;
        }
        // 判断客户端ack合法性
        MessageQueue messageQueue = sessionWrapper.getMessageQueue();
        if (!messageQueue.isAckOK(requestParam.getAck())){
            notifyTokenCheckFailed(channel, requestParam, FailReason.ACK);
            return false;
        }
        // ---- 这里验证成功 ack 和 token都验证通过
        // 禁用验证成功的token之前的token
        forbiddenTokenHelper.forbiddenPreToken(clientToken);

        // 关闭旧channel
        if (sessionWrapper.getChannel()!=channel){
            NetUtils.closeQuietly(sessionWrapper.getChannel());
        }

        // 更新消息队列
        messageQueue.updateSentQueue(requestParam.getAck());

        // 分配新的token并进入等待状态
        Token nextToken= tokenMrg.nextToken(clientToken);
        sessionWrapper.changeToWaitState(channel, requestParam.getSndTokenTimes(), clientToken, nextToken, nextSessionTimeout());

        notifyTokenCheckSuccess(channel, requestParam, messageQueue.getAck(), nextToken);
        logger.info("client reconnect success, sessionInfo={}",sessionWrapper.getSession());

        // 重发已发送未确认的消息
        if (messageQueue.getSentQueue().size()>0){
            for (Message message:messageQueue.getSentQueue()){
                sessionWrapper.getChannel().write(message.build(messageQueue.getAck()));
            }
            sessionWrapper.getChannel().flush();
        }
        return true;
    }

    /**
     * 通知客户端退出
     * @param channel 会话对应的的channel
     * @param sessionWrapper 会话信息
     */
    private void notifyClientExit(Channel channel, SessionWrapper sessionWrapper){
        long clientGuid = sessionWrapper.getSession().getClientGuid();
        Token failToken = tokenMrg.newFailToken(clientGuid, worldInfoMrg.getWorldGuid());
        notifyTokenCheckResult(channel,sessionWrapper.getSndTokenTimes(),false, -1, failToken);
    }

    /**
     * 通知客户端token验证失败
     * 注意token校验失败，不能认定当前会话失效，可能是错误或非法的连接，因此不能对会话下手
     * @param requestTO 客户端的请求信息
     * @param failReason 失败原因，用于记录日志
     */
    private void notifyTokenCheckFailed(Channel channel, ConnectRequestTO requestTO, FailReason failReason){
        Token failToken = tokenMrg.newFailToken(requestTO.getClientGuid(), requestTO.getServerGuid());
        notifyTokenCheckResult(channel,requestTO.getSndTokenTimes(),false, -1, failToken);
        logger.warn("client {} checkTokenResult failed by reason of {}",requestTO.getClientGuid(),failReason);
    }

    /**
     * 通知客户端token验证成功
     * @param requestTO 客户端的请求信息
     * @param ack 服务器的捎带确认ack
     * @param nextToken 连接成功时新分配的token
     */
    private void notifyTokenCheckSuccess(Channel channel,ConnectRequestTO requestTO, long ack, Token nextToken){
        notifyTokenCheckResult(channel,requestTO.getSndTokenTimes(),true, ack,nextToken);
    }

    /**
     * 通知客户端token验证结果
     * @param channel 发起请求验证token的channel
     * @param sndTokenTimes 这是客户端的第几次请求
     * @param success 是否成功
     * @param ack 服务器的ack
     * @param token 新的token
     */
    private void notifyTokenCheckResult(Channel channel, int sndTokenTimes, boolean success, long ack, Token token){
        byte[] encryptToken = tokenMrg.encryptToken(token);
        ConnectResponseTO connectResponse=new ConnectResponseTO(sndTokenTimes,success, ack,encryptToken);
        ChannelFuture future = channel.writeAndFlush(connectResponse);
        // token验证失败情况下，发送之后，关闭channel
        if (!success){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 收到客户端的定时Ack-ping包
     * @param eventChannel 产生事件的channel
     * @param ackPingParam 心跳包参数
     */
    public void onRcvClientAckPing(Channel eventChannel, AckPingPongEventParam ackPingParam){
        tryUpdateMessageQueue(eventChannel,ackPingParam,sessionWrapper -> {
            MessageQueue messageQueue = sessionWrapper.getMessageQueue();
            sessionWrapper.writeAndFlush(new AckPingPongMessage(messageQueue.getAck()));
        });
    }

    /**
     * 尝试用message更新消息队列
     * @param eventChannel 产生事件的channel
     * @param eventParam 消息参数
     * @param then 当且仅当message是当前channel上期望的下一个消息，且ack合法时执行。
     */
    private <T extends MessageEventParam> void tryUpdateMessageQueue(Channel eventChannel, T eventParam, Consumer<SessionWrapper> then){
        SessionWrapper sessionWrapper = sessionWrapperMap.get(eventParam.sessionGuid());
        if (null==sessionWrapper){
            NetUtils.closeQuietly(eventChannel);
            return;
        }
        // 必须是相同的channel (isEventChannelOk)
        if (eventChannel!=sessionWrapper.getChannel()){
            NetUtils.closeQuietly(eventChannel);
            return;
        }
        // 在新channel收到客户端的消息时 => 客户端一定收到了token验证结果
        // 确定客户端已收到了新的token,更新channel为已激活状态，并添加禁用的token
        if (sessionWrapper.getPreToken() != null){
            sessionWrapper.changeToActiveState();
            forbiddenTokenHelper.forbiddenPreToken(sessionWrapper.getToken());
        }
        // 更新session超时时间
        sessionWrapper.setSessionTimeout(nextSessionTimeout());

        MessageTO message=eventParam.messageTO();
        MessageQueue messageQueue=sessionWrapper.getMessageQueue();
        // 不是期望的下一个消息
        if (message.getSequence()!=messageQueue.getAck()+1){
            return;
        }
        // 客户端发来的ack错误
        if (!messageQueue.isAckOK(message.getAck())){
            return;
        }
        // 更新消息队列
        messageQueue.setAck(message.getSequence());
        messageQueue.updateSentQueue(message.getAck());

        // 然后执行自己的逻辑
        then.accept(sessionWrapper);
    }

    /**
     * 当接收到客户端发送的消息时
     */
    public void onRcvClientLogicMsg(Channel eventChannel, LogicMessageEventParam logicMessageParam){
        tryUpdateMessageQueue(eventChannel,logicMessageParam,sessionWrapper -> {
            dispatcherMrg.handleClientMessage(sessionWrapper.getSession(),logicMessageParam.messageTO().getMessage());
        });
    }

    /**
     * S2CSession的包装类，不对外暴露细节
     */
    private static final class SessionWrapper {
        /**
         * 注册的会话信息
         */
        private final S2CSession session;
        /**
         * 会话的消息队列
         */
        private final MessageQueue messageQueue=new MessageQueue();
        /**
         * 会话channel一定不为null
         */
        private Channel channel;
        /**
         * 会话当前token，一定不为null。
         * (如果preToken不为null,则客户端可能还未收到该token)
         */
        private Token token;
        /**
         * 会话过期时间(秒)(时间到则需要移除)
         */
        private int sessionTimeout;
        /**
         * 这是客户端第几次发送token验证
         */
        private int sndTokenTimes;
        /**
         * 在确认客户端收到新的token之前会保留
         */
        private Token preToken=null;

        public SessionWrapper(S2CSession session) {
            this.session = session;
        }

        public S2CSession getSession() {
            return session;
        }

        public Channel getChannel() {
            return channel;
        }

        public Token getToken() {
            return token;
        }

        public int getSessionTimeout() {
            return sessionTimeout;
        }

        public int getSndTokenTimes() {
            return sndTokenTimes;
        }

        public MessageQueue getMessageQueue() {
            return messageQueue;
        }

        public Token getPreToken() {
            return preToken;
        }

        /**
         * 切换到等待状态，即确认客户端收到新的token之前，新的token还不能生效
         * (等待客户端真正的产生消息,也就是收到了新的token)
         * @param channel 新的channel
         * @param sndTokenTimes 这是对客户端第几次发送token验证
         * @param preToken 上一个token
         * @param nextToken 新的token
         * @param sessionTimeout 会话超时时间
         */
        public void changeToWaitState(Channel channel, int sndTokenTimes, Token preToken, Token nextToken, int sessionTimeout){
            this.channel=channel;
            this.token=nextToken;
            this.sessionTimeout =sessionTimeout;
            this.preToken=preToken;
            this.sndTokenTimes=sndTokenTimes;
        }

        /**
         * 切换到激活状态，确定客户端已收到了新的token
         * (收到客户端用当前channel发过来的消息)
         */
        public void changeToActiveState(){
            this.preToken=null;
        }

        public void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }

        /**
         * 立即发送一个消息
         * @param message
         */
        public void writeAndFlush(Message message){
            // 服务器不需要设置它的超时时间，只需要设置捎带确认的ack
            messageQueue.getSentQueue().addLast(message);
            // 发送
            channel.writeAndFlush(message.build(messageQueue.getAck()));
        }

        /**
         * 获取当前缓存的消息数
         * 缓存过多可能需要关闭会话
         * @return
         */
        public int getCacheMessageNum(){
            return messageQueue.getCacheMessageNum();
        }
    }

}
