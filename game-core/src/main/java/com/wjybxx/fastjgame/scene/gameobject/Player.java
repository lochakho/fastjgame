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

package com.wjybxx.fastjgame.scene.gameobject;

import com.wjybxx.fastjgame.misc.PlatformType;

/**
 * 玩家对象，也是机器人对象；
 * 暂时先直接继承GameObject；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 16:58
 * @github - https://github.com/hl845740757
 */
public class Player extends GameObject{

    /**
     * 玩家所在的平台
     */
    private PlatformType platformType;
    /**
     * 玩家所在的服，逻辑服；
     * 注册时决定；
     */
    private int serverId;

    /**
     * 玩家当前真正所属的服务器（合服之后的服）；
     * 登录时决定；
     */
    private int actualServerId;

    public Player(long guid) {
        super(guid);
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(PlatformType platformType) {
        this.platformType = platformType;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getActualServerId() {
        return actualServerId;
    }

    public void setActualServerId(int actualServerId) {
        this.actualServerId = actualServerId;
    }

    @Override
    public GameObjectType getObjectType() {
        return GameObjectType.PLAYER;
    }
}
