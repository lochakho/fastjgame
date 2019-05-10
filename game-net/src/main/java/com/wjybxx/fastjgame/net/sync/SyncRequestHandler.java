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

package com.wjybxx.fastjgame.net.sync;

import javax.annotation.Nonnull;

/**
 * 同步请求处理器。
 * 必须返回一个结果。
 * @param <T> the type of request
 * @param <R> the type of result
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/30 12:22
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface SyncRequestHandler<T,R> {
    /**
     * 处理同步请求
     * @param session 请求方的会话信息
     * @param request 请求内容
     * @return 必须返回一个非空结果(包含在messageMapper中)
     */
    @Nonnull R handle(SyncS2CSession session, @Nonnull T request);
}
