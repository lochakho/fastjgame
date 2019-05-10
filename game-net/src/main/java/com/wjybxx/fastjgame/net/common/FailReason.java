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

package com.wjybxx.fastjgame.net.common;

/**
 * token验证失败原因
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/9 17:03
 * @github - https://github.com/hl845740757
 */
public enum FailReason {
    /**
     * token解析错误，无法解析
     */
    NULL,
    /**
     * 无效的token
     */
    INVALID,
    /**
     * token与请求不匹配
     */
    TOKEN_NOT_MATCH_REQUEST,
    /**
     * 不是登录token
     */
    NOT_LOGIN_TOKEN,
    /**
     * ack校验错误
     */
    ACK,
    /**
     * token超时了
     */
    TOKEN_TIMEOUT,
    /**
     * 同一个channel
     */
    SAME_CHANNEL,
    /**
     * 旧请求
     */
    OLD_REQUEST,
}
