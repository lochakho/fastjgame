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
import com.wjybxx.fastjgame.misc.IntSequencer;
import com.wjybxx.fastjgame.misc.LongSequencer;
import com.wjybxx.fastjgame.mrg.NetConfigMrg;
import com.wjybxx.fastjgame.mrg.TokenMrg;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.common.Token;
import com.wjybxx.fastjgame.net.sync.SyncC2SSession;
import com.wjybxx.fastjgame.net.sync.event.SyncConnectResponseEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncLogicResponseEvent;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectResponseTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicResponseTO;
import com.wjybxx.fastjgame.utils.CollectionUtils;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 同步RPC调用客户端到服务器的会话管理器。
 * 主要用于发起同步RPC调用。
 *
 * 什么时候与远程进行连接？
 * 1.第一次注册的时候
 * 2.发起请求的时候，如果发现channel已关闭(断开连接，则重连)
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 12:01
 * @github - https://github.com/hl845740757
 */
public class SyncC2SSessionMrg {

    private static final Logger logger= LoggerFactory.getLogger(SyncC2SSessionMrg.class);

    private final WorldInfoMrg worldInfoMrg;
    private final SyncNetServiceMrg syncNetServiceMrg;
    private final NetConfigMrg netConfigMrg;
    private final TokenMrg tokenMrg;
    /**
     * 所有的服务器信息
     */
    private final Long2ObjectMap<SessionWrapper> sessionMap =new Long2ObjectOpenHashMap<>();
    /**
     * 建立链接的响应队列
     */
    private final LinkedBlockingQueue<SyncConnectResponseEvent> connectResponseQueue =new LinkedBlockingQueue<>();
    /**
     * 逻辑消息的响应队列
     */
    private final LinkedBlockingQueue<SyncLogicResponseEvent> logicResponseQueue =new LinkedBlockingQueue<>();

    @Inject
    public SyncC2SSessionMrg(WorldInfoMrg worldInfoMrg, SyncNetServiceMrg syncNetServiceMrg,
                             NetConfigMrg netConfigMrg, TokenMrg tokenMrg) {
        this.worldInfoMrg = worldInfoMrg;
        this.syncNetServiceMrg = syncNetServiceMrg;
        this.netConfigMrg = netConfigMrg;
        this.tokenMrg = tokenMrg;
    }

    /**
     * 获取某个服务器的session
     * @param serverGuid 服务器guid
     * @return 如果不存在则返回null
     */
    @Nullable
    public SyncC2SSession getSession(long serverGuid){
        SessionWrapper wrapper = sessionMap.get(serverGuid);
        return null==wrapper?null:wrapper.getSession();
    }

    /**
     * 注册一个rpc同步调用服务器。必须先注册才能发起请求。
     * @param serverGuid 服务器标识
     * @param roleType 服务器角色类型(用于验证)
     * @param host 服务器地址
     * @param port 服务器端口
     * @param initializerSupplier initializer提供器，如果线程安全，可以总是返回同一个initializer。
     * @return 由注册信息创建的session
     */
    @Nullable
    public SyncC2SSession registerServer(final long serverGuid, RoleType roleType, String host, int port,
                               Supplier<ChannelInitializer<SocketChannel>> initializerSupplier,
                               SessionLifecycleAware<SyncC2SSession> lifeCycleAware) {
        if (sessionMap.containsKey(serverGuid)){
            throw new IllegalArgumentException("server " +serverGuid + " is already registered");
        }
        // 创建登录用的token(同步调用是用于服务器之间的，因此不需要外部传入)
        Token loginToken=tokenMrg.newLoginToken(worldInfoMrg.getWorldGuid(),worldInfoMrg.getWorldType(), serverGuid,roleType);
        byte[] tokenBytes=tokenMrg.encryptToken(loginToken);

        // 创建会话信息
        SyncC2SSession session=new SyncC2SSession(serverGuid, roleType, host,port,initializerSupplier, lifeCycleAware);
        SessionWrapper sessionWrapper=new SessionWrapper(session,tokenBytes);
        sessionMap.put(serverGuid,sessionWrapper);

        // 执行连接请求
        connect(sessionWrapper);
        return session;
    }

