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

package com.wjybxx.fastjgame.net.sync.event;

import java.util.Arrays;

/**
 * 同步包类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/30 0:56
 * @github - https://github.com/hl845740757
 */
public enum SyncPkgType {
    /**
     * 客户端请求建立链接
     */
    SYNC_CONNECT_REQUEST((byte) 1),
    /**
     * 服务器通知建立连接结果
     */
    SYNC_CONNECT_RESPONSE((byte)2),
    /**
     * 逻辑消息请求包
     */
    SYNC_LOGIC_REQUEST((byte)3),
    /**
     * 逻辑消息响应包
     */
    SYNC_LOGIC_RESPONSE((byte)4),
    /**
     * ping包
     */
    SYNC_PING((byte)5);

    public final byte pkgType;

    SyncPkgType(byte pkgType) {
        this.pkgType = pkgType;
    }

    /**
     * 排序号的枚举数组，方便查找
     */
    private static final SyncPkgType[] sortValues;

    static {
        SyncPkgType[] values = SyncPkgType.values();
        SyncPkgType[] syncPkgTypes = Arrays.copyOf(values, values.length);
        Arrays.sort(syncPkgTypes);
        sortValues = syncPkgTypes;
    }

    public static SyncPkgType forNumber(byte pkgType){
        if (pkgType<1 || pkgType>sortValues.length){
            return null;
        }
        return sortValues[pkgType-1];
    }
}
