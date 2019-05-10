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

/**
 * 业务逻辑消息处理器
 * @param <T> 消息类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:05
 * @github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface MessageHandler<Session,T> {

    /**
     * 处理该会话发来的消息
     * @param session 会话信息
     * @param message 业务逻辑消息
     */
    void handle(Session session, T message) throws Exception;
}
