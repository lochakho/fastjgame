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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.common.MessageMapper;
import com.wjybxx.fastjgame.net.common.MessageMappingStrategy;
import com.wjybxx.fastjgame.net.common.MessageSerializer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Map;

/**
 * CodecHelper管理器。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:01
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public final class CodecHelperMrg {

    private final Map<String, CodecHelper> codecMapper = new HashMap<>();

    @Inject
    public CodecHelperMrg() {

    }

    /**
     * 通过mappingStrategy和serializer注册codec
     * @param name codec的名字
     * @param mappingStrategy 消息映射策略
     * @param messageSerializer 消息序列化方式
     * @throws Exception mapping error , or init exception
     */
    public void registerCodecHelper(String name, MessageMappingStrategy mappingStrategy, MessageSerializer messageSerializer) throws Exception {
        Object2IntMap<Class<?>> mapper = mappingStrategy.mapping();
        MessageMapper messageMapper = new MessageMapper(mapper);
        messageSerializer.init(messageMapper);
        registerCodecHelper(name,new CodecHelper(messageMapper,messageSerializer));
    }

    /**
     * 注册codecHelper
     * @param name
     * @param codecHelper
     */
    public void registerCodecHelper(String name, CodecHelper codecHelper){
        if (codecMapper.containsKey(name)){
            throw new IllegalArgumentException("duplicate codecHelper name "+name);
        }
        codecMapper.put(name,codecHelper);
    }

    /**
     * 获取codecHelper
     * @param name
     * @return
     */
    public CodecHelper getCodecHelper(String name){
        CodecHelper codecHelper = codecMapper.get(name);
        if (null == codecHelper){
            throw new IllegalArgumentException("codecHelper " + name + " is not registered.");
        }
        return codecHelper;
    }

}
