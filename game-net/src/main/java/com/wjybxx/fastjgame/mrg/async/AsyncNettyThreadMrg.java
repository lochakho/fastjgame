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
import com.wjybxx.fastjgame.mrg.NetConfigMrg;
import com.wjybxx.fastjgame.mrg.NettyThreadMrg;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步网络服务控制器。
 * 它和disruptor构成多生产者单消费者模型，使用RingBuffer进行速度协调。
 *
 * 与之关联的会话处理器为： {@link S2CSessionMrg} 和 {@link C2SSessionMrg}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:08
 * @github - https://github.com/hl845740757
 */
public class AsyncNettyThreadMrg extends NettyThreadMrg {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Inject
    public AsyncNettyThreadMrg(NetConfigMrg netConfigMrg) {
        super(netConfigMrg);
    }

    /**
     * 启动netty线程
     */
    @Override
    protected void startImp() {
        bossGroup=new NioEventLoopGroup(1,new BossThreadFactory());
        workerGroup=new NioEventLoopGroup(netConfigMrg.maxIoThreadNum(),new WorkerTheadFactory());
    }

    /**
     * 关闭Netty的线程
     */
    @Override
    protected void shutdownImp() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    private static final class BossThreadFactory implements ThreadFactory {

        private final AtomicInteger threadId=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"ACCEPTOR_THREAD_"+ threadId.getAndIncrement());
        }
    }

    private static final class WorkerTheadFactory implements ThreadFactory{

        private final AtomicInteger threadId=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"IO_THREAD_"+threadId.getAndIncrement());
        }
    }
}
