package com.wjybxx.fastjgame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 游戏帮助类;
 * (不知道放哪儿的方法就放这里)
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
     * {@code io.netty.channel.SingleThreadEventLoop#safeExecute(Runnable)}
     * @param task 要执行的任务，可以将要执行的方法封装为 ()-> safeExecute()
     */
    public static void safeExecute(Runnable task){
        try {
            task.run();
        } catch (Exception e) {
            logger.warn("A task raised an exception. Task: {}", task, e);
        }
    }

    /**
     * 字符串具有可读性
     * @param integer
     * @return
     */
    public static byte[] serializeToStringBytes(int integer){
        return String.valueOf(integer).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从字符串字节数组中解析一个int
     * @param bytes
     * @return
     */
    public static int parseIntFromStringBytes(byte[] bytes){
        return Integer.parseInt(new String(bytes,StandardCharsets.UTF_8));
    }
}
