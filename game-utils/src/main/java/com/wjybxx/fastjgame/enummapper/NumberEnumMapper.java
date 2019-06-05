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

import javax.annotation.Nullable;

/**
 * 数字枚举映射，主要用于在运行期间提高查找效率；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 15:08
 * @github - https://github.com/hl845740757
 */
public interface NumberEnumMapper<T extends NumberEnum>{

    /**
     * 通过数字找到对应的枚举
     * @param number 枚举的唯一编号
     * @return T 如果不存在，则返回null，而不是抛出异常
     */
    @Nullable
    T forNumber(int number);

}
