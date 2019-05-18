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

package com.wjybxx.fastjgame.core;

import com.wjybxx.fastjgame.core.node.ZKOnlineCenterNode;
import com.wjybxx.fastjgame.core.nodename.CenterServerNodeName;
import com.wjybxx.fastjgame.misc.PlatformType;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 21:24
 * @github - https://github.com/hl845740757
 */
public class CenterInLoginInfo {
    /**
     * 进程guid
     */
    private final long processGuid;
    /**
     * 平台
     */
    private final PlatformType platformType;
    /**
     * 服id
     */
    private final int serverId;
    /**
     * 使用http与center通信(没必要长链接)
     */
    private final String innerHttpAddress;

    public CenterInLoginInfo(long processGuid, PlatformType platformType, int serverId, String innerHttpAddress) {
        this.processGuid = processGuid;
        this.platformType = platformType;
        this.serverId = serverId;
        this.innerHttpAddress = innerHttpAddress;
    }

    public long getProcessGuid() {
        return processGuid;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getServerId() {
        return serverId;
    }

    public String getInnerHttpAddress() {
        return innerHttpAddress;
    }
}
