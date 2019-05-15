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

package com.wjybxx.fastjgame.core;

/**
 * zookeeper在线WarzoneServer节点信息
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 17:22
 * @github - https://github.com/hl845740757
 */
public class ZKOnlineWarzoneNode extends ZKOnlineNode{

    public ZKOnlineWarzoneNode(String tcpAddress, String wsAddress, String httpAddress, String syncRpcAddress) {
        super(tcpAddress, wsAddress, httpAddress, syncRpcAddress);
    }
}
