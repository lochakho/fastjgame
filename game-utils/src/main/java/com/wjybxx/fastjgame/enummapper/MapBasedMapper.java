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

package com.wjybxx.fastjgame.enummapper;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import javax.annotation.Nullable;

/**
 * 基于map的映射。
 * 对于枚举值较多或数字取值范围散乱的枚举适合；
 * @param <T>
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 15:49
 * @github - https://github.com/hl845740757
 */
public class MapBasedMapper<T extends NumberEnum> implements NumberEnumMapper<T>{

    private final Int2ObjectMap<T> mapping;

    public MapBasedMapper(Int2ObjectMap<T> mapping) {
        this.mapping = mapping;
    }

    @Nullable
    @Override
    public T forNumber(int number) {
        return mapping.get(number);
    }
}
