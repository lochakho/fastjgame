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

package com.wjybxx.fastjgame.net.async;

import javax.annotation.Nonnull;

/**
 * 作为客户端的一方发来的请求消息。
 * 在连接中，当前world作为服务器角色，另一方作为客户端角色。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 15:55
 * @github - https://github.com/hl845740757
 */
public interface RequestMessageHandler<T> extends MessageHandler<S2CSession,T>{
    /**
     * 当收到客户端发来的请求时。
     * @param session 客户端对应的会话信息
     * @param message 客户端发来的消息
     */
    void handle(S2CSession session, @Nonnull T message);
}
