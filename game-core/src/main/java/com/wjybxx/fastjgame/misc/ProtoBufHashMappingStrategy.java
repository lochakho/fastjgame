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

package com.wjybxx.fastjgame.misc;

import com.google.protobuf.MessageLite;
import com.wjybxx.fastjgame.net.common.MessageMappingStrategy;
import com.wjybxx.fastjgame.protobuffer.MessageEnum;
import com.wjybxx.fastjgame.utils.ReflectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * protoBuffer消息hash映射策略。
 * 扫描生成的枚举文件，而不必扫描生成的类文件，扫描生成的类文件不友好。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 15:21
 * @github - https://github.com/hl845740757
 */
public class ProtoBufHashMappingStrategy implements MessageMappingStrategy {

    @Override
    public Object2IntMap<Class<?>> mapping() throws Exception {
        Object2IntMap<Class<?>> messageClass2IdMap=new Object2IntOpenHashMap<>();
        for (MessageEnum messageEnum : MessageEnum.values()){
            Class<? extends MessageLite> messageClass = ReflectionUtils.findMessageClass(messageEnum.getJavaPackageName(),
                    messageEnum.getJavaOuterClassName(),
                    messageEnum.getMessageName());

            messageClass2IdMap.put(messageClass,messageEnum.getMessageId());
        }
        return messageClass2IdMap;
    }
}
