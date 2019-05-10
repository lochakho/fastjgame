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
 * 服务器消息处理器。
 * 在连接中，当前world作为客户端角色，另一方作为服务端角色。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 15:57
 * @github - https://github.com/hl845740757
 */
public interface ServerMessageHandler<T> extends MessageHandler<C2SSession,T>{
    /**
     * 处理服务器发来的消息
     * @param session 服务器信息
     * @param message 服务器发来的消息
     */
    void handle(C2SSession session, @Nonnull T message);
}
