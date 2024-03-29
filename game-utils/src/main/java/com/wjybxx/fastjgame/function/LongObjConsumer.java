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

package com.wjybxx.fastjgame.function;

import java.util.Objects;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/9 12:11
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface LongObjConsumer<V> {

    void accept(long k, V v);

    default LongObjConsumer<V> andThen(LongObjConsumer<? super V> after) {
        Objects.requireNonNull(after);
        return (k, v) -> { accept(k, v); after.accept(k, v); };
    }
}
