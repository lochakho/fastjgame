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

package com.wjybxx.fastjgame.net.sync.transferobject;

import com.wjybxx.fastjgame.net.common.TransferObject;

import javax.annotation.concurrent.Immutable;

/**
 * 建立连接请求传输对象
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 19:48
 * @github - https://github.com/hl845740757
 */
@Immutable
public class SyncConnectRequestTO implements TransferObject {
    /**
     * 我是谁(客户端)
     */
    private final long clientGuid;
    /**
     * 服务器guid,我想连谁
     */
    private final long serverGuid;
    /**
     * 这是客户端第几次发起连接请求，用于服务器识别最新请求和客户端追踪结果
     */
    private final int sndTokenTimes;
    /**
     * 客户端持有的token字节数组
     */
    private final byte[] encryptTokenBytes;

    public SyncConnectRequestTO(long clientGuid, long serverGuid, int sndTokenTimes, byte[] encryptTokenBytes) {
        this.clientGuid = clientGuid;
        this.serverGuid = serverGuid;
        this.sndTokenTimes = sndTokenTimes;
        this.encryptTokenBytes = encryptTokenBytes;
    }

    public long getClientGuid() {
        return clientGuid;
    }

    public long getServerGuid() {
        return serverGuid;
    }

    public int getSndTokenTimes() {
        return sndTokenTimes;
    }

    public byte[] getEncryptTokenBytes() {
        return encryptTokenBytes;
    }
}
