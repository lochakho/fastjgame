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

package com.wjybxx.fastjgame.net.async.transferobject;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * 业务逻辑包传输对象
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:53
 * @github - https://github.com/hl845740757
 */
@Immutable
public class LogicMessageTO extends MessageTO{

    /**
     * 业务逻辑内容。
     */
    private final Object message;

    public LogicMessageTO(long ack, long sequence, Object message) {
        super(ack, sequence);
        this.message = message;
    }

    public @Nullable Object getMessage() {
        return message;
    }
}
