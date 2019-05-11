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

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 全局的线程池管理器，用于执行一些不是那么大型的任务。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/10 0:34
 * @github - https://github.com/hl845740757
 */
public class GlobalExecutorMrg extends AbstractThreadLifeCycleHelper {

    private final ThreadPoolExecutor executorService;

    @Inject
    public GlobalExecutorMrg(NetConfigMrg netConfigMrg) {
        // 最多创建5个线程
        executorService =new ThreadPoolExecutor(1,netConfigMrg.globalExecutorThreadNum(),
                5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(),
                new DefaultThreadFactory());
        // 允许核心线程销毁
        executorService.allowCoreThreadTimeOut(true);
    }

    @Nonnull
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    protected void startImp() {
        // nothing
    }

    @Override
    protected void shutdownImp() {
        executorService.shutdown();
    }

    private static class DefaultThreadFactory implements ThreadFactory{

        private final AtomicInteger threadIndex=new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"global_executor_"+threadIndex.getAndIncrement());
        }
    }
}
