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

import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import io.netty.channel.Channel;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 19:46
 * @github - https://github.com/hl845740757
 */
public class SyncConnectRequestEvent extends SyncRequestEvent {

    private final SyncConnectRequestTO connectRequestTO;

    public SyncConnectRequestEvent(Channel channel, SyncConnectRequestTO connectRequestTO) {
        super(channel);
        this.connectRequestTO = connectRequestTO;
    }

    public SyncConnectRequestTO getConnectRequestTO() {
        return connectRequestTO;
    }

    public long getClientGuid() {
        return connectRequestTO.getClientGuid();
    }

    public long getSndTokenTimes(){
        return connectRequestTO.getSndTokenTimes();
    }

}
