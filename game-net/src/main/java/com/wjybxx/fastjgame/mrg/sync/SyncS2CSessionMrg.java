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

package com.wjybxx.fastjgame.mrg.sync;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.misc.PortRange;
import com.wjybxx.fastjgame.mrg.*;
import com.wjybxx.fastjgame.net.common.*;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import com.wjybxx.fastjgame.net.sync.event.SyncConnectRequestEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncLogicRequestEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncPingEvent;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectResponseTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicResponseTO;
import com.wjybxx.fastjgame.trigger.Timer;
import com.wjybxx.fastjgame.utils.FastCollectionsUtils;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.BindException;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 同步rpc调用服务器到客户端会话管理器。
 * 对于同步请求，需要注意：收到某个客户端的请求时，该客户端的所有请求中，只有最大id请求是有效的。
 *
 * token相关说明请查看文档 关于token.txt
 * 众多相同理论请查看{@link com.wjybxx.fastjgame.mrg.async.S2CSessionMrg}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 11:56
 * @github - https://github.com/hl845740757
 */
public class SyncS2CSessionMrg {

    private static final Logger logger= LoggerFactory.getLogger(SyncS2CSessionMrg.class);

    private final SyncRequestDispatcherMrg syncRequestDispatcherMrg;
    private final WorldInfoMrg worldInfoMrg;
    private final SystemTimeMrg systemTimeMrg;
    private final TimerMrg timerMrg;
    private final NetConfigMrg netConfigMrg;
    private final TokenMrg tokenMrg;
    private final AcceptorMrg acceptorMrg;
    private final SyncNettyThreadMrg syncNettyThreadMrg;
    private final ForbiddenTokenHelper forbiddenTokenHelper;
    /**
     * 会话生命周期回调。
     * 这样设计是因为角色类型很多，而真正与你建立会话的角色类型很少。
     */
    private final EnumMap<RoleType,SessionLifecycleAware<SyncS2CSession>> lifecycleAwareMap=new EnumMap<>(RoleType.class);

    /**
     * 与客户端建立的会话信息
     */
    private final Long2ObjectMap<SessionWrapper> sessionWrapperMap =new Long2ObjectOpenHashMap<>();
    /**
     * 所有待处理的连接请求队列
     */
    private final ConcurrentLinkedQueue<SyncConnectRequestEvent> connectRequestQueue=new ConcurrentLinkedQueue<>();
    /**
     * 所有待处理的逻辑请求队列
     */
    private final ConcurrentLinkedQueue<SyncLogicRequestEvent> logicRequestQueue =new ConcurrentLinkedQueue<>();
    /**
     * 每个客户端的最新逻辑请求缓存
     */
    private final Long2ObjectMap<SyncLogicRequestEvent> latestLogicRequestCache =new Long2ObjectOpenHashMap<>();
    /**
     * ping包队列
     */
    private final ConcurrentLinkedQueue<SyncPingEvent> pingEventQueue =new ConcurrentLinkedQueue<>();

    @Inject
    public SyncS2CSessionMrg(SyncRequestDispatcherMrg syncRequestDispatcherMrg, WorldInfoMrg worldInfoMrg,
                             SystemTimeMrg systemTimeMrg, TimerMrg timerMrg, NetConfigMrg netConfigMrg, TokenMrg tokenMrg, AcceptorMrg acceptorMrg, SyncNettyThreadMrg syncNettyThreadMrg) {
        this.syncRequestDispatcherMrg = syncRequestDispatcherMrg;
        this.worldInfoMrg = worldInfoMrg;
        this.systemTimeMrg = systemTimeMrg;
        this.timerMrg = timerMrg;
        this.netConfigMrg = netConfigMrg;
        this.tokenMrg = tokenMrg;
        this.acceptorMrg = acceptorMrg;
        this.syncNettyThreadMrg = syncNettyThreadMrg;
        this.forbiddenTokenHelper=new ForbiddenTokenHelper(systemTimeMrg,timerMrg,netConfigMrg.tokenForbiddenTimeout());

        Timer checkSessionTimeoutTimer=Timer.newInfiniteTimer(netConfigMrg.syncRpcSessionTimeout()/3 * 1000,this::checkSessionTimeout);
        timerMrg.addTimer(checkSessionTimeoutTimer,systemTimeMrg.getSystemMillTime());
    }

