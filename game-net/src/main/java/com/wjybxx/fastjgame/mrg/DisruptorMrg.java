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
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.async.SleepingWaitExtendStrategy;
import com.wjybxx.fastjgame.net.async.event.*;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.ThreadFactory;

/**
 * disruptor控制器，它本身不是线程安全的。
 * 我们通过严格的线程启动顺序保证安全性：
 * 1.main 启动 disruptor消费者(业务逻辑线程)
 * 2.业务逻辑线程 启动 Netty线程
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 12:50
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class DisruptorMrg extends AbstractThreadLifeCycleHelper {

    private static final Logger logger= LoggerFactory.getLogger(DisruptorMrg.class);

    private final NetConfigMrg configMrg;

    /**
     * 游戏世界线程工厂(逻辑线程工厂/disruptor消费者工厂)。
     * 理论上只会创建一个线程，它取决于handler的数量。
     */
    private ThreadFactory worldThreadFactory;
    /**
     * 网络事件处理器，运行在游戏世界线程。
     */
    private NetEventHandler netEventHandler;
    /**
     * disruptor 对象，保留以控制生命周期
     */
    private Disruptor<NetEvent> disruptor;
    /**
     * RingBuffer 网络事件缓冲区/逻辑事件缓冲区
     */
    private RingBuffer<NetEvent> logicQueue;

    @Inject
    public DisruptorMrg(NetConfigMrg configMrg) {
        this.configMrg=configMrg;
    }

    /**
     * 启动游戏世界线程(启动disruptor消费者线程)。
     * 它是游戏世界的启动口。
     */
    @Override
    protected void startImp() {
        if (netEventHandler==null || worldThreadFactory==null){
            throw new IllegalStateException("netEventHandler==null || worldThreadFactory==null");
        }
        SleepingWaitExtendStrategy<NetEvent> sleepingWaitStrategy=new SleepingWaitExtendStrategy<>(netEventHandler);
        // 线程的数量取决于handler数量，而不是Executor
        // Netty线程作为生产者，因此是多线程，而消费者只有游戏世界(逻辑线程)，因此是单线程。
        // 总结：多生产者-单消费者模型
        this.disruptor= new Disruptor<NetEvent>(
                new NetEventFactory(),
                configMrg.ringBufferSize(),
                worldThreadFactory,
                ProducerType.MULTI,
                sleepingWaitStrategy
        );
        this.disruptor.handleEventsWith(netEventHandler);
        this.logicQueue=disruptor.getRingBuffer();
        this.disruptor.start();
    }

    /**
     * 关闭游戏世界(关闭disruptor消费者线程)。
     * 它是游戏世界的结束口。
     */
    @Override
    protected void shutdownImp() {
        // 这里不能调用shutdown，如果调用shutdown会等待消费者消费完所有事件！
        // 然后消费者就是当前线程，阻塞等待自己，死锁。
        disruptor.halt();
    }

    /**
     * 启动游戏世界
     * @param worldThreadFactory 游戏世界线程工厂
     * @param netEventHandler 游戏世界(网络事件处理器)
     */
    public void start(ThreadFactory worldThreadFactory, NetEventHandler netEventHandler){
        this.worldThreadFactory=worldThreadFactory;
        this.netEventHandler = netEventHandler;
        super.start();
    }

    /**
     * 发布事件，disruptor要求必须使用try finally块的方式发布，否则可能阻塞生产者。
     * @param channel 产生事件的channel
     * @param eventType 事件类型
     * @param eventParam 事件参数。类型决定参数
     */
    public void publishEvent(@Nullable Channel channel, NetEventType eventType, NetEventParam eventParam){
        long sequence=logicQueue.next();
        try {
            NetEvent netEvent =logicQueue.get(sequence);
            netEvent.setChannel(channel);
            netEvent.setEventType(eventType);
            netEvent.setNetEventParam(eventParam);
        }finally {
            logicQueue.publish(sequence);
        }
    }

}
