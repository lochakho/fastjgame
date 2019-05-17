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

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 10:59
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface TryAcquireFun2<T> {
    /**
     * 尝试申请资源，成功则返回true,否则返回false
     * @param resource 资源
     * @return
     */
    boolean tryAcquire(T resource);
}
