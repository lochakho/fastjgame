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

package com.wjybxx.fastjgame.net.sync.transferobject;

import com.wjybxx.fastjgame.net.common.TransferObject;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * 逻辑消息响应传输对象
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/30 11:47
 * @github - https://github.com/hl845740757
 */
@Immutable
public class SyncLogicResponseTO implements TransferObject {
    /**
     * 请求包的唯一id
     */
    private final long requestGuid;
    /**
     * 响应结果,如果为null表示请求失败
     */
    private final Object response;

    public SyncLogicResponseTO(long requestGuid, Object response) {
        this.requestGuid = requestGuid;
        this.response = response;
    }

    public long getRequestGuid() {
        return requestGuid;
    }

    public @Nullable Object getResponse() {
        return response;
    }
}
