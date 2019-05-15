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

import com.wjybxx.fastjgame.misc.IntSequencer;

import java.util.Set;

/**
 * GameServer在SceneServer中的信息
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 12:30
 * @github - https://github.com/hl845740757
 */
public class GameInSceneInfo {

    /**
     * GameServer进程guid
     */
    private final long gameProcessGuid;
    /**
     * 连接的是哪个服
     */
    private final int serverId;
    /**
     * 我被分配的频道
     */
    private final int channelId;
    /**
     * 配置的期望启动的区域，尽可能的都启动它们，且不启动额外的区域。
     */
    private final Set<SceneRegion> configuredRegions;
    /**
     * 激活的区域；
     * 激活的区域可以比配置的区域多，比如当其它区域服务器宕机时，它可能会承载这部分区域。
     * 如果该进程承载的都是互斥区域，那么新启动的进程也无法分担它的压力。
     * 如果该进程城战的有非互斥区域，那么启动新的带有相同非互斥区域的服务器将降低该进程压力。
     */
    private final Set<SceneRegion> activeRegions;
    /**
     * 在线玩家数量计数器
     */
    private final IntSequencer onlinePlayerSequencer=new IntSequencer(0);

    public GameInSceneInfo(long gameProcessGuid, int serverId, int channelId, Set<SceneRegion> configuredRegions, Set<SceneRegion> activeRegions) {
        this.gameProcessGuid = gameProcessGuid;
        this.serverId = serverId;
        this.channelId = channelId;
        this.configuredRegions = configuredRegions;
        this.activeRegions = activeRegions;
    }

    public long getGameProcessGuid() {
        return gameProcessGuid;
    }

    public int getServerId() {
        return serverId;
    }

    public int getChannelId() {
        return channelId;
    }

    public Set<SceneRegion> getConfiguredRegions() {
        return configuredRegions;
    }

    public Set<SceneRegion> getActiveRegions() {
        return activeRegions;
    }

    public IntSequencer getOnlinePlayerSequencer() {
        return onlinePlayerSequencer;
    }

    public boolean addActiveSceneRegion(SceneRegion sceneRegion){
        return this.activeRegions.add(sceneRegion);
    }
}

