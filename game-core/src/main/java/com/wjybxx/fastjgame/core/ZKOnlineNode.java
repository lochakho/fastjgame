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

package com.wjybxx.fastjgame.core;

/**
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 17:23
 * @github - https://github.com/hl845740757
 */
public abstract class ZKOnlineNode {

    /**
     * 绑定的tcp端口信息 host:port
     */
    private final String tcpAddress;
    /**
     * 绑定的websocket端口信息
     */
    private final String wsAddress;
    /**
     * 绑定的http端口信息
     */
    private final String httpAddress;
    /**
     * 同步rpc调用端口信息
     */
    private final String syncRpcAddress;

    public ZKOnlineNode(String tcpAddress, String wsAddress, String httpAddress, String syncRpcAddress) {
        this.tcpAddress = tcpAddress;
        this.wsAddress = wsAddress;
        this.httpAddress = httpAddress;
        this.syncRpcAddress = syncRpcAddress;
    }

    public String getTcpAddress() {
        return tcpAddress;
    }

    public String getWsAddress() {
        return wsAddress;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public String getSyncRpcAddress() {
        return syncRpcAddress;
    }
}
