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

import com.wjybxx.fastjgame.misc.PlatformType;

/**
 * 中心服需要总是生成相同的名字，以使得只有一个center进程存在
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 0:14
 * @github - https://github.com/hl845740757
 */
public class CenterNodeName {
    /**
     * 战区id来自父节点
     */
    private final int warzoneId;
    /**
     * 平台类型
     */
    private final PlatformType platformType;
    /**
     * 服id
     */
    private final int serverId;

    public CenterNodeName(int warzoneId, PlatformType platformType, int serverId) {
        this.platformType = platformType;
        this.warzoneId = warzoneId;
        this.serverId = serverId;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getServerId() {
        return serverId;
    }

    @Override
    public String toString() {
        return "CenterNodeName{" +
                "platformType=" + platformType +
                ", warzoneId=" + warzoneId +
                ", serverId=" + serverId +
                '}';
    }
}
