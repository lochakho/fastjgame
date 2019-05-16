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
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * 游戏服信息管理器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 11:05
 * @github - https://github.com/hl845740757
 */
public class ExampleGameServerInfoMrg extends ExampleWorldInfoMrg{

    private int tcpPort;
    private int wsPort;
    private int httpPort;
    /**
     * 允许同步rpc调用的端口
     */
    private int syncRpcPort;

    @Inject
    public ExampleGameServerInfoMrg() {
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) {
        super.initImp(startArgs);

        tcpPort= startArgs.getAsInt("tcpPort",-1);
        wsPort= startArgs.getAsInt("wsPort",-1);
        httpPort= startArgs.getAsInt("httpPort",-1);
        syncRpcPort=startArgs.getAsInt("syncRpcPort",-1);

        // tcp端口和websocket端口至少开启一个
        boolean bothLtZero = tcpPort < 0 && wsPort < 0;
        if (bothLtZero){
            throw new IllegalArgumentException("tcpPort="+ tcpPort + ", wsPort="+wsPort);
        }
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.CENTER_SERVER;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getWsPort() {
        return wsPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getSyncRpcPort() {
        return syncRpcPort;
    }
}
