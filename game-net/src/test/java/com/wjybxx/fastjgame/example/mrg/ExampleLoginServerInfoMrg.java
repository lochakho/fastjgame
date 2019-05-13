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

package com.wjybxx.fastjgame.example.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.example.bean.ServerInfo;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.configwrapper.MapConfigWrapper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * 登录服信息管理器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 11:04
 * @github - https://github.com/hl845740757
 */
public class ExampleLoginServerInfoMrg extends ExampleWorldInfoMrg{

    /**
     * 要连接服服务器列表
     */
    private Long2ObjectMap<ServerInfo> serverMap =new Long2ObjectOpenHashMap<>(8);

    @Inject
    public ExampleLoginServerInfoMrg() {
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) {
        super.initImp(startArgs);

        parseServerInfos(startArgs);
    }

    @Override
    public RoleType getWorldType() {
        return RoleType.LOGIN_SERVER;
    }

    public Long2ObjectMap<ServerInfo> getServerMap() {
        return serverMap;
    }

    public ServerInfo getServerInfo(long serverGuid){
        return serverMap.get(serverGuid);
    }

    /**
     * 解析服务器信息
     * @param startArgs
     * @return
     */
    private void parseServerInfos(ConfigWrapper startArgs) {
        MapConfigWrapper mapConfigWrapper = startArgs.convert2MapWrapper();

        for (String key:mapConfigWrapper.keys()){
            if (key.startsWith("tcp_")){
                getServerInfo(key).setTcpAddress(mapConfigWrapper.getAsString(key));
                continue;
            }

            if (key.startsWith("ws_")){
                getServerInfo(key).setWsAddress(mapConfigWrapper.getAsString(key));
                continue;
            }

            if (key.startsWith("http_")){
                getServerInfo(key).setHttpAddress(mapConfigWrapper.getAsString(key));
                continue;
            }
            if (key.startsWith("syncRpc_")){
                getServerInfo(key).setSyncRpcAddress(mapConfigWrapper.getAsString(key));
                continue;
            }
        }
    }

    private ServerInfo getServerInfo(String key) {
        long serverGuid= Long.parseLong(key.split("_", 2)[1]);
        return serverMap.computeIfAbsent(serverGuid, ServerInfo::new);
    }

}
