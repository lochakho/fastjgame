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

package com.wjybxx.fastjgame.core.nodename;

/**
 * 登录服务器节点名字
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 21:27
 * @github - https://github.com/hl845740757
 */
public class LoginServerNodeName {

    /**
     * 登录服绑定的端口
     */
    private final int port;
    /**
     * 登录服进程guid
     */
    private final long loginProcessGuid;

    public LoginServerNodeName(int port, long loginProcessGuid) {
        this.port = port;
        this.loginProcessGuid = loginProcessGuid;
    }

    public int getPort() {
        return port;
    }

    public long getLoginProcessGuid() {
        return loginProcessGuid;
    }
}
