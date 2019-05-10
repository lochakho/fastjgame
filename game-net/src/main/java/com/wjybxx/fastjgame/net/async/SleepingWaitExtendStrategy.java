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

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.wjybxx.fastjgame.net.async.event.NetEventHandler;

import java.util.concurrent.locks.LockSupport;

/**
 * 消费者等待策略 (也就是我们的游戏世界等待网络事件时的策略)。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:06
 * @github - https://github.com/hl845740757
 */
public class SleepingWaitExtendStrategy<T> implements WaitStrategy {

    private static final int DEFAULT_RETRIES = 200;
    private static final long DEFAULT_SLEEP = 10*10000;

    private final int retries;
    private final long sleepTimeNs;

    private final NetEventHandler eventConsumer;

    public SleepingWaitExtendStrategy(NetEventHandler eventConsumer)
    {
        this.eventConsumer = eventConsumer;
        this.retries = DEFAULT_RETRIES;
        this.sleepTimeNs = DEFAULT_SLEEP;
    }

    @Override
    public long waitFor(final long sequence, Sequence cursor, final Sequence dependentSequence,
                        final SequenceBarrier barrier) throws AlertException {

        long availableSequence;
        int counter = retries;

        // dependentSequence 该项目组织架构中，其实只要生产者的sequence。
        // 在等待生产者生产数据的过程中，尝试执行游戏世界循环
        while ((availableSequence = dependentSequence.get()) < sequence)
        {
            eventConsumer.onWaitEvent();
            counter = applyWaitMethod(barrier, counter);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, int counter)
            throws AlertException
    {
        barrier.checkAlert();

        if (counter > 100) {
            --counter;// 大于100时自旋
        }
        else if (counter > 0) {
            --counter;
            Thread.yield();//大于0时尝试让出Cpu
        }
        else
        {
            LockSupport.parkNanos(sleepTimeNs);//等到最大次数了，睡眠等待
        }
        return counter;
    }
}