    /**
     * 注册针对某种类型的会话生命周期回调。
     * 这样设计是因为角色类型很多，而真正与你建立会话的角色类型很少。
     * @param roleType 角色类型
     * @param lifecycleAware 会话通知
     */
    public void registerLifeCycleAware(@Nonnull RoleType roleType,@Nonnull SessionLifecycleAware<SyncS2CSession> lifecycleAware){
        if (lifecycleAwareMap.containsKey(roleType)){
            throw new IllegalArgumentException("duplicate roleType " + roleType);
        }
        lifecycleAwareMap.put(roleType,lifecycleAware);
    }

    private void checkSessionTimeout(Timer timer){
        FastCollectionsUtils.removeIfAndThen(sessionWrapperMap,
                (k, sessionWrapper) -> systemTimeMrg.getSystemSecTime() > sessionWrapper.getSessionTimeout(),
                (k, sessionWrapper) -> afterRemoved(sessionWrapper,"timeout"));
    }

    /**
     * @see AcceptorMrg#bind(NettyThreadMrg, boolean, int, ChannelInitializer)
     */
    public HostAndPort bind(boolean outer, int port, ChannelInitializer<SocketChannel> initializer) throws BindException {
        return acceptorMrg.bind(syncNettyThreadMrg,outer,port,initializer);
    }

    /**
     * @see AcceptorMrg#bindRange(NettyThreadMrg, boolean, PortRange, ChannelInitializer)
     */
    public HostAndPort bindRange(boolean outer, PortRange portRange, ChannelInitializer<SocketChannel> initializer) throws BindException {
        return acceptorMrg.bindRange(syncNettyThreadMrg,outer,portRange,initializer);
    }

    /**
     * 移除一个会话
     * @param clientGuid
     * @param reason 要是可扩展的，好像只有字符串最合适
     */
    public SyncS2CSession removeSession(long clientGuid,String reason){
        SessionWrapper sessionWrapper= sessionWrapperMap.remove(clientGuid);
        if (null==sessionWrapper){
            return null;
        }
        afterRemoved(sessionWrapper, reason);
        return sessionWrapper.getSession();
    }

    /**
     * 当session移除之后
     */
    private void afterRemoved(@Nonnull SessionWrapper sessionWrapper, String reason) {
        // 禁用该token及之前的token
        forbiddenTokenHelper.forbiddenCurToken(sessionWrapper.getToken());

        SyncS2CSession session=sessionWrapper.getSession();
        notifyClientExit(sessionWrapper.getChannel(),sessionWrapper);
        logger.info("remove session by reason of {}, session info={}.",reason, session);

        SessionLifecycleAware<SyncS2CSession> lifecycleAware = lifecycleAwareMap.get(session.getRoleType());
        if (null != lifecycleAware){
            try {
                lifecycleAware.onSessionDisconnected(session);
            }catch (Exception e){
                logger.warn("disconnect callback caught exception",e);
            }
        }
    }

    /**
     * 分发同步请求
     */
    public void dispatchSyncRpcRequest(){
        // 先处理连接请求，连接请求需要全部处理。先处理连接请求可以过滤掉旧连接上的事件
        handleConnectRequests();

        //  再处理逻辑业务请求
        handleLogicRequests();

        // 最后处理心跳包
        handlePingEvents();
    }

    /**
     * 处理连接事件
     */
    private void handleConnectRequests() {
        SyncConnectRequestEvent connectRequestEvent;
        while ((connectRequestEvent=connectRequestQueue.poll())!=null){
            try {
                handleConnectRequest(connectRequestEvent);
            }catch (Exception e){
                logger.warn("handleConnectRequest caught exception",e);
            }
        }
    }


