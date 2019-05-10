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

import com.wjybxx.fastjgame.net.common.TransferObject;
import jdk.nashorn.internal.ir.annotations.Immutable;

/**
 * 消息传输对象，将要发送的数据安全的传输到IO线程。
 * 通信采用捎带确认机制：一个消息包必须有ack和sequence字段
 *
 * 不可以直接使用Message，会导致线程安全问题
 * 子类实现也必须是不可变对象，保证线程安全。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 9:26
 * @github - https://github.com/hl845740757
 */
@Immutable
public abstract class MessageTO implements TransferObject {

    /**
     * 捎带确认的ack
     */
    private final long ack;
    /**
     * 当前包id
     */
    private final long sequence;

    public MessageTO(long ack, long sequence) {
        this.ack = ack;
        this.sequence = sequence;
    }

    public long getAck() {
        return ack;
    }

    public long getSequence() {
        return sequence;
    }

}
