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

import com.wjybxx.fastjgame.net.async.transferobject.AckPingPongMessageTO;
import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * ack心跳包，网络底层使用的。
 * 除了一般概念下心跳包的保活作用以外，还包括ack捎带确认。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:51
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class AckPingPongMessage extends Message{

    public AckPingPongMessage(long sequence) {
        super(sequence);
    }

    @Override
    public MessageTO build(long ack) {
        return new AckPingPongMessageTO(ack,getSequence());
    }
}
