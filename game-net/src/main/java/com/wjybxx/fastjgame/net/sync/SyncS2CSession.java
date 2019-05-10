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

package com.wjybxx.fastjgame.net.sync;

import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * 服务器到客户端之间的会话
 * (接收到的连接请求)
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 22:54
 * @github - https://github.com/hl845740757
 */
public class SyncS2CSession {
    /**
     * 客户端唯一标识
     */
    private final long clientGuid;
    /**
     * 客户端角色类型
     */
    private final RoleType roleType;

    public SyncS2CSession(long clientGuid, RoleType roleType) {
        this.clientGuid = clientGuid;
        this.roleType = roleType;
    }

    public long getClientGuid() {
        return clientGuid;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    @Override
    public String toString() {
        return "SyncS2CSession{" +
                "clientGuid=" + clientGuid +
                ", roleType=" + roleType +
                '}';
    }
}
