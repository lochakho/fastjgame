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

import com.wjybxx.fastjgame.enummapper.NumberEnum;
import com.wjybxx.fastjgame.enummapper.NumberEnumMapper;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

/**
 * 游戏对象类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 22:59
 * @github - https://github.com/hl845740757
 */
public enum GameObjectType implements NumberEnum {

    /**
     * 玩家
     */
    PLAYER,

    /**
     * 宠物
     */
    PET,

    /**
     * 普通NPC
     */
    NPC,
    ;

    @Override
    public int getNumber() {
        return ordinal();
    }

    private static final NumberEnumMapper<GameObjectType> mapper = ReflectionUtils.indexNumberEnum(values());

    public static GameObjectType forNumber(int number){
        return mapper.forNumber(number);
    }
}