    /**
     * 执行真正的连接请求
     */
    private void connect(SessionWrapper sessionWrapper) {
        SyncC2SSession session=sessionWrapper.getSession();

        long serverGuid=session.getServerGuid();
        String host=session.getHost();
        int port=session.getPort();
        ChannelInitializer<SocketChannel> initializer=session.getInitializerSupplier().get();

        final Channel channel = syncNetServiceMrg.connectSyn(host, port, initializer);
        sessionWrapper.setChannel(channel);

        if (!channel.isActive()){
            NetUtils.closeQuietly(channel);
            logger.warn("can't connect remote {}:{}",host,port);
            return;
        }

        // 发送建立连接请求
        final int sndTokenTimes = sessionWrapper.getSndTokenSequencer().incAndGet();
        channel.writeAndFlush(new SyncConnectRequestTO(worldInfoMrg.getWorldGuid(), serverGuid,
                sndTokenTimes,sessionWrapper.getTokenBytes()));

        // 清除无效结果集
        connectResponseQueue.clear();

        long startTime=System.currentTimeMillis();
        // 等待连接响应
        SyncConnectResponseEvent connectResponseEvent = CollectionUtils.waitElementWithPoll(connectResponseQueue,
                e -> isExpectConnectResponse(e, channel, serverGuid, sndTokenTimes),
                netConfigMrg.syncRpcConnectTimeout());

        // 超时
        if (null == connectResponseEvent){
            logger.warn("connect {}:{} timeout",host,port);
            NetUtils.closeQuietly(channel);
            return;
        }

        // 禁止连接(token验证失败)
        SyncConnectResponseTO response = connectResponseEvent.getConnectResponseTO();
        if (!response.isSuccess()){
            logger.warn("connect {}:{} was refused",host,port);
            removeSession(serverGuid,"connect refused");
            return;
        }

        // 建立连接成功
        sessionWrapper.setTokenBytes(response.getEncryptTokenBytes());
        int verifiedTimes = sessionWrapper.getVerifiedSequencer().incAndGet();

        long costTime = System.currentTimeMillis() - startTime;
        if (verifiedTimes == 1 && null != session.getLifeCycleAware()){
            logger.info("syncRpc first connect success, cost {} milltime, sessionInfo={}", costTime,session);
            session.getLifeCycleAware().onSessionConnected(session);
        }else {
            logger.info("syncRpc reconnect success , cost {} milltime,verifiedTimes={}, sessionInfo={}",costTime,verifiedTimes,session);
        }
    }

    /**
     * 是否是期望的连接响应
     * @param connectResponseEvent 连接结果事件
     * @param waitChannel 等待的channel
     * @param waitServerGuid 等待的服务器响应
     * @param sndTokenTimes 等待的连接请求标记
     * @return
     */
    private boolean isExpectConnectResponse(SyncConnectResponseEvent connectResponseEvent, Channel waitChannel,
                                            long waitServerGuid, int sndTokenTimes){
        //  这里需要判断channel，因为只有新的channel有意义，旧channel无意义
        return connectResponseEvent.getChannel() == waitChannel
                && connectResponseEvent.getServerGuid() == waitServerGuid
                && connectResponseEvent.getSndTokenTimes() == sndTokenTimes;
    }

    /**
     * 删除某个同步RPC服务器会话
     * @param serverGuid 服务器guid
     * @param reason 删除会话的原因
     * @return 返回删除的session，如果不存在则返回null
     */
    public @Nullable SyncC2SSession removeSession(long serverGuid, String reason){
        SessionWrapper sessionWrapper = sessionMap.remove(serverGuid);
        if (null==sessionWrapper){
            return null;
        }
        SyncC2SSession session=sessionWrapper.getSession();
        NetUtils.closeQuietly(sessionWrapper.getChannel());

        logger.info("remove syncRpc session by reason of,sessionInfo={}",reason,session);

        // 验证成功过(调用过connect)才会执行disconnect回调。
        if (sessionWrapper.getVerifiedSequencer().get() > 0 && session.getLifeCycleAware() != null){
            try {
                session.getLifeCycleAware().onSessionDisconnected(session);
            }catch (Exception e){
                logger.warn("disconnect callback caught exception");
            }
        }
        return session;
    }

