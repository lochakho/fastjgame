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

import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * google protoBuf 序列化工具。
 * protoBuf 生成的类都有一个静态的parser。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:26
 * @github - https://github.com/hl845740757
 */
public class ProtoBufMessageSerializer implements MessageSerializer {

    private final Map<Class<?>, Parser<?>> parserMap=new IdentityHashMap<>();

    @Override
    public void init(MessageMapper messageMapper) throws Exception{
        for (Class<?> messageClazz:messageMapper.getAllMessageClasses()){
            Parser<?> parser= ReflectionUtils.findParser(messageClazz);
            parserMap.put(messageClazz,parser);
        }
    }

    @Override
    public byte[] serialize(Object message) throws UnsupportedEncodingException {
        if (message instanceof MessageLite){
            return ((MessageLite) message).toByteArray();
        }else {
            throw new UnsupportedEncodingException("not protoBuf class " + message.getClass().getSimpleName());
        }
    }

    @Override
    public <T> T deserialize(Class<T> messageClazz, byte[] messageBytes) throws IOException {
        @SuppressWarnings("unchecked")
        Parser<T> parser = (Parser<T>) parserMap.get(messageClazz);
        if (parser == null){
            throw new UnsupportedEncodingException("unregistered protoBuf class " + messageClazz.getSimpleName());
        }
        return parser.parseFrom(messageBytes);
    }
}
