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

package com.wjybxx.fastjgame.core.onlinenode;

/**
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 17:23
 * @github - https://github.com/hl845740757
 */
public abstract class OnlineNodeData {

    /**
     * 服务器之间通信用的tcp端口信息，格式  host:port
     * (可以和对外开放的端口是同一个，如果与前端通信也用protoBuffer)
     */
    private final String innerTcpAddress;
    /**
     * 服务器之间同步rpc调用端口信息
     */
    private final String innerRpcAddress;

    /**
     * 用于GM等工具而绑定的http端口信息
     */
    private final String innerHttpAddress;

    public OnlineNodeData(String innerTcpAddress, String innerRpcAddress, String innerHttpAddress) {
        this.innerTcpAddress = innerTcpAddress;
        this.innerRpcAddress = innerRpcAddress;
        this.innerHttpAddress = innerHttpAddress;
    }

    public String getInnerTcpAddress() {
        return innerTcpAddress;
    }

    public String getInnerRpcAddress() {
        return innerRpcAddress;
    }

    public String getInnerHttpAddress() {
        return innerHttpAddress;
    }

}
