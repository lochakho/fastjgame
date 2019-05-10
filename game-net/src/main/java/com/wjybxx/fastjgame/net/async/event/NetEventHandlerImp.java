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

import com.wjybxx.fastjgame.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetEventHandler的具体实现，负责管理world的启动、关闭、tick。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 23:55
 * @github - https://github.com/hl845740757
 */
public class NetEventHandlerImp implements NetEventHandler{

    private final Logger logger= LoggerFactory.getLogger(NetEventHandlerImp.class);
    /**
     * 最多连续处理多少个网络事件，尝试执行一次loop,默认值10000
     */
    private static final int LOOP_PER_EVENT_TIMES =10000;
    /**
     * 连续处理事件次数
     */
    private int handleEventTimes=0;
    /**
     * 游戏世界
     */
    private final World world;
    /**
     * 游戏世界tick间隔(毫秒)
     */
    private final long frameInterval;
    /**
     * 上次tick的时间戳
     */
    private long lastTickMillTime;

    /**
     * @param world 游戏世界
     * @param framesPerSecond 游戏世界帧率。建议10-50帧。
     *                        在繁忙的时候不一定能达成指定的帧数
     */
    public NetEventHandlerImp(World world, int framesPerSecond) {
        this.world = world;
        this.frameInterval = 1000/framesPerSecond;
    }

    /**
     * 启动过程中不允许出现任何错误，不可以在错误的状态下开始运行
     */
    @Override
    public void onStart() {
        logger.info("world thread starting.");
        try {
            world.onStart();
            logger.info("world thread start success.");
        }catch (Throwable e){
            logger.error("world thread start failed!",e);
            System.exit(-1);
        }
    }

    @Override
    public void onEvent(NetEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            world.onNetEvent(event);
        }catch (Throwable e){
            if (e instanceof OutOfMemoryError){
                logger.error("onNetEvent caught outOfMemory exception",e);
                System.exit(-2);
            }
            logger.warn("onEvent caught error ",e);
        }finally {
            event.close();
            handleEventTimes++;
            if (handleEventTimes == LOOP_PER_EVENT_TIMES){
                tryLoop();
            }
        }
    }

    @Override
    public void tryLoop() {
        // 处理同步rpc事件
        world.dispatchSyncRpcEvent();

        handleEventTimes=0;
        // 控制tick间隔
        long curMillTime=System.currentTimeMillis();
        if (curMillTime-lastTickMillTime<frameInterval){
            return;
        }
        lastTickMillTime=curMillTime;

        try {
            world.tick(curMillTime);
        }catch (Throwable e){
            if (e instanceof OutOfMemoryError){
                logger.error("tick caught outOfMemory exception",e);
                System.exit(-3);
            }
            logger.warn("loop caught exception ",e);
        }
    }

    @Override
    public void onWaitEvent() {
        tryLoop();
    }

    @Override
    public void onShutdown() {
        logger.info("world shutdown success.");
    }

}
