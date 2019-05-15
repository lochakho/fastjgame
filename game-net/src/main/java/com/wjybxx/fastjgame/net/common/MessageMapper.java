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

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 消息对象映射器，存储消息id到消息类的映射关系。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 13:47
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public final class MessageMapper {
    /**
     * 消息类->消息id的映射
     */
    private final Object2IntMap<Class<?>> messageClazz2IdMap=new Object2IntOpenHashMap<>();
    /**
     * 消息id->消息类的映射
     */
    private final Int2ObjectMap<Class<?>> messageId2ClazzMap=new Int2ObjectOpenHashMap<>();

    public MessageMapper(Object2IntMap<Class<?>> mapper){
        for (Object2IntMap.Entry<Class<?>> entry:mapper.object2IntEntrySet()){
            messageClazz2IdMap.put(entry.getKey(),entry.getIntValue());
            messageId2ClazzMap.put(entry.getIntValue(),entry.getKey());
        }
    }

    /**
     * 通过协议id获取到对应的协议类。
     *
     * @param messageId 消息id
     * @return
     */
    public final Class<?> getMessageClazz(int messageId){
        return messageId2ClazzMap.get(messageId);
    }

    /**
     * 通过协议类获取它对应的协议id。
     *
     * @param messageClazz 消息对应的class
     * @return
     */
    public final int getMessageId(Class<?> messageClazz){
        return messageClazz2IdMap.getInt(messageClazz);
    }

    /**
     * 获取所有的消息类文件
     * @return
     */
    public final Set<Class<?>> getAllMessageClasses(){
        return Collections.unmodifiableSet(messageClazz2IdMap.keySet());
    }

    /**
     * 获取所有的消息映射
     * @return
     */
    public final Object2IntMap<Class<?>> getMessageClazz2IdMap(){
        return Object2IntMaps.unmodifiable(messageClazz2IdMap);
    }
}
