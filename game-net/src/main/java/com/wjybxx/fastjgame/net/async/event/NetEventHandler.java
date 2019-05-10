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

package com.wjybxx.fastjgame.net.async.event;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

/**
 * 网络事件处理器，显式继承{@link EventHandler}是为了表面它是一个单线程消费者。
 * 我们的游戏世界(业务逻辑处理)在{@link NetEventHandler}所在的线程。
 *
 * 显示的继承{@link LifecycleAware}因为我们需要在线程启动和结束时有必要的操作执行。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 9:49
 * @github - https://github.com/hl845740757
 */
public interface NetEventHandler extends EventHandler<NetEvent>, LifecycleAware {

    /**
     * 线程启动时，完成初始化工作
     */
    @Override
    void onStart();

    /**
     * 当产生一个网络事件的时候。
     * 注意：即使网络事件很多，也需要在某个时候调用loop，不能一直处理网络事件。
     * @param event
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    @Override
    void onEvent(NetEvent event, long sequence, boolean endOfBatch) throws Exception;

    /**
     * 尝试执行游戏世界循环。
     * 处理网络事件和游戏世界循环需要交替执行。
     * 注意：子类实现需要保证loop的间隔。
     */
    void tryLoop();

    /**
     * 当游戏世界线程没有新的事件消费时
     */
    void onWaitEvent();

    /**
     * 线程关闭时，释放资源等
     */
    @Override
    void onShutdown();
}