    /**
     * 发起同步RPC调用请求
     * @param serverGuid 服务器id，向哪个服务器发起请求
     * @param request 请求内容。本质是rpc，只不过不是标准的rpc调用形式，对象的类型就决定了要调用的方法
     * @param responseClazz 响应消息的类型(帮助强转)
     * @param <T>
     * @return 注意查看Optional的文档 {@link Optional#isPresent()} {@link Optional#get()}
     * {@link Optional#ifPresent(Consumer)}
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> request(long serverGuid, @Nonnull Object request, Class<T> responseClazz){
        SessionWrapper sessionWrapper = sessionMap.get(serverGuid);
        if (null == sessionWrapper){
            throw new IllegalArgumentException("server " +serverGuid + " is not registered or removed");
        }
        // 使用的时候检查是否断开连接，断开了连接则重连
        if (!sessionWrapper.getChannel().isActive()){
            connect(sessionWrapper);
        }
        // 仍然不可用，无法建立连接
        if (!sessionWrapper.getChannel().isActive()){
            return Optional.empty();
        }
        long nextRequestGuid= sessionWrapper.getRequestGuidSequencer().incAndGet();
        SyncLogicRequestTO logicRequestTO=new SyncLogicRequestTO(nextRequestGuid,request);

        for (int tryTimes=1;tryTimes<=netConfigMrg.syncRpcMaxTryTimes();tryTimes++){
            SyncLogicResponseTO responseTO= requestImp(sessionWrapper,logicRequestTO,tryTimes);
            if (null!=responseTO){
                return (Optional<T>) Optional.ofNullable(responseTO.getResponse());
            }
        }
        return Optional.empty();
    }

    /**
     * 发起同步rpc请求的实现
     * @param sessionWrapper 会话信息
     * @param logicRequestTO 请求信息
     * @param tryTimes 当前尝试次数，1开始，大于1表示重试
     * @return 如果未得到结果则返回null
     */
    private SyncLogicResponseTO requestImp(SessionWrapper sessionWrapper, SyncLogicRequestTO logicRequestTO, int tryTimes){
        if (tryTimes==1){
            // 该请求第一次发起,结果集中的一定不存在期望的结果
            logicResponseQueue.clear();
        }else {
            // 该请求重试，先查看当前结果集是否有了结果，减少IO次数
            SyncLogicResponseEvent responseEvent = CollectionUtils.findElementWithPoll(logicResponseQueue,
                    e -> isExpectLogicResponse(e,sessionWrapper.getSession().getServerGuid(),logicRequestTO.getRequestGuid()));
            if(null!=responseEvent){
                return responseEvent.getLogicResponseTO();
            }
        }
        // 到这里，只能说明结果里暂时没有结果
        sessionWrapper.getChannel().writeAndFlush(logicRequestTO);

        // 等待结果到来
        SyncLogicResponseEvent responseEvent= CollectionUtils.waitElementWithPoll(logicResponseQueue,
                e -> isExpectLogicResponse(e,sessionWrapper.getSession().getServerGuid(),logicRequestTO.getRequestGuid()),
                netConfigMrg.syncRpcRequestTimeout());

        if (null!=responseEvent){
            return responseEvent.getLogicResponseTO();
        }else {
            return null;
        }
    }

    /**
     * 是否是期望的逻辑包响应
     * @param responseEvent 收到的某个请求响应
     * @param serverGuid 等待的服务器
     * @param waitRequestId 等待的请求id
     * @return
     */
    private boolean isExpectLogicResponse(SyncLogicResponseEvent responseEvent, long serverGuid, long waitRequestId){
        // 这里不判断channel，原因是：只要收到对应的结果即可，不论中途是否产生了网络故障导致了channel变更
        return responseEvent.getServerGuid() == serverGuid
                && responseEvent.getRequestGuid() == waitRequestId;
    }

    /**
     * 当接收到连接响应
     * @param connectResponseEvent 连接响应事件
     */
    public void onRcvConnectResponse(SyncConnectResponseEvent connectResponseEvent){
        connectResponseQueue.offer(connectResponseEvent);
    }

    /**
     * 当接收到逻辑包响应
     * @param logicResponseEvent 逻辑消息响应事件
     */
    public void onRcvLogicResponse(SyncLogicResponseEvent logicResponseEvent){
        logicResponseQueue.offer(logicResponseEvent);
    }

    /**
     * 会话包装类，不对外暴露细节
     */
    private static class SessionWrapper{
        /**
         * 原始session
         */
        private final SyncC2SSession session;
        /**
         * 请求id序号生成器
         */
        private final LongSequencer requestGuidSequencer = new LongSequencer(NetConstants.INIT_REQUEST_GUID);
        /**
         * 发送token次数序号生成器
         */
        private final IntSequencer sndTokenSequencer =new IntSequencer(0);
        /**
         * 已验证成功次数sequencer
         */
        private final IntSequencer verifiedSequencer =new IntSequencer(0);
        /**
         * 对应的channel
         */
        private Channel channel;
        /**
         * 客户端持有的服务器发来的token
         */
        private byte[] tokenBytes;

        private SessionWrapper(SyncC2SSession session, byte[] tokenBytes) {
            this.session = session;
            this.tokenBytes = tokenBytes;
        }

        public SyncC2SSession getSession() {
            return session;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel){
            this.channel=channel;
        }

        void setTokenBytes(byte[] tokenBytes) {
            this.tokenBytes = tokenBytes;
        }

        LongSequencer getRequestGuidSequencer() {
            return requestGuidSequencer;
        }

        IntSequencer getSndTokenSequencer() {
            return sndTokenSequencer;
        }

        byte[] getTokenBytes() {
            return tokenBytes;
        }

        IntSequencer getVerifiedSequencer() {
            return verifiedSequencer;
        }
    }
}
