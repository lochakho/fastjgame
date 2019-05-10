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

package com.wjybxx.fastjgame.net.sync;

import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.common.RoleType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 客户端保存的会话信息
 * (我主动发起的连接)
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 16:22
 * @github - https://github.com/hl845740757
 */
public class SyncC2SSession {
    /**
     * 服务器唯一标识
     */
    private final long serverGuid;
    /**
     * 服务器类型
     */
    private final RoleType roleType;
    /**
     * 服务器的地址
     */
    private final String host;
    /**
     * 服务器监听的端口
     */
    private final int port;

    private final Supplier<ChannelInitializer<SocketChannel>> initializerSupplier;
    /**
     * 绑定的生命周期处理器
     */
    private final SessionLifecycleAware<SyncC2SSession> lifeCycleAware;

    public SyncC2SSession(long serverGuid, RoleType roleType, String host, int port,
                          Supplier<ChannelInitializer<SocketChannel>> initializerSupplier, SessionLifecycleAware<SyncC2SSession> lifeCycleAware) {
        this.serverGuid = serverGuid;
        this.roleType = roleType;
        this.host = host;
        this.port = port;
        this.initializerSupplier=initializerSupplier;
        this.lifeCycleAware = lifeCycleAware;
    }

    public long getServerGuid() {
        return serverGuid;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Supplier<ChannelInitializer<SocketChannel>> getInitializerSupplier() {
        return initializerSupplier;
    }

    public @Nullable SessionLifecycleAware<SyncC2SSession> getLifeCycleAware() {
        return lifeCycleAware;
    }

    @Override
    public String toString() {
        return "SyncC2SSession{" +
                "serverGuid=" + serverGuid +
                ", roleType=" + roleType +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
