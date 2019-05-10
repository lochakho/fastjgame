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

package com.wjybxx.fastjgame.example.bean;

import com.wjybxx.fastjgame.misc.IntSequencer;

import javax.annotation.Nullable;

/**
 * 测试用的服务器信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 15:18
 * @github - https://github.com/hl845740757
 */
public class ServerInfo {

    /**
     * 服务器guid
     */
    private final long serverGuid;
    /**
     * 服务器绑定的4个端口信息，null表示不连接
     */
    private String tcpAddress;
    private String wsAddress;
    private String httpAddress;
    private String syncRpcAddress;

    /**
     * 当前是否已注册(tcp或websocket端口)
     */
    private boolean tcpOrWsRegistered = false;
    /**
     * 当前消息发送的账户id
     */
    private final IntSequencer accountSequencer=new IntSequencer(0);
    /**
     * 同步rpc端口是否已注册
     */
    private boolean syncRpcRegistered =false;

    private final IntSequencer syncRpcSequencer=new IntSequencer(0);

    public ServerInfo(long serverGuid) {
        this.serverGuid = serverGuid;
    }

    /**
     * 服务器guid
     */
    public long getServerGuid() {
        return serverGuid;
    }

    /**
     * 当前是否已连接
     */
    public boolean isTcpOrWsRegistered() {
        return tcpOrWsRegistered;
    }

    public void setTcpOrWsRegistered(boolean tcpOrWsRegistered) {
        this.tcpOrWsRegistered = tcpOrWsRegistered;
    }

    public boolean isSyncRpcRegistered() {
        return syncRpcRegistered;
    }

    public void setSyncRpcRegistered(boolean syncRpcRegistered) {
        this.syncRpcRegistered = syncRpcRegistered;
    }

    public IntSequencer getAccountSequencer() {
        return accountSequencer;
    }

    public IntSequencer getSyncRpcSequencer() {
        return syncRpcSequencer;
    }

    @Nullable
    public String getTcpAddress() {
        return tcpAddress;
    }

    public void setTcpAddress(String tcpAddress) {
        this.tcpAddress = tcpAddress;
    }

    @Nullable
    public String getWsAddress() {
        return wsAddress;
    }

    public void setWsAddress(String wsAddress) {
        this.wsAddress = wsAddress;
    }

    @Nullable
    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    @Nullable
    public String getSyncRpcAddress() {
        return syncRpcAddress;
    }

    public void setSyncRpcAddress(String syncRpcAddress) {
        this.syncRpcAddress = syncRpcAddress;
    }
}
