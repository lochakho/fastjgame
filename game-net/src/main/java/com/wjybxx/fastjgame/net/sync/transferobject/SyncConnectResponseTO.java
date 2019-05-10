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

/**
 * 建立连接响应传输对象
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 19:52
 * @github - https://github.com/hl845740757
 */
public class SyncConnectResponseTO implements TransferObject {
    /**
     * 起始请求id(客户端发来的值，服务器验证之后返回)
     */
    private final int sndTokenTimes;
    /**
     * 是否允许建立链接 1运行 0不允许
     */
    private final boolean success;
    /**
     * 返回给客户端的新token
     */
    private final byte[] encryptTokenBytes;

    public SyncConnectResponseTO(int sndTokenTimes, boolean success, byte[] encryptTokenBytes) {
        this.sndTokenTimes = sndTokenTimes;
        this.success = success;
        this.encryptTokenBytes = encryptTokenBytes;
    }

    public int getSndTokenTimes() {
        return sndTokenTimes;
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getEncryptTokenBytes() {
        return encryptTokenBytes;
    }
}
