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

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.misc.ProtoBufHashMappingStrategy;
import com.wjybxx.fastjgame.net.common.MessageMapper;
import com.wjybxx.fastjgame.net.common.ProtoBufMessageSerializer;
import com.wjybxx.fastjgame.protobuffer.p_enum;

import static com.wjybxx.fastjgame.protobuffer.p_game_scene.Hello;

/**
 * 测试protoBuf生成的文件是否可用,以及序列化反序列化
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 15:46
 * @github - https://github.com/hl845740757
 */
public class ProtoBufTest {

    public static void main(String[] args) throws Exception {
//        Hello hello=Hello.newBuilder()
//                .setRoleTye(p_enum.RoleType.ROLE_TYPE_CODER)
//                .setName("wjybxx")
//                .build();
//
//        System.out.println(hello.toString());
//
//        // 初始话序列化工具(提供运行时的性能)
//        ProtoBufHashMappingStrategy mappingStrategy = new ProtoBufHashMappingStrategy();
//        MessageMapper messageMapper = new MessageMapper(mappingStrategy.mapping());
//        ProtoBufMessageSerializer serializer = new ProtoBufMessageSerializer();
//        serializer.init(messageMapper);
//
//        byte[] messageBytes = serializer.serialize(hello);
//        Hello message = serializer.deserialize(hello.getClass(), messageBytes);
//
//        System.out.println(message);
    }
}
