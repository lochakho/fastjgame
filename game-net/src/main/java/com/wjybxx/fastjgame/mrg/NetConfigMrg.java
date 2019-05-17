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
import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.utils.ConfigLoader;
import com.wjybxx.fastjgame.utils.NetUtils;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 网络层配置管理器，不做热加载，网络层本身不需要经常修改，而且很多配置也无法热加载。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 15:09
 * @github - https://github.com/hl845740757
 */
public class NetConfigMrg {

    private final ConfigWrapper configWrapper;
    /**
     * 本机ip
     */
    private final String localIp;
    /**
     * 公网ip(好像不是很好处理，走配置倒是可以，就是有点不智能)
     * 作为备选方案
     */
    private final String outerIp;

    // 参数含义及单位见get方法或配置文件
    private final byte[] tokenKeyBytes;
    private final int tokenForbiddenTimeout;

    private final int maxIoThreadNum;
    private final int maxFrameLength;
    private final int sndBufferAsServer;
    private final int revBufferAsServer;
    private final int sndBufferAsClient;
    private final int revBufferAsClient;
    private final int ringBufferSize;
    private final int globalExecutorThreadNum;

    private final int connectMaxTryTimes;
    private final int connectTimeout;
    private final int waitTokenResultTimeout;
    private final int loginTokenTimeout;
    private final int ackTimeout;
    private final int sessionTimeout;

    private final int serverMaxCacheNum;
    private final int clientMaxCacheNum;

    private final int httpRequestTimeout;
    private final int httpSessionTimeout;

    private final int syncRpcConnectTimeout;
    private final int syncRpcRequestTimeout;
    private final int syncRpcMaxTryTimes;
    private final int syncRpcPingInterval;
    private final int syncRpcSessionTimeout;

    @Inject
    public NetConfigMrg() throws IOException {
        configWrapper = ConfigLoader.loadConfig(NetConfigMrg.class.getClassLoader(), NetConstants.NET_CONFIG_NAME);
        localIp= configWrapper.getAsString("localIp",NetUtils.getLocalIp());
        outerIp= configWrapper.getAsString("outerIp",NetUtils.getOuterIp());

        tokenKeyBytes = configWrapper.getAsString("tokenKey").getBytes(StandardCharsets.UTF_8);
        tokenForbiddenTimeout = configWrapper.getAsInt("tokenForbiddenTimeout",3600);

        maxIoThreadNum= configWrapper.getAsInt("maxIoThreadNum");
        maxFrameLength= configWrapper.getAsInt("maxFrameLength");
        sndBufferAsServer= configWrapper.getAsInt("sndBufferAsServer");
        revBufferAsServer= configWrapper.getAsInt("revBufferAsServer");
        sndBufferAsClient= configWrapper.getAsInt("sndBufferAsClient");
        revBufferAsClient= configWrapper.getAsInt("revBufferAsClient");
        ringBufferSize= configWrapper.getAsInt("ringBufferSize");
        globalExecutorThreadNum=configWrapper.getAsInt("globalExecutorThreadNum");

        serverMaxCacheNum= configWrapper.getAsInt("serverMaxCacheNum");
        clientMaxCacheNum= configWrapper.getAsInt("clientMaxCacheNum");

        connectMaxTryTimes = configWrapper.getAsInt("connectMaxTryTimes");
        connectTimeout = configWrapper.getAsInt("connectTimeout");
        waitTokenResultTimeout = configWrapper.getAsInt("waitTokenResultTimeout");
        loginTokenTimeout = configWrapper.getAsInt("loginTokenTimeout");
        ackTimeout = configWrapper.getAsInt("ackTimeout");
        sessionTimeout = configWrapper.getAsInt("sessionTimeout");


        httpRequestTimeout =configWrapper.getAsInt("httpRequestTimeout");
        httpSessionTimeout =configWrapper.getAsInt("httpSessionTimeout");

        syncRpcConnectTimeout =configWrapper.getAsInt("syncRpcConnectTimeout");
        syncRpcRequestTimeout =configWrapper.getAsInt("syncRpcRequestTimeout");
        syncRpcMaxTryTimes=configWrapper.getAsInt("syncRpcMaxTryTimes");
        syncRpcPingInterval=configWrapper.getAsInt("syncRpcPingInterval");
        syncRpcSessionTimeout=configWrapper.getAsInt("syncRpcSessionTimeout");
    }

