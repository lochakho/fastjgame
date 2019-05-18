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

import com.wjybxx.fastjgame.misc.PlatformType;

/**
 * CenterServer在WarzoneServer信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 14:06
 * @github - https://github.com/hl845740757
 */
public class CenterInWarzoneInfo {

    private final long gameProcessGuid;

    private final PlatformType platformType;

    private final int serverId;

    public CenterInWarzoneInfo(long gameProcessGuid, PlatformType platformType, int serverId) {
        this.gameProcessGuid = gameProcessGuid;
        this.platformType = platformType;
        this.serverId = serverId;
    }

    public long getGameProcessGuid() {
        return gameProcessGuid;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getServerId() {
        return serverId;
    }
}
