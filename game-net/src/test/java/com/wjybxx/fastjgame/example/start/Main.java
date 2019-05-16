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

package com.wjybxx.fastjgame.example.start;

import com.google.inject.AbstractModule;
import com.wjybxx.fastjgame.NetBootstrap;
import com.wjybxx.fastjgame.example.module.ExampleGameServerModule;
import com.wjybxx.fastjgame.example.module.ExampleLoginServerModule;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.configwrapper.ArrayConfigWrapper;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;

import java.io.File;

/**
 * 以下参数替换ip后可直接复制到启动参数。
 *
 * 服务端(游戏服)的参数如(我们启动两个服务器，每个服务器同时监听tcp端口、websocket端口、http端口、SyncRpc端口
 roleType=CENTER_SERVER processGuid=1 tcpPort=10001 wsPort=10002 httpPort=10003 syncRpcPort=10004
 roleType=CENTER_SERVER processGuid=2 tcpPort=20001 wsPort=20002 httpPort=20003 syncRpcPort=20004
 *
 * 客户端(登录服)可以连接任意个数服务器(参数项决定)
 * 一个客户端最多可以连接同一个服4个端口中的三个， http端口 + syncRpc端口 + tcp端口或websocket端口
 * (tcp和websocket只能二选一，否则sessionId冲突)，一个都不连的话啥也不干.
 *
 * tcp客户端(同时连接服务器的tcp端口和syncRpc端口)：
 roleType=LOGIN_SERVER processGuid=20001
 tcp_1=192.168.1.103:10001 syncRpc_1=192.168.1.103:10004
 tcp_2=192.168.1.103:20001 syncRpc_2=192.168.1.103:20004
 *
 * ws客户端(同时连接服务器的ws端口和syncRpc端口)：
 roleType=LOGIN_SERVER processGuid=20002
 ws_1=192.168.1.103:10002 syncRpc_1=192.168.1.103:10004
 ws_2=192.168.1.103:20002 syncRpc_2=192.168.1.103:20004
 *
 * http客户端(只连接http端口)：
 roleType=LOGIN_SERVER processGuid=20003 http_1=192.168.1.103:10003 http_2=192.168.1.103:20003
 *
 * 参数详细意义可参考{@link com.wjybxx.fastjgame.example.mrg.ExampleLoginServerInfoMrg#parseServerInfos(ConfigWrapper)}
 * tcp_serverGuid 意味着连接指定 serverGuid 的http端口，后面的属性为地址
 * ws_serverGuid 意味着连接指定 serverGuid 的websocket端口，后面的属性为地址
 * http_serverGuid 意味着连接指定 serverGuid 的http端口，后面的属性为地址
 * syncRpc_serverGuid 意味着连接指定 serverGuid 的syncRpc端口，后面的属性为地址
 * 192.168.1.103 需要替换为服务器端ip地址(本机启动服务器的话为本机ipv4地址)
 *
 * Main必须完成的是初始化log文件路径。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 15:28
 * @github - https://github.com/hl845740757
 */
public class Main {

    public static void main(String[] args) throws Exception {
        ConfigWrapper configWrapper =new ArrayConfigWrapper(args);
        // 启动的进程类型
        RoleType roleType= RoleType.valueOf(configWrapper.getAsString("roleType"));
        // 进程唯一编号
        long processGuid= configWrapper.getAsLong("processGuid");
        // 日志文件夹
        String logDir= configWrapper.getAsString("logDic");
        if (logDir==null){
            logDir=new File("").getAbsolutePath() + File.separator + "log";
        }
        // 必须先初始化日志路径属性(后面的类可能会立即使用logger)
        String logFilePath = logDir + File.separator + roleType.name().toLowerCase() + "-" + processGuid + ".log";
        System.setProperty("logFilePath",logFilePath);

        AbstractModule module;
        int framesPerSecond;

        switch (roleType){
            case CENTER_SERVER:
                module=new ExampleGameServerModule();
                framesPerSecond=25;
                break;
            case LOGIN_SERVER:
                module=new ExampleLoginServerModule();
                framesPerSecond=1;
                break;
                default:
                    throw new IllegalArgumentException("unexpect roleType");
        }
        new NetBootstrap<>()
                .setArgs(configWrapper)
                .addModule(module)
                .setFramesPerSecond(framesPerSecond)
                .start();
    }
}
