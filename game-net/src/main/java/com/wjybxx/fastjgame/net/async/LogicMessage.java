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

import com.wjybxx.fastjgame.net.async.transferobject.LogicMessageTO;
import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * 业务逻辑消息，应用层使用的对象。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:50
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class LogicMessage extends Message{

    /**
     * 真正的消息内容
     */
    private final Object message;

    public LogicMessage(long sequence, Object message) {
        super(sequence);
        this.message = message;
    }

    public @Nonnull Object getMessage() {
        return message;
    }

    @Override
    public MessageTO build(long ack) {
        return new LogicMessageTO(ack,getSequence(), getMessage());
    }
}
