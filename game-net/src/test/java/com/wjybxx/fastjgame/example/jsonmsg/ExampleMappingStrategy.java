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

package com.wjybxx.fastjgame.example.jsonmsg;

import com.wjybxx.fastjgame.example.jsonmsg.ExampleJsonMsg;
import com.wjybxx.fastjgame.net.common.MessageMappingStrategy;
import com.wjybxx.fastjgame.utils.NetUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 基于 反射扫描+ hash 的消息映射。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 13:54
 * @github - https://github.com/hl845740757
 */
public class ExampleMappingStrategy implements MessageMappingStrategy {

    public ExampleMappingStrategy() {
    }

    @Override
    public Object2IntMap<Class<?>> mapping() {
        Object2IntMap<Class<?>> messageClazz2IdMap=new Object2IntOpenHashMap<>();
        // 基于反射扫描，发生在启动时，不必太纠结性能
        Class<?>[] messageClazzArray = ExampleJsonMsg.class.getDeclaredClasses();
        for (Class<?> messageClazz:messageClazzArray){
            int messageId= NetUtils.uniqueHash(messageClazz.getSimpleName());
            messageClazz2IdMap.put(messageClazz,messageId);
        }
        return messageClazz2IdMap;
    }

}
