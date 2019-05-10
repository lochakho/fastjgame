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
import com.wjybxx.fastjgame.mrg.NetConfigMrg;
import com.wjybxx.fastjgame.mrg.AbstractNetServiceMrg;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同步网络服务控制器。
 * 为什么需要独立的线程？why?
 * 1.异步网络服务使用RingBuffer，如果阻塞自己(消费者)，最终可能阻塞IO线程(生产者)，可能造成死锁或超时频发。
 * 2.死锁：它与当前断线重连机制不兼容，断线重连机制会保证包的顺序，如果不允许插队消费，结果返回也得等着前面的包被消费(然而当前线程阻塞中，导致死锁)。
 * 3.超时：它需要尽快的获得结果，当共用io线程时，这些异步服务的线程可能有大量的数据包。
 * 4.用在关键逻辑可以极大的简化编程难度。
 *
 * 导致的问题：
 * 1.同步请求的包和异步请求的包之间是没有严格顺序的，在进行逻辑处理时一定要注意。但它用在某些地方可能会大大降低复杂度。
 * 2.性能问题，大量使用同步将导致性能问题。只应该用在关键逻辑。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 20:11
 * @github - https://github.com/hl845740757
 */
public class SyncNetServiceMrg extends AbstractNetServiceMrg {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @Inject
    public SyncNetServiceMrg(NetConfigMrg netConfigMrg) {
        super(netConfigMrg);
    }

    @Override
    protected void startImp() {
        // 同步网络服务器只需要启动一个接收线程和IO线程即可。
        bossGroup=new NioEventLoopGroup(1,new BossThreadFactory());
        workerGroup=new NioEventLoopGroup(1,new WorkerTheadFactory());
    }

    @Override
    protected void shutdownImp() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    protected EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    protected EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    private static final class BossThreadFactory implements ThreadFactory {

        private final AtomicInteger threadId=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"SYNC_ACCEPTOR_THREAD_"+ threadId.getAndIncrement());
        }
    }

    private static final class WorkerTheadFactory implements ThreadFactory{

        private final AtomicInteger threadId=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"SYNC_IO_THREAD_"+threadId.getAndIncrement());
        }
    }
}
