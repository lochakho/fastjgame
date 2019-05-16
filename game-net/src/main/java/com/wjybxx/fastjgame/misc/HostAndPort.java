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

package com.wjybxx.fastjgame.misc;

import javax.annotation.Nonnull;

/**
 * 远端信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 18:22
 * @github - https://github.com/hl845740757
 */
public class HostAndPort {
    /**
     * 地址
     */
    private final String host;
    /**
     * 端口
     */
    private final int port;

    public HostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * 转为了特定格式的字符串
     * @return 可解析的字符串
     */
    @Override
    public String toString() {
        return host + ":" + port;
    }

    /**
     * 解析特定格式的address，返回对应的对象
     * @param address 远端地址信息，格式为  host:port
     * @return 返回一个对象
     */
    @Nonnull
    public static HostAndPort parseHostAndPort(@Nonnull String address){
        String[] hostAndPort = address.split(":",2);
        String host=hostAndPort[0];
        int port=Integer.parseInt(hostAndPort[1]);
        return new HostAndPort(host,port);
    }

    /**
     * 将指定的hostAndPort对象转化为可供以后解析的字符串
     * @param hostAndPort 指定实例
     * @return 特定格式的字符串
     */
    @Nonnull
    public static String toString(@Nonnull HostAndPort hostAndPort){
        return hostAndPort.toString();
    }
}
