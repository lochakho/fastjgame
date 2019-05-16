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
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.misc.PortRange;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象网络服务器控制器
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 20:02
 * @github - https://github.com/hl845740757
 */
public abstract class NettyThreadMrg extends AbstractThreadLifeCycleHelper {

    protected final NetConfigMrg netConfigMrg;

    @Inject
    protected NettyThreadMrg(NetConfigMrg netConfigMrg) {
        this.netConfigMrg = netConfigMrg;
    }

    /**
     * Reactor模型中的acceptor
     */
    public abstract EventLoopGroup getBossGroup();
    /**
     * Reactor模型中的worker
     */
    public abstract EventLoopGroup getWorkerGroup();

}
