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

package com.wjybxx.fastjgame.net.common;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.Map;

/**
 * 消息映射策略，自己决定消息类到消息id的映射。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 17:21
 * @github - https://github.com/hl845740757
 */
public interface MessageMappingStrategy {

    /**
     * 进行消息映射，发生在启动时，只会调用一次，不必太纠结性能。
     *
     * @return 消息类->消息id的映射关系
     */
    Object2IntMap<Class<?>> mapping() throws Exception;

}