    /**
     * 处理连接请求
     * @param connectRequestEvent 连接请求事件
     */
    private void handleConnectRequest(SyncConnectRequestEvent connectRequestEvent){
        SyncConnectRequestTO requestParam = connectRequestEvent.getConnectRequestTO();
        Channel channel=connectRequestEvent.getChannel();
        Token clientToken=tokenMrg.decryptToken(requestParam.getEncryptTokenBytes());

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
    private boolean isRequestMatchToken(SyncConnectRequestTO requestParam, @Nonnull Token token){
        // token不是用于该客户端的
        if (requestParam.getClientGuid() != token.getClientGuid()
                || requestParam.getServerGuid() != token.getServerGuid()){
            return false;
        }
        // token不是用于该服务器的
        if (token.getServerGuid() != worldInfoMrg.getProcessGuid()
                || token.getServerRoleType() != worldInfoMrg.getProcessType()){
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
    private boolean login(Channel channel, SyncConnectRequestTO requestParam, @Nonnull Token clientToken) {
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
        // 为何要删除旧会话？(前两个问题都是可以解决的，但是第三个问题不能不管)
        // 1.会话是有状态的，无法基于旧状态与新状态的客户端通信(requestGuid限制)
        // 2.旧的token需要被禁用
        // 3.必须进行通知，需要让逻辑层知道旧连接彻底断开了，可能有额外逻辑
        removeSession(requestParam.getClientGuid(),"reLogin");

        // 禁用验证成功的token之前的token(不能放到removeSession之前，会导致覆盖)
        forbiddenTokenHelper.forbiddenPreToken(clientToken);

        // 登录成功
        SyncS2CSession session=new SyncS2CSession(requestParam.getClientGuid(), clientToken.getClientRoleType());
        SessionWrapper sessionWrapper=new SessionWrapper(session);
        sessionWrapperMap.put(requestParam.getClientGuid(),sessionWrapper);

        // 分配新的token并进入等待状态
        Token nextToken= tokenMrg.newLoginSuccessToken(clientToken);
        sessionWrapper.changeToWaitState(channel, requestParam.getSndTokenTimes(), clientToken,nextToken,nextSessionTimeout());

        notifyTokenCheckSuccess(channel, requestParam,nextToken);
        logger.info("client login success, sessionInfo={}",session);

        // 连接建立回调(通知)
        SessionLifecycleAware<SyncS2CSession> lifecycleAware = lifecycleAwareMap.get(session.getRoleType());
        if (lifecycleAware != null){
            try {
                lifecycleAware.onSessionConnected(session);
            }catch (Exception e){
                logger.warn("sessionConnected callback caught exception",e);
            }
        }
        return true;
    }

    /**
     * 客户端尝试断线重连，token是服务器保存的两个token之一。
     * @param channel 产生事件的channel
     * @param requestParam 客户端发来的请求参数
     * @param clientToken 客户端携带的token信息，等于服务器使用的token(之一) (usingToken)
     */
    private boolean reconnect(Channel channel, SyncConnectRequestTO requestParam, @Nonnull Token clientToken) {
        SessionWrapper sessionWrapper = sessionWrapperMap.get(requestParam.getClientGuid());
        // 这是一个旧请求
        if (requestParam.getSndTokenTimes() <= sessionWrapper.getSndTokenTimes()){
            notifyTokenCheckFailed(channel, requestParam, FailReason.OLD_REQUEST);
            return false;
        }
        // 禁用验证成功的token之前的token
        forbiddenTokenHelper.forbiddenPreToken(clientToken);

        // 关闭旧channel
        if (sessionWrapper.getChannel()!=channel){
            NetUtils.closeQuietly(sessionWrapper.getChannel());
        }

        // 分配新的token并进入等待状态
        Token nextToken= tokenMrg.nextToken(clientToken);
        sessionWrapper.changeToWaitState(channel, requestParam.getSndTokenTimes(), clientToken, nextToken, nextSessionTimeout());

        notifyTokenCheckSuccess(channel, requestParam, nextToken);
        logger.info("client reconnect success, sessionInfo={}",sessionWrapper.getSession());
        return true;
    }

    /**
     * 通知客户端退出
     * @param channel 会话对应的的channel
     * @param sessionWrapper 会话信息
     */
    private void notifyClientExit(Channel channel, SessionWrapper sessionWrapper){
        long clientGuid = sessionWrapper.getSession().getClientGuid();
        Token failToken = tokenMrg.newFailToken(clientGuid, worldInfoMrg.getProcessGuid());
        notifyTokenCheckResult(channel,sessionWrapper.getSndTokenTimes(),false, failToken);
    }

    /**
     * 通知客户端token验证失败
     * 注意token校验失败，不能认定当前会话失效，可能是错误或非法的连接，因此不能对会话下手
     * @param requestTO 客户端的请求信息
     * @param failReason 失败原因，用于记录日志
     */
    private void notifyTokenCheckFailed(Channel channel, SyncConnectRequestTO requestTO, FailReason failReason){
        Token failToken = tokenMrg.newFailToken(requestTO.getClientGuid(), requestTO.getServerGuid());
        notifyTokenCheckResult(channel,requestTO.getSndTokenTimes(),false, failToken);
        logger.warn("client {} checkTokenResult failed by reason of {}",requestTO.getClientGuid(),failReason);
    }

    /**
     * 通知客户端token验证成功
     * @param requestTO 客户端的请求信息
     * @param nextToken 连接成功时新分配的token
     */
    private void notifyTokenCheckSuccess(Channel channel, SyncConnectRequestTO requestTO, Token nextToken){
        notifyTokenCheckResult(channel,requestTO.getSndTokenTimes(),true, nextToken);
    }

    /**
     * 通知客户端token验证结果
     * @param channel 发起请求验证token的channel
     * @param sndTokenTimes 这是客户端的第几次请求
     * @param success 是否成功
     * @param token 新的token
     */
    private void notifyTokenCheckResult(Channel channel, int sndTokenTimes, boolean success, Token token){
        byte[] encryptToken = tokenMrg.encryptToken(token);
        SyncConnectResponseTO connectResponse=new SyncConnectResponseTO(sndTokenTimes,success,encryptToken);
        ChannelFuture future = channel.writeAndFlush(connectResponse);
        // token验证失败情况下，发送之后，关闭channel
        if (!success){
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 处理客户端同步调用
     */
    private void handleLogicRequests() {
        // 业务逻辑只需处理该连接上的最新请求，旧请求表示客户端已经放弃了，因此不必执行
        findLatestLogicRequest();
        if (latestLogicRequestCache.size()==0){
            return;
        }
        try {
            for (SyncLogicRequestEvent logicRequestEvent: latestLogicRequestCache.values()){
                try {
                    handleLogicRequest(logicRequestEvent);
                }catch (Exception e){
                    logger.error("handle syncRpcLogicRequest caught exception",e);
                }
            }
        }finally {
            latestLogicRequestCache.clear();
        }
    }
    /**
     * 收集最新的逻辑请求
     */
    private void findLatestLogicRequest(){
        SyncLogicRequestEvent logicRequestEvent;
        while ((logicRequestEvent= logicRequestQueue.poll())!=null){
            long clientGuid = logicRequestEvent.getClientGuid();
            SessionWrapper sessionWrapper= sessionWrapperMap.get(clientGuid);
            // 不存在该请求对应的session
            if (null == sessionWrapper){
                NetUtils.closeQuietly(logicRequestEvent.getChannel());
                continue;
            }
            // 不是session当前channel上的事件
            if (logicRequestEvent.getChannel() != sessionWrapper.getChannel()){
                NetUtils.closeQuietly(logicRequestEvent.getChannel());
                continue;
            }
            SyncLogicRequestEvent existElement = latestLogicRequestCache.get(clientGuid);
            // 如果是更加新的请求就处理它
            if (null == existElement || logicRequestEvent.getRequestGuid() > existElement.getRequestGuid()){
                latestLogicRequestCache.put(clientGuid,logicRequestEvent);
            }
        }
    }

    /**
     * 处理逻辑请求
     * @param logicRequestEvent 逻辑请求事件，已过滤，这里一定当前会话上的事件
     */
    private void handleLogicRequest(SyncLogicRequestEvent logicRequestEvent){
        SessionWrapper sessionWrapper= sessionWrapperMap.get(logicRequestEvent.getClientGuid());
        // 在新channel收到客户端的消息时 => 客户端一定收到了token验证结果
        // 确定客户端已收到了新的token,更新channel为已激活状态，并添加禁用的token
        if (sessionWrapper.getPreToken()!=null){
            sessionWrapper.changeToActiveState();
            forbiddenTokenHelper.forbiddenPreToken(sessionWrapper.getToken());
        }
        // 更新session超时时间
        sessionWrapper.setSessionTimeout(nextSessionTimeout());

        long requestGuid = logicRequestEvent.getRequestGuid();
        // 无效的请求id
        if (requestGuid <= NetConstants.INIT_REQUEST_GUID){
            onLogicRequestInvalid(logicRequestEvent.getChannel(),requestGuid);
            return;
        }
        SyncLogicResponseTO lastResponseTO=sessionWrapper.getLastResponseTO();
        // 确认失效的请求
        if (null != lastResponseTO && requestGuid < lastResponseTO.getRequestGuid()){
            onLogicRequestInvalid(logicRequestEvent.getChannel(),requestGuid);
            return;
        }
        // 重试请求(保证幂等性，总是返回第一次的结果)
        if (null != lastResponseTO && requestGuid == lastResponseTO.getRequestGuid()){
            logicRequestEvent.getChannel().writeAndFlush(lastResponseTO);
            return;
        }
        // 新请求
        Object response = syncRequestDispatcherMrg.handleRequest(sessionWrapper.getSession(),logicRequestEvent.getRequest());
        SyncLogicResponseTO logicResponseTO=new SyncLogicResponseTO(requestGuid,response);
        // 存下结果，以支持客户端重试
        sessionWrapper.setLastResponseTO(logicResponseTO);
        logicRequestEvent.getChannel().writeAndFlush(logicResponseTO);
    }

    /**
     * 当客户端请求无效时
     * @param channel 收到请求的channel
     * @param requestGuid 发起的请求id
     */
    private void onLogicRequestInvalid(Channel channel, long requestGuid){
        SyncLogicResponseTO logicResponseTO=new SyncLogicResponseTO(requestGuid,null);
        channel.writeAndFlush(logicResponseTO);
    }

    /**
     * 处理所有ping包事件
     */
    private void handlePingEvents() {
        SyncPingEvent syncPingEvent;
        while ((syncPingEvent=pingEventQueue.poll())!=null){
            handlePingEvent(syncPingEvent);
        }
    }

    /**
     * 处理ping包事件
     * @param syncPingEvent ping包事件
     */
    private void handlePingEvent(SyncPingEvent syncPingEvent) {
        long clientGuid=syncPingEvent.getClientGuid();
        SessionWrapper sessionWrapper= sessionWrapperMap.get(clientGuid);
        // 不存在该请求对应的session
        if (null == sessionWrapper){
            NetUtils.closeQuietly(syncPingEvent.getChannel());
            return;
        }
        // 不是session当前channel上的事件
        if (syncPingEvent.getChannel() != sessionWrapper.getChannel()){
            NetUtils.closeQuietly(syncPingEvent.getChannel());
            return;
        }
        // 由于ping包是IO线程自动发的，因此无法推断客户端收到了最新的token，只能更新它的超时时间
        sessionWrapper.setSessionTimeout(nextSessionTimeout());
    }


    /**
     * 获取session的下一个超时时间
     */
    private int nextSessionTimeout(){
        return systemTimeMrg.getSystemSecTime() + netConfigMrg.syncRpcSessionTimeout();
    }

    /**
     * 当收到客户端的连接请求事件
     * @param syncConnectRequestEvent 连接请求事件
     */
    public void onRcvConnectRequest(SyncConnectRequestEvent syncConnectRequestEvent) {
        connectRequestQueue.offer(syncConnectRequestEvent);
    }

    /**
     * 当收到客户的逻辑请求事件
     * @param syncLogicRequestEvent 逻辑请求事件
     */
    public void onRcvLogicRequest(SyncLogicRequestEvent syncLogicRequestEvent) {
        logicRequestQueue.offer(syncLogicRequestEvent);
    }

    /**
     * 当收到ping包的时候
     * @param pingEvent
     */
    public void onRcvPingPkg(SyncPingEvent pingEvent) {
        pingEventQueue.offer(pingEvent);
    }

    /**
     * session包装对象，不对外暴露细节
     */
    private static class SessionWrapper{
        /**
         * 原始会话
         */
        private final SyncS2CSession session;
        /**
         * 客户端连接
         */
        private Channel channel;

        /**
         * 这是客户端第几次发起连接请求，用于服务器识别最新请求和客户端追踪结果
         */
        private int sndTokenTimes;
        /**
         * 会话新分配的token。
         * 在切换到activeState之前还不能确定客户端收到了。
         * 在切换到activeState之后能确定客户端一定收到了。
         */
        private Token token;
        /**
         * 重连成功时客户端传来的token
         */
        private Token preToken;
        /**
         * 上一次请求的返回结果(为了保持幂等性，始终返回第一次的处理结果)，此外肩负识别最新逻辑请求的责任
         */
        private SyncLogicResponseTO lastResponseTO;
        /**
         * 会话超时时间(秒)
         */
        private int sessionTimeout;

        private SessionWrapper(SyncS2CSession session) {
            this.session = session;
        }

        SyncS2CSession getSession() {
            return session;
        }

        Channel getChannel() {
            return channel;
        }

        SyncLogicResponseTO getLastResponseTO() {
            return lastResponseTO;
        }

        void setLastResponseTO(SyncLogicResponseTO lastResponseTO) {
            this.lastResponseTO = lastResponseTO;
        }

        int getSndTokenTimes() {
            return sndTokenTimes;
        }

        Token getToken() {
            return token;
        }

        Token getPreToken() {
            return preToken;
        }

        /**
         * 重连成功
         * @param channel 连接成功时的channel
         * @param sndTokenTimes 这是客户端的第几次请求
         */
        void changeToWaitState(Channel channel, int sndTokenTimes, Token token, Token preToken,int sessionTimeout){
            this.channel=channel;
            this.sndTokenTimes=sndTokenTimes;
            this.token=token;
            this.preToken=preToken;
            this.sessionTimeout=sessionTimeout;
        }

        /**
         * 切换到激活状态
         */
        void changeToActiveState(){
            this.preToken=null;
        }

        int getSessionTimeout() {
            return sessionTimeout;
        }

        void setSessionTimeout(int sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
        }
    }
}
