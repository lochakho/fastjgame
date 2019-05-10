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

package com.wjybxx.fastjgame.net.async.event;

import com.wjybxx.fastjgame.net.async.transferobject.AckPingPongMessageTO;

import javax.annotation.concurrent.Immutable;

/**
 * ack心跳包事件参数
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 11:59
 * @github - https://github.com/hl845740757
 */
@Immutable
public class AckPingPongEventParam implements MessageEventParam {

    /**
     * 会话唯一id，对方唯一id。
     */
    private final long sessionGuid;
    /**
     * ping包
     */
    private final AckPingPongMessageTO ackPingPongMessage;

    public AckPingPongEventParam(long sessionGuid, AckPingPongMessageTO ackPingPongMessage) {
        this.sessionGuid = sessionGuid;
        this.ackPingPongMessage = ackPingPongMessage;
    }

    public long getSessionGuid() {
        return sessionGuid;
    }

    public AckPingPongMessageTO getAckPingPongMessage() {
        return ackPingPongMessage;
    }

    public long getAck() {
        return ackPingPongMessage.getAck();
    }

    public long getSequence() {
        return ackPingPongMessage.getSequence();
    }

    @Override
    public long sessionGuid() {
        return sessionGuid;
    }

    @Override
    public AckPingPongMessageTO messageTO() {
        return ackPingPongMessage;
    }
}
