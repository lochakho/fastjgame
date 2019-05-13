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

package com.wjybxx.fastjgame.net.async;

import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;

/**
 * http请求处理器
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 19:16
 * @github - https://github.com/hl845740757
 */
public interface HttpRequestHandler{
    /**
     * 处理Http请求
     * @param httpSession 该http对应的session
     * @param path 注册时的请求路径
     * @param requestParams 请求参数
     */
    void handle(HttpSession httpSession, String path, ConfigWrapper requestParams);
}
