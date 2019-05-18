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

import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.nodename.*;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.net.common.RoleType;
import org.apache.curator.utils.PathUtils;

/**
 * zookeeper节点路径辅助类。
 * 注意区分： 节点名字和节点路径的概念。
 * 节点路径：从根节点到当前节点的完整路径。
 * 节点名字：节点路径的最后一部分。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 20:49
 * @github - https://github.com/hl845740757
 */
public class ZKPathUtils {

    private static final String CHANNELID_PREFIX="channel-";

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
            throw new IllegalArgumentException("path " + path + " is root");
        }
        return path.substring(0,delimiterIndex);
    }

    /**
     * 获取父节点的名字
     * @param path
     * @return
     */
    private static String findParentNodeName(String path){
        return findNodeName(findParentPath(path));
    }

    /**
     * 构建一个全路径
     * @param parent 父节点全路径
     * @param nodeName 属性名字
     * @return
     */
    public static String makePath(String parent,String nodeName){
        return parent + "/" + nodeName;
    }

    /**
     * 找到一个合适的锁节点，锁的范围越小越好。
     * 对于非根节点来说，使用它的父节点路径。
     * 对应根节点来说，使用全局锁位置。
     * @param path 查询的路径
     * @return 一个合适的加锁路径
     */
    public static String findAppropriateLockPath(String path){
        PathUtils.validatePath(path);
        int delimiterIndex = path.lastIndexOf("/");
        if (delimiterIndex == 0){
            return globalLockPath();
        }
        return path.substring(0,delimiterIndex);
    }

    /**
     * 获取全局锁路径
     * @return
     */
    private static String globalLockPath() {
        return "/globalLock";
    }

    /**
     * 返回全局guidIndex所在路径
     * @return
     */
    public static String guidIndexPath(){
        return "/mutex/guid/guidIndex";
    }

    /**
     * 运行平台参数路径
     * @param platformType 平台枚举
     * @return
     */
    public static String platParamPath(PlatformType platformType){
        return "/config/platform/" + platformType;
    }

    /**
     * 真实服配置节点
     * @param platformType 平台枚举
     * @param actualServerId 真实服id，现存的服务器
     * @return
     */
    public static String actualServerConfigPath(PlatformType platformType, int actualServerId){
        return platParamPath(platformType) +"/actualserver/" + actualServerId;
    }

    /**
     * 逻辑服到真实服映射节点
     * @param platformType 平台枚举
     * @param logicServerId 逻辑服id(合服前的服id)
     * @return
     */
    public static String logicServerConfigPath(PlatformType platformType, int logicServerId){
        return platParamPath(platformType) +"/logicserver/" + logicServerId;
    }

    /**
     *
     * @param warzoneId
     * @return
     */
    public static String warzoneConfigPath(int warzoneId){
        return "/config/warzone/"+warzoneId;
    }

    /**
     * 本服进程申请channelId的地方(本服内竞争)
     * @param warzoneId 战区id
     * @param serverId 服id
     * @return 父节点路径
     */
    public static String singleChannelPath(int warzoneId,int serverId){
        // 拼一下，不弄那么深
        return "/mutex/channel/single/" + warzoneId + "-" + serverId + "/" + CHANNELID_PREFIX;
    }

    /**
     * 跨服进程申请channelId的地方(战区内竞争)
     * @param warzoneId 战区id
     * @return 父节点路径
     */
    public static String crossChannelPath(int warzoneId){
        return "/mutex/channel/cross/" + warzoneId + "/" + CHANNELID_PREFIX;
    }

    /**
     * 解析临时顺序节点的序号
     * @param path 有序节点的名字
     * @return zk默认序号是从0开始的，最小为0
     */
    public static int parseSequentialId(String path){
        return Integer.parseInt(findNodeName(path).split("-",2)[1]);
    }

    // region 在线节点路径

    /**
     * 在线节点信息的根节点
     * @return
     */
    public static String onlineRootPath(){
        return "/online";
    }

    /**
     * 同一个战区下的服务器注册在同一节点下.
     *
     * 如果战区跨平台：
     * online/warzone-x/
     * 如果战区不夸平台:
     * online/platfrom/warzone
     * @param warzoneId 战区id
     * @return
     */
    public static String onlineParentPath(int warzoneId){
        return "/online/warzone-" + warzoneId;
    }

    /**
     * 寻找在线节点所属的战区
     * @param onlinePath 在线节点的路径，在线节点全部在战区节点之下
     * @return
     */
    private static int findWarzoneId(String onlinePath){
        // "warzone-x"
        String onlineParentNodeName = findParentNodeName(onlinePath);
        String[] params = onlineParentNodeName.split("-", 2);
        return Integer.parseInt(params[1]);
    }

    // endregion 在线节点路径

    // region 在线节点名字

    /**
     * 通过服务器的节点名字解析服务器的类型。
     * 名字的第一个字段始终是服务器类型的枚举名。
     * @param nodeName 服务器节点名字
     * @return 返回服务器的类型
     */
    public static RoleType parseServerType(String nodeName){
        return RoleType.valueOf(nodeName.split("-",2)[0]);
    }

    /**
     * 为战区创建一个有意义的节点名字
     * @param warzoneId 战区id
     * @return 唯一的有意义的名字
     */
    public static String buildWarzoneNodeName(int warzoneId){
        return RoleType.WARZONE + "-" + warzoneId;
    }

    /**
     * 解析战区的节点路径(名字)
     * @param path fullpath
     * @return 战区基本信息
     */
    public static WarzoneNodeName parseWarzoneNodeNode(String path) {
        String[] params = findNodeName(path).split("-");
        int warzoneId = Integer.parseInt(params[1]);
        return new WarzoneNodeName(warzoneId);
    }

    /**
     * 为指定服创建一个有意义的节点名字
     * @param platformType 平台
     * @param serverId 几服
     * @return 唯一的有意义的名字
     */
    public static String buildCenterNodeName(PlatformType platformType, int serverId){
        return RoleType.CENTER + "-" + platformType + "-" + serverId;
    }

    /**
     * 解析game节点的路径(名字)
     * @param centerPath fullpath
     * @return game服的信息
     */
    public static CenterServerNodeName parseCenterNodeName(String centerPath){
        int warzoneId = findWarzoneId(centerPath);
        String[] params = findNodeName(centerPath).split("-");
        PlatformType platformType = PlatformType.valueOf(params[1]);
        int serverId = Integer.parseInt(params[2]);
        return new CenterServerNodeName(warzoneId, platformType, serverId);
    }

    /**
     * 为指定本服scene进程创建一个有意义的节点名字，用于注册到zookeeper
     * @param platformType 所属的平台
     * @param serverId 几服
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildSingleSceneNodeName(PlatformType platformType, int serverId, long processGuid){
        return RoleType.SCENE + "-" + SceneProcessType.SINGLE.name() + "-" + platformType + "-" + serverId + "-" + processGuid;
    }

    /**
     * 为跨服节点创建一个有意义的节点名字，用于注册到zookeeper
     * @param processGuid 进程guid
     * @return 唯一的有意义的名字
     */
    public static String buildCrossSceneNodeName(long processGuid){
        return RoleType.SCENE + "-" + SceneProcessType.CROSS.name() + "-" + processGuid;
    }

    /**
     * 通过场景节点的名字解析场景进程的类型
     * @param sceneNodePath scene节点的名字
     * @return scene进程的类型
     */
    public static SceneProcessType parseSceneType(String sceneNodePath){
        String[] params = findNodeName(sceneNodePath).split("-", 3);
        return SceneProcessType.valueOf(params[1]);
    }

    /**
     * 解析单服scene进程的节点路径(名字)
     * @param path fullpath
     * @return scene包含的基本信息
     */
    public static SingleSceneNodeName parseSingleSceneNodeName(String path){
        int warzoneId = findWarzoneId(path);
        String[] params = findNodeName(path).split("-");
        PlatformType platformType = PlatformType.valueOf(params[2]);
        int serverId = Integer.parseInt(params[3]);
        long processGuid = Long.parseLong(params[4]);
        return new SingleSceneNodeName(warzoneId, platformType, serverId,processGuid);
    }

    /**
     * 解析跨服节点的节点路径(名字)
     * @param path fullpath
     * @return 跨服节点信息
     */
    public static CrossSceneNodeName parseCrossSceneNodeName(String path){
        int warzoneId = findWarzoneId(path);
        String[] params = findNodeName(path).split("-");
        long processGuid = Long.parseLong(params[2]);
        return new CrossSceneNodeName(warzoneId,processGuid);
    }

    /**
     * 为loginserver创建一个节点名字
     * @param port 端口号
     * @param processGuid 进程guid
     * @return 一个唯一的有意义的名字
     */
    public static String buildLoginNodeName(int port,long processGuid){
        return RoleType.LOGIN + "-" + port + "-"+processGuid;
    }

    /**
     * 解析loginserver的节点名字
     * @param path 节点路径
     * @return
     */
    public static LoginServerNodeName parseLoginNodeName(String path){
        String[] params = findNodeName(path).split("-");
        int port=Integer.parseInt(params[1]);
        long processGuid = Long.parseLong(params[2]);
        return new LoginServerNodeName(port,processGuid);
    }
    // endregion
}
