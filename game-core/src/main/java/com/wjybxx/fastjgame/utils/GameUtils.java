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
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.net.common.RoleType;
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
    public static <T> T parseFromJson(byte[] json,Class<T> clazz){
        return parseFromJson(new String(json,StandardCharsets.UTF_8),clazz);
    }

    /**
     * 为指定本服scene进程创建一个有意义的节点名字，用于注册到zookeeper
     * @param warzoneId 战区id
     * @param serverId 几服
     * @param worldGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildLocalSceneNodeName(int warzoneId, int serverId, long worldGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.LOCAL.name() + "-" + warzoneId + "-" + serverId + "-" + worldGuid;
    }

    /**
     * 为跨服节点创建一个有意义的节点名字，用于注册到zookeeper
     * @param warzoneId 战区id
     * @param worldGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildCrossSceneNodeName(int warzoneId, long worldGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.CROSS.name() + "-" + warzoneId + "-" + worldGuid;
    }

    /**
     * 为指定服创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @param serverId 几服
     * @param worldGuid 该进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildGameNodeName(int warzoneId, int serverId,long worldGuid){
        return RoleType.GAME_SERVER + "-" + warzoneId + "-" + serverId + "-" + worldGuid;
    }

    /**
     * 为战区创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @param worldGuid 该进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildWarzoneNodeName(int warzoneId,long worldGuid){
        return RoleType.WARZONE_SERVER + "-" + warzoneId + "-" + worldGuid;
    }

    /**
     *
     * @param path
     * @return
     */
    public static int parseWarzoneIdFromWarzoneNode(String path) {
        return 0;
    }

    /**
     * 通过服务器的节点名字解析服务器的类型
     * @param nodeName 服务器节点名字
     * @return 返回服务器的类型
     */
    public static RoleType parseServerType(String nodeName){
        return RoleType.valueOf(nodeName.split("-",2)[0]);
    }

    /**
     * 通过场景节点的名字解析场景进程的类型
     * @param sceneNodeName scene节点的名字
     * @return scene进程的类型
     */
    public static SceneProcessType parseSceneType(String sceneNodeName){
        return SceneProcessType.valueOf(sceneNodeName.split("-",3)[1]);
    }

}
