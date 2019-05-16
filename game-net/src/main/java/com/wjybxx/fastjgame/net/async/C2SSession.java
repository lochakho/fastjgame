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

package com.wjybxx.fastjgame.net.async;

import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 客户端到服务器的会话信息
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:00
 * @github - https://github.com/hl845740757
 */
public class C2SSession {
    /**
     * 服务器唯一标识(会话id)
     */
    private final long serverGuid;
    /**
     * 服务器类型
     */
    private final RoleType roleType;
    /**
     * 服务器地址
     */
    private final HostAndPort hostAndPort;
    /**
     * 该会话使用的initializer提供者
     */
    private final Supplier<ChannelInitializer<SocketChannel>> initializerSupplier;
    /**
     * 生命周期回调接口
     */
    private final SessionLifecycleAware<C2SSession> lifecycleAware;

    public C2SSession(long serverGuid, RoleType roleType, HostAndPort hostAndPort,
                      Supplier<ChannelInitializer<SocketChannel>> initializerSupplier,
                      SessionLifecycleAware<C2SSession> lifecycleAware) {
        this.serverGuid = serverGuid;
        this.roleType = roleType;
        this.hostAndPort=hostAndPort;
        this.initializerSupplier = initializerSupplier;
        this.lifecycleAware = lifecycleAware;
    }

    public long getServerGuid() {
        return serverGuid;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public Supplier<ChannelInitializer<SocketChannel>> getInitializerSupplier() {
        return initializerSupplier;
    }

    public @Nullable SessionLifecycleAware<C2SSession> getLifecycleAware() {
        return lifecycleAware;
    }

    @Override
    public String toString() {
        return "C2SSession{" +
                "serverGuid=" + serverGuid +
                ", roleType=" + roleType +
                ", hostAndPort=" + hostAndPort +
                '}';
    }
}
