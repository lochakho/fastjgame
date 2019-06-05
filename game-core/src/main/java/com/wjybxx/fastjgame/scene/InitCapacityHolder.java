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

package com.wjybxx.fastjgame.scene;

/**
 * 游戏对象容器初始化容量信息holder；
 * {@link #newBuilder()}，请使用Builder构建对象；初始化自己需要的数值；
 * {@link GameObjectContainer}
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 21:57
 * @github - https://github.com/hl845740757
 */
public class InitCapacityHolder {

    /**
     * 什么都未指定的holder
     */
    public static final InitCapacityHolder EMPTY = newBuilder().build();

    // 数值大于0的有效
    /**
     * 玩家集合初始值大小
     */
    private final int playerSetInitCapacity;

    /**
     * 宠物集合初始值大小
     */
    private final int petSetInitCapacity;
    /**
     * npc集合初始值大小
     */
    private final int npcSetInitCapacity;

    private InitCapacityHolder(Builder builder) {
        this.playerSetInitCapacity = builder.playerSetInitCapacity;
        this.petSetInitCapacity = builder.petSetInitCapacity;
        this.npcSetInitCapacity = builder.npcSetInitCapacity;
    }

    public int getPlayerSetInitCapacity() {
        return playerSetInitCapacity;
    }

    public int getPetSetInitCapacity() {
        return petSetInitCapacity;
    }

    public int getNpcSetInitCapacity() {
        return npcSetInitCapacity;
    }

    // region builder

    public static Builder newBuilder(){
        return new Builder();
    }

    public static class Builder {

        private int playerSetInitCapacity=0;
        private int petSetInitCapacity=0;
        private int npcSetInitCapacity=0;

        public Builder setPlayerSetInitCapacity(int playerSetInitCapacity) {
            this.playerSetInitCapacity = playerSetInitCapacity;
            return this;
        }

        public Builder setPetSetInitCapacity(int petSetInitCapacity) {
            this.petSetInitCapacity = petSetInitCapacity;
            return this;
        }

        public Builder setNpcSetInitCapacity(int npcSetInitCapacity) {
            this.npcSetInitCapacity = npcSetInitCapacity;
            return this;
        }

        public InitCapacityHolder build() {
            return new InitCapacityHolder(this);
        }
    }

    // endregion
}
