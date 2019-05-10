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
 * 同步Rpc调用请求事件
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 14:16
 * @github - https://github.com/hl845740757
 */
public abstract class SyncRequestEvent extends SyncEvent {

    public SyncRequestEvent(Channel channel) {
        super(channel);
    }

    /**
     * 获取客户端的唯一标识
     * @return
     */
    public abstract long getClientGuid();

}
