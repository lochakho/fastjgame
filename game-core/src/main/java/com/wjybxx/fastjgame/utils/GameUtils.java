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
import com.wjybxx.fastjgame.core.parserresult.CenterServerNodeName;
import com.wjybxx.fastjgame.core.parserresult.CrossSceneNodeName;
import com.wjybxx.fastjgame.core.parserresult.SingleSceneNodeName;
import com.wjybxx.fastjgame.core.parserresult.WarzoneNodeName;
import com.wjybxx.fastjgame.misc.PortRange;
import com.wjybxx.fastjgame.net.common.RoleType;
import org.apache.curator.utils.PathUtils;
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

    public static final PortRange INNER_TCP_PORT_RANGE=new PortRange(10001,10500);
    public static final PortRange INNER_HTTP_PORT_RANGE=new PortRange(12001,12500);
    public static final PortRange INNER_SYNC_PORT_RANGE=new PortRange(14001,14500);

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
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildSingleSceneNodeName(int warzoneId, int serverId, long processGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.SINGLE.name() + "-" + warzoneId + "-" + serverId + "-" + processGuid;
    }

    /**
     * 解析本地scene进程的节点路径(名字)
     * @param path fullpath
     * @return scene包含的基本信息
     */
    public static SingleSceneNodeName parseSingleSceneNodeName(String path){
        String[] params = findNodeName(path).split("-",5);
        int warzoneId = Integer.parseInt(params[2]);
        int serverId = Integer.parseInt(params[3]);
        long processGuid = Long.parseLong(params[4]);
        return new SingleSceneNodeName(warzoneId,serverId,processGuid);
    }

    /**
     * 为跨服节点创建一个有意义的节点名字，用于注册到zookeeper
     * @param warzoneId 战区id
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildCrossSceneNodeName(int warzoneId, long processGuid){
        return RoleType.SCENE_SERVER  + "-" + SceneProcessType.CROSS.name() + "-" + warzoneId + "-" + processGuid;
    }

    /**
     * 解析跨服节点的节点路径(名字)
     * @param path fullpath
     * @return 跨服节点信息
     */
    public static CrossSceneNodeName parseCrossSceneNodeName(String path){
        String[] params = findNodeName(path).split("-", 4);
        int warzoneId = Integer.parseInt(params[2]);
        long processGuid = Long.parseLong(params[3]);
        return new CrossSceneNodeName(warzoneId,processGuid);
    }

    /**
     * 通过场景节点的名字解析场景进程的类型
     * @param sceneNodePath scene节点的名字
     * @return scene进程的类型
     */
    public static SceneProcessType parseSceneType(String sceneNodePath){
        String[] params = findNodeName(sceneNodePath).split("-");
        return SceneProcessType.valueOf(params[1]);
    }

    /**
     * 为指定服创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @param serverId 几服
     * @param processGuid 该进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildGameNodeName(int warzoneId, int serverId,long processGuid){
        return RoleType.CENTER_SERVER + "-" + warzoneId + "-" + serverId + "-" + processGuid;
    }

    /**
     * 解析game节点的路径(名字)
     * @param gameNodeName fullpath
     * @return game服的信息
     */
    public static CenterServerNodeName parseGameNodeName(String gameNodeName){
        String[] params = findNodeName(gameNodeName).split("-", 4);
        int warzoneId = Integer.parseInt(params[1]);
        int serverId = Integer.parseInt(params[2]);
        long processGuid = Long.parseLong(params[3]);
        return new CenterServerNodeName(warzoneId, serverId,processGuid);
    }

    /**
     * 为战区创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @param processGuid 该进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildWarzoneNodeName(int warzoneId,long processGuid){
        return RoleType.WARZONE_SERVER + "-" + warzoneId + "-" + processGuid;
    }

    /**
     * 解析战区的节点路径(名字)
     * @param path fullpath
     * @return 战区基本信息
     */
    public static WarzoneNodeName parseWarzoneNodeNode(String path) {
        String[] params = findNodeName(path).split("-", 3);
        int warzoneId = Integer.parseInt(params[1]);
        long worldProcessGuid = Long.parseLong(params[2]);
        return new WarzoneNodeName(warzoneId, worldProcessGuid);
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
     * 寻找节点的名字，即最后一部分
     * @param path
     * @return
     */
    public static String findNodeName(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        return path.substring(delimiterIndex+1);
    }

    /**
     * 寻找节点的父节点路径
     * @param path 路径参数，不可以是根节点("/")
     * @return
     */
    public static String findParentPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        // root(nameSpace)
        if (delimiterIndex == 0){
            throw new IllegalArgumentException("path " + path + " is root parent");
        }
        return path.substring(0,delimiterIndex);
    }
}
