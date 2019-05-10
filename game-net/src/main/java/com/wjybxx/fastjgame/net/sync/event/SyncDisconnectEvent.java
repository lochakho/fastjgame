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

package com.wjybxx.fastjgame.net.sync.event;

import io.netty.channel.Channel;

/**
 * channel断开连接
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/7 23:04
 * @github - https://github.com/hl845740757
 */
public class SyncDisconnectEvent extends SyncEvent{

    private final long serverGuid;

    public SyncDisconnectEvent(Channel channel, long serverGuid) {
        super(channel);
        this.serverGuid = serverGuid;
    }

    public long getServerGuid() {
        return serverGuid;
    }
}
