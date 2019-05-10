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

package com.wjybxx.fastjgame.constants;

/**
 * 网络模块中的一些常量
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 12:59
 * @github - https://github.com/hl845740757
 */
public final class NetConstants {

    /**
     * 网络包配置文件名字
     */
    public static final String NET_CONFIG_NAME= "net_config.properties";
    /**
     * 无效的sessionId
     */
    public static final long INVALID_SESSION_ID=Long.MIN_VALUE;
    /**
     * 初始请求id
     */
    public static final long INIT_REQUEST_GUID=0;

    private NetConstants() {

    }
}
