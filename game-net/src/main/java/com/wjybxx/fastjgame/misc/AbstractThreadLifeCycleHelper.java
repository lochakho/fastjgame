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

package com.wjybxx.fastjgame.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简单的线程生命周期控制帮助类。
 * 它并不是一个通用的帮助类，它并没有真正的管理线程的状态，只是管理方法的调用问题。
 * (真正的线程生命周期由netty、disruptor等等它们自己管理)
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 16:22
 * @github - https://github.com/hl845740757
 */
public abstract class AbstractThreadLifeCycleHelper {

    private static final Logger logger= LoggerFactory.getLogger(AbstractThreadLifeCycleHelper.class);

    /**
     * 空闲状态(未启动状态)，还未成功调用{@link #start()}方法
     */
    private static final int NOT_START = 1;
    /**
     * 运行状态，成功调用过{@link #start()}方法，还未成功调用过@link #shutdown()}方法。
     */
    private static final int RUNNING= NOT_START +1;
    /**
     * 已关闭，成功调用{@link #shutdown()}方法之后
     */
    private static final int SHUTDOWN=RUNNING+1;
    /**
     * 当前线程的状态
     */
    private final AtomicInteger state = new AtomicInteger(NOT_START);

    /**
     * 请求启动线程
     * @return
     */
    public final boolean start() throws Exception {
        if (state.compareAndSet(NOT_START,RUNNING)){
            startImp();
            logger.info("{} start thread(s) success!",this.getClass().getSimpleName());
            return true;
        }else {
            logger.error("thread(s) already start or shutdown");
            return false;
        }
    }

    /**
     * 子类真正的启动线程
     * @throws Exception 允许启动时抛出异常
     */
    protected abstract void startImp() throws Exception;

    /**
     * 请求关闭线程
     * @return
     */
    public final boolean shutdown(){
        if (state.compareAndSet(RUNNING,SHUTDOWN)){
            shutdownImp();
            logger.info("{} shutdown thread(s) success!",this.getClass().getSimpleName());
            return true;
        }else {
            logger.error("thread(s) already shutdown or not start!");
            return false;
        }
    }

    /**
     * 子类真正的关闭线程
     */
    protected abstract void shutdownImp();

    /**
     * 是否处于空闲状态
     * @return
     */
    public boolean isIdle(){
        return state.get() == NOT_START;
    }

    /**
     * 是否正在运行
     * @return
     */
    public boolean isRunning(){
        return state.get() == RUNNING;
    }

    /**
     * 是否已关闭
     * @return
     */
    public boolean isShutdown(){
        return state.get() == SHUTDOWN;
    }

}
