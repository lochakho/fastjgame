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

import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 逻辑线程待发送的消息对象，它是非线程安全的
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:42
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class Message {
    /**
     * 当前包id
     */
    private final long sequence;
    /**
     * 消息确认超时时间
     * 发送的时候设置超时时间
     */
    private long timeout;

    public Message(long sequence) {
        this.sequence = sequence;
    }

    public long getSequence() {
        return sequence;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * 构建传输对象
     * @return
     * @param ack
     */
    public abstract MessageTO build(long ack);

}
