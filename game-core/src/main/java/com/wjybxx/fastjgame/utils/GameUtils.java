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

import com.google.gson.GsonBuilder;
import com.wjybxx.fastjgame.misc.PortRange;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
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
    /**
     * 内部通信用的codec名字
     */
    public static final String INNER_CODEC_NAME = "protoBufCodec";
    /**
     * 服务器之间通信端口
     */
    public static final PortRange INNER_TCP_PORT_RANGE=new PortRange(10001,10500);
    public static final PortRange INNER_HTTP_PORT_RANGE=new PortRange(12001,12500);
    public static final PortRange INNER_SYNC_PORT_RANGE=new PortRange(14001,14500);
    /**
     * 与玩家之间通信端口
     */
    public static final PortRange OUTER_TCP_PORT_RANGE=new PortRange(16001,16500);
    public static final PortRange OUTER_WS_PORT_RANGE=new PortRange(18001,18500);

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
     * 安全的执行一个任务，只是将错误打印到日志，不抛出异常。
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

    /**
     * 序列化为json
     * @param obj 普通对象，若包含复杂对象需要自己管理
     * @return 序列化后的json字符串
     */
    public static String serializeToJson(Object obj){
        return new GsonBuilder()
                .create()
                .toJson(obj);
    }

    /**
     * 将json对象序列化为字节数组
     * @param obj json对象，如果是复杂对象，请自己管理
     * @return UTF-8编码的json字符
     */
    public static byte[] serializeToJsonBytes(Object obj){
        return serializeToJson(obj).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从json字符串中解析对象，如果是复杂对象，需要自己管理
     * @param json json字符串
     * @param clazz json字符串对应的类
     * @param <T> 对象类型
     * @return 反序列化得到的对象
     */
    public static <T> T parseFromJson(String json,Class<T> clazz){
        return new GsonBuilder()
                .create()
                .fromJson(json,clazz);
    }
    /**
     * 从json字符串UTF-8编码后的字节数组中解析对象，如果是复杂对象，需要自己管理
     * @param json json字符串UTF-8编码后的字节数组
     * @param clazz json字节数组对应的类
     * @param <T> 对象类型
     * @return 反序列化得到的对象
     */
    public static <T> T parseFromJsonBytes(byte[] json, Class<T> clazz){
        return parseFromJson(new String(json,StandardCharsets.UTF_8),clazz);
    }

    /**
     * 返回控制的会话生命周期通知器
     * @param <T> 会话参数类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> SessionLifecycleAware<T> emptyAware(){
        return (SessionLifecycleAware<T>) emptyAware;
    }

    private static SessionLifecycleAware<Object> emptyAware=new SessionLifecycleAware<Object>() {
        @Override
        public void onSessionConnected(Object t) {

        }

        @Override
        public void onSessionDisconnected(Object t) {

        }
    };
}
