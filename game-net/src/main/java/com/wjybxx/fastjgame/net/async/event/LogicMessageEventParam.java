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

import com.wjybxx.fastjgame.net.async.transferobject.LogicMessageTO;
import com.wjybxx.fastjgame.net.async.transferobject.MessageTO;

import javax.annotation.concurrent.Immutable;

/**
 * 业务逻辑包事件参数
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 12:06
 * @github - https://github.com/hl845740757
 */
@Immutable
public class LogicMessageEventParam implements MessageEventParam {

    /**
     * 会话唯一标识，对方唯一标识。
     * channel上取出来的。
     */
    private final long sessionGuid;
    /**
     * 逻辑包数据
     */
    private final LogicMessageTO logicMessageTO;

    public LogicMessageEventParam(long sessionGuid, LogicMessageTO logicMessageTO) {
        this.sessionGuid = sessionGuid;
        this.logicMessageTO = logicMessageTO;
    }

    @Override
    public long sessionGuid() {
        return sessionGuid;
    }

    @Override
    public LogicMessageTO messageTO() {
        return logicMessageTO;
    }
}
