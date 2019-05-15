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
 * 远程资源申请(主要抛出的异常不同，本地资源申请只允许抛出中断异常)
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 20:07
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface AcquireRemoteFun<T> {

    /**
     * 申请资源，直到成功或中断
     * @param resource
     * @throws Exception
     */
    void acquire(T resource) throws Exception;
}
