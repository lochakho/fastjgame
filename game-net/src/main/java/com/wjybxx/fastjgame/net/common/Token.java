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

package com.wjybxx.fastjgame.net.common;

/**
 * Token 客户端请求与服务器建立连接时的验证信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 15:03
 * @github - https://github.com/hl845740757
 */
public class Token {
    // ---------------------不变量--------------------
    /**
     * 客户端的唯一标识
     */
    private final long clientGuid;
    /**
     * 客户端的角色类型
     */
    private final RoleType clientRoleType;

    /**
     * 服务器的唯一标识
     */
    private final long serverGuid;
    /**
     * 服务器角色类型
     */
    private final RoleType serverRoleType;

    // --------------------每次进行Token验证成功时会改变---------
    /**
     * 已验证次数(成功建立链接，登录成功次数)，初始值0
     */
    private final int verifiedTimes;
    /**
     * 创建Token时的时间戳(秒)
     */
    private final int createSecTime;

    /**
     * 不要直接使用构造方法创建，
     * use {@link com.wjybxx.fastjgame.mrg.TokenMrg#newLoginToken(long, RoleType, long, RoleType)} (Token)}
     * use {@link com.wjybxx.fastjgame.mrg.TokenMrg#newFailToken(long, long)} (Token)}
     * use {@link com.wjybxx.fastjgame.mrg.TokenMrg#nextToken(Token)}
     * @param clientGuid 客户端guid
     * @param clientRoleType 客户端角色类型
     * @param serverGuid 服务器guid
     * @param serverRoleType 服务器角色类型
     * @param verifiedTimes 已验证次数
     * @param createSecTime 创建Token时的时间戳(秒)
     */
    public Token(long clientGuid, RoleType clientRoleType, long serverGuid, RoleType serverRoleType, int verifiedTimes, int createSecTime) {
        this.clientGuid = clientGuid;
        this.serverGuid = serverGuid;
        this.clientRoleType = clientRoleType;
        this.serverRoleType = serverRoleType;
        this.verifiedTimes = verifiedTimes;
        this.createSecTime = createSecTime;
    }

    public long getClientGuid() {
        return clientGuid;
    }

    public RoleType getClientRoleType() {
        return clientRoleType;
    }

    public long getServerGuid() {
        return serverGuid;
    }

    public RoleType getServerRoleType(){
        return serverRoleType;
    }

    public int getVerifiedTimes() {
        return verifiedTimes;
    }

    public int getCreateSecTime() {
        return createSecTime;
    }

    @Override
    public String toString() {
        return "Token{" +
                "clientGuid=" + clientGuid +
                ", clientRoleType=" + clientRoleType +
                ", serverGuid=" + serverGuid +
                ", serverRoleType=" + serverRoleType +
                ", verifiedTimes=" + verifiedTimes +
                ", createSecTime=" + createSecTime +
                '}';
    }
}