    /**
     * 获取原始的config保证其，以获取不在本类中定义的属性
     * @return
     */
    public ConfigWrapper configWrapper() {
        return configWrapper;
    }

    /**
     * 获取本机内网ip
     * @return
     */
    public String localIp() {
        return localIp;
    }

    /**
     * 获取本机外网ip，如果没有，则是内网ip
     * @return
     */
    public String outerIp() {
        return outerIp;
    }

    /**
     * 用于默认的异或加密token的秘钥
     * @return
     */
    public byte[] getTokenKeyBytes(){
        return tokenKeyBytes;
    }
    /**
     * netty IO 线程数量
     * @return
     */
    public int maxIoThreadNum() {
        return maxIoThreadNum;
    }
    /**
     * 最大帧长度
     * @return
     */
    public int maxFrameLength(){
        return maxFrameLength;
    }

    /**
     * 作为服务器时的发送缓冲区
     * @return
     */
    public int sndBufferAsServer(){
        return sndBufferAsServer;
    }
    /**
     * 作为服务器时的接收缓冲区
     * @return
     */
    public int revBufferAsServer(){
        return revBufferAsServer;
    }
    /**
     * 作为客户端时的发送缓冲区
     * @return
     */
    public int sndBufferAsClient(){
        return sndBufferAsClient;
    }
    /**
     * 作为客户端时的接收缓冲区
     * @return
     */
    public int revBufferAsClient(){
        return revBufferAsClient;
    }

    /**
     * 全局线程池最大线程数
     * @return
     */
    public int globalExecutorThreadNum(){
        return globalExecutorThreadNum;
    }

    /**
     * 获取服务器最大可缓存消息数
     * @return
     */
    public int serverMaxCacheNum(){
        return serverMaxCacheNum;
    }

    /**
     * 获取客户端最大可缓存消息数
     * @return
     */
    public int clientMaxCacheNum(){
        return clientMaxCacheNum;
    }

    /**
     * 异步通信会话超时时间(秒)
     * @return
     */
    public int sessionTimeout(){
        return sessionTimeout;
    }

    /**
     * 获取Token登录超时时间(秒，登录Token时效性)
     * @return
     */
    public int loginTokenTimeout() {
        return loginTokenTimeout;
    }

    /**
     * 最大重连尝试次数(连接状态下尝试连接次数)
     */
    public int connectMaxTryTimes() {
        return connectMaxTryTimes;
    }

    /**
     * 异步建立连接超时时间(毫秒)
     */
    public long connectTimeout() {
        return connectTimeout;
    }

    /**
     * 等待token验证结果超时时间，需要适当的长一点(毫秒)
     * @return
     */
    public long waitTokenResultTimeout(){
        return waitTokenResultTimeout;
    }

    /**
     * 异步通信中ack超时时间(毫秒)
     */
    public long ackTimeout() {
        return ackTimeout;
    }

    /**
     * 获取ringBuffer缓冲区大小，必须是2的整次幂
     * @return
     */
    public int ringBufferSize() {
        return ringBufferSize;
    }

    /**
     * okHttpClient请求超时时间(秒)
     * @return
     */
    public int httpRequestTimeout(){
        return httpRequestTimeout;
    }

    /**
     * http会话超时时间(秒)
     * 此外，它也是检查session超时的间隔
     * @return
     */
    public int httpSessionTimeout(){
        return httpSessionTimeout;
    }

    /**
     * 建立同步rpc连接超时时间
     * @return
     */
    public int syncRpcConnectTimeout() {
        return syncRpcConnectTimeout;
    }

    /**
     * 同步rpc调用超时时间(毫秒)
     * @return
     */
    public int syncRpcRequestTimeout() {
        return syncRpcRequestTimeout;
    }

    /**
     * 同步rpc调用最大尝试次数
     * @return
     */
    public int syncRpcMaxTryTimes() {
        return syncRpcMaxTryTimes;
    }

    /**
     * 同步rpd调用 会话心跳包间隔(秒)
     * @return
     */
    public int syncRpcPingInterval() {
        return syncRpcPingInterval;
    }

    /**
     * 同步rpc调用 会话超时时间(秒)
     * @return
     */
    public int syncRpcSessionTimeout(){
        return syncRpcSessionTimeout;
    }

    /**
     * token被禁用的超时时间(秒)
     * @return
     */
    public int tokenForbiddenTimeout(){
        return tokenForbiddenTimeout;
    }
}
