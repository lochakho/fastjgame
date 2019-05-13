package com.wjybxx.fastjgame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏帮助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 15:33
 * @github - https://github.com/hl845740757
 */
public class GameUtils {

    private static final Logger logger= LoggerFactory.getLogger(GameUtils.class);

    private GameUtils() {
    }

    /**
     * 安静的关闭，忽略产生的异常
     * @param closeable 实现了close方法的对象
     */
    public static void closeQuietly(AutoCloseable closeable){
        if (null!=closeable){
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 安全的执行一个任务，不抛出异常。
     * {@link io.netty.channel.SingleThreadEventLoop#safeExecute(Runnable)}
     * @param task 要执行的任务，可以将要执行的方法封装为 ()-> safeExecute()
     */
    public static void safeExecute(Runnable task){
        try {
            task.run();
        } catch (Exception e) {
            logger.warn("A task raised an exception. Task: {}", task, e);
        }
    }
}
