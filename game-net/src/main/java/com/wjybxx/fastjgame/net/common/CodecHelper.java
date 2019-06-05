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

/**
 * 编解码器帮助类，对{@link MessageMapper} 和 {@link MessageSerializer} 进行绑定。
 *
 * 它持有的{@link MessageMapper}为不可变对象，{@link MessageSerializer}为事实不可变对象，
 * 它自身是<b>事实不可变对象</b>，因此它不是线程安全的；
 * 事实不可变对象需要安全的发布才能保证线程安全；
 *
 * （全部域都是final不代表是不可变对象，引用的对象可能是可变的）
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 10:47
 * @github - https://github.com/hl845740757
 */
public final class CodecHelper {
    /**
     * 消息映射器
     */
    private final MessageMapper messageMapper;
    /**
     * 消息序列化器
     */
    private final MessageSerializer messageSerializer;

    public CodecHelper(MessageMapper messageMapper, MessageSerializer messageSerializer) {
        this.messageMapper = messageMapper;
        this.messageSerializer = messageSerializer;
    }

    public MessageMapper getMessageMapper() {
        return messageMapper;
    }

    public MessageSerializer getMessageSerializer() {
        return messageSerializer;
    }
}
