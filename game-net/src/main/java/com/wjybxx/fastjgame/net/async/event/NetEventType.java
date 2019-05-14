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

package com.wjybxx.fastjgame.net.async.event;

import java.util.Arrays;

/**
 * 网络事件类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/26 23:14
 */
public enum NetEventType {

    /**
     * 客户端请求建立链接(验证TOKEN)
     */
    CLIENT_CONNECT_REQUEST((byte) 1),
    /**
     * 服务器通知建立链接结果(TOKEN验证结果)
     */
    SERVER_CONNECT_RESPONSE((byte)2),
    /**
     * 客户端发送给服务器的业务逻辑包，不一定会有返回消息。
     */
    CLIENT_LOGIC_MSG((byte)3),
    /**
     * 服务器发送给客户端的业务逻辑包，也不一定有返回消息。
     */
    SERVER_LOGIC_MSG((byte)4),
    /**
     * 客户端发送给服务器的ack-ping包(必定会返回一条ack-pong消息)
     */
    CLIENT_ACK_PING((byte)5),
    /**
     * 服务器返回给客户端的ack-pong包，对ack-ping包的响应
     */
    SERVER_ACK_PONG((byte)6),
    /**
     * http请求事件
     */
    HTTP_REQUEST((byte)7),
    /**
     * okHttpClient异步调用结果
     */
    OK_HTTP_RESPONSE((byte)8),
    /**
     * 子类自定义事件
     */
    CHILD_CUSTOM_EVENTS((byte)9)
    ;

    public final byte pkgType;
    /**
     * 排序号的枚举数组，方便查找
     */
    private static final NetEventType[] sortValues;

    NetEventType(byte pkgType) {
        this.pkgType = pkgType;
    }

    static {
        NetEventType[] values = NetEventType.values();
        NetEventType[] netEventTypes = Arrays.copyOf(values, values.length);
        Arrays.sort(netEventTypes);
        sortValues = netEventTypes;
    }

    /**
     * 通过网络包中的pkgType找到对应的枚举。
     * @param pkgType
     * @return
     */
    public static NetEventType forNumber(byte pkgType){
        if (pkgType<1 || pkgType>sortValues.length){
            return null;
        }
        return sortValues[pkgType-1];
    }
}
