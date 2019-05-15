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

package com.wjybxx.fastjgame.utils;

import com.wjybxx.fastjgame.function.AcquireFun;
import com.wjybxx.fastjgame.function.AcquireRemoteFun;
import com.wjybxx.fastjgame.function.TryAcquireFun;
import com.wjybxx.fastjgame.function.TryAcquireRemoteFun;

import javax.annotation.Nonnull;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 并发工具包
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 1:02
 * @github - https://github.com/hl845740757
 */
public class ConcurrentUtils {

    private ConcurrentUtils() {

    }

    /**
     * 在{@link CountDownLatch#await()}上等待，等待期间不响应中断
     * @param countDownLatch 闭锁
     */
    public static void awaitUninterruptibly(@Nonnull CountDownLatch countDownLatch){
        awaitUninterruptibly(countDownLatch,CountDownLatch::await);
    }

    /**
     * 在{@link Semaphore#acquire()}上等待，等待期间不响应中断
     * @param semaphore 信号量
     */
    public static void awaitUninterruptibly(Semaphore semaphore){
        awaitUninterruptibly(semaphore,Semaphore::acquire);
    }

    /**
     * 在等待期间不响应中断
     * @param resource 资源，锁，信号量等等
     * @param acquireFun 如果在资源上申请资源
     * @param <T> 资源的类型
     */
    public static <T> void awaitUninterruptibly(T resource, AcquireFun<T> acquireFun){
        boolean interrupted=false;
        try {
            while (true){
                try {
                    acquireFun.acquire(resource);
                    break;
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 在等待闭锁通过期间带有心跳(保持线程的活性，否则可能导致某些资源关闭)
     * @param countDownLatch 闭锁
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithHeartBeat(CountDownLatch countDownLatch, long heartbeat, TimeUnit timeUnit){
        awaitWithHeartBeat(countDownLatch,CountDownLatch::await,heartbeat,timeUnit);
    }

    /**
     * 在等待信号量期间带有心跳(保持线程的活性，否则可能导致某些资源关闭)
     * @param semaphore 信号量
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     */
    public static void awaitWithHeartBeat(Semaphore semaphore, long heartbeat, TimeUnit timeUnit){
        awaitWithHeartBeat(semaphore,Semaphore::tryAcquire,heartbeat,timeUnit);
    }

     /**
     * 在等待资源期间带有心跳(保持线程的活性，否则可能导致某些资源关闭)
     * @param resource 资源
     * @param tryAcquireFun 如何在资源上尝试获取资源
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     * @param <T> 资源的类型
     */
    public static <T> void awaitWithHeartBeat(T resource, TryAcquireFun<T> tryAcquireFun, long heartbeat, TimeUnit timeUnit){
        boolean interrupted=false;
        try {
            while (true){
                try {
                    if (tryAcquireFun.tryAcquire(resource,heartbeat,timeUnit)){
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    // 远程资源申请
    /**
     * 在等待远程资源期间不响应中断
     * @param resource 资源，锁，信号量等等
     * @param acquireFun 如果在资源上申请资源
     * @param <T> 资源的类型
     * @throws Exception
     */
    public static <T> void awaitRemoteUninterruptibly(T resource, AcquireRemoteFun<T> acquireFun) throws Exception {
        boolean interrupted=false;
        try {
            while (true){
                try {
                    acquireFun.acquire(resource);
                    break;
                }catch (InterruptedException e){
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 在等待远程资源时保持线程心跳
     * @param resource 要申请的远程资源
     * @param tryAcquireFun 尝试申请
     * @param heartbeat 心跳间隔
     * @param timeUnit 时间单位
     * @param <T> 资源类型
     * @throws Exception
     */
    public static <T> void awaitRemoteWithHeartBeat(T resource, TryAcquireRemoteFun<T> tryAcquireFun, long heartbeat, TimeUnit timeUnit) throws Exception {
        // 虽然是重复代码，但是不好消除
        boolean interrupted=false;
        try {
            while (true){
                try {
                    if (tryAcquireFun.tryAcquire(resource,heartbeat,timeUnit)){
                        break;
                    }
                } catch (InterruptedException e) {
                    interrupted=true;
                }
            }
        }finally {
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }
}
