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

import com.wjybxx.fastjgame.enummapper.NumberEnum;
import com.wjybxx.fastjgame.enummapper.NumberEnumMapper;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

/**
 * 格子遮挡标记；
 * 没有使用更加高效的int值，因为可读性会较差，而{@link java.util.EnumSet}也是基于位运算的；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 13:30
 * @github - https://github.com/hl845740757
 */
public enum GridObstacle implements NumberEnum {

    /**
     * 遮挡，玩家和NPC都不可以走动
     */
    OBSTACLE(0),

    /**
     * 无限制，任何人都可以走动
     */
    FREE(1),

    /**
     * 安全区
     */
    SAFE_AREA(2)
    ;

    private final int number;

    GridObstacle(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    private static final NumberEnumMapper<GridObstacle> mapper = ReflectionUtils.indexNumberEnum(values());

    public static GridObstacle forNumber(int number){
        return mapper.forNumber(number);
    }

}
