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

package com.wjybxx.fastjgame.core.nodename;

/**
 * 单服场景节点名字。
 * 场景服需要不同的名字，场景进程之间没有直接的互斥关系，后启动的可以和先启动的同时存在。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 0:15
 * @github - https://github.com/hl845740757
 */
public class SingleSceneNodeName {

    private final int warzoneId;

    private final int serverId;

    private final long sceneProcessGuid;

    public SingleSceneNodeName(int warzoneId, int serverId, long sceneProcessGuid) {
        this.warzoneId = warzoneId;
        this.serverId = serverId;
        this.sceneProcessGuid = sceneProcessGuid;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    public int getServerId() {
        return serverId;
    }

    public long getSceneProcessGuid() {
        return sceneProcessGuid;
    }

    @Override
    public String toString() {
        return "SingleSceneNodeName{" +
                "warzoneId=" + warzoneId +
                ", serverId=" + serverId +
                ", sceneProcessGuid=" + sceneProcessGuid +
                '}';
    }
}
