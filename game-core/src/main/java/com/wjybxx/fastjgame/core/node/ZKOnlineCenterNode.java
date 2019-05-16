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

package com.wjybxx.fastjgame.core.node;

/**
 * zookeeper上在线CenterServer节点信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 17:21
 * @github - https://github.com/hl845740757
 */
public class ZKOnlineCenterNode extends ZKOnlineNode{

    /**
     * 进程唯一标识
     */
    private final long processGuid;

    public ZKOnlineCenterNode(String innerTcpAddress, String innerRpcAddress, String innerHttpAddress, long processGuid) {
        super(innerTcpAddress, innerRpcAddress, innerHttpAddress);
        this.processGuid=processGuid;
    }


    public long getProcessGuid() {
        return processGuid;
    }
}
