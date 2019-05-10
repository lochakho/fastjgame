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

import com.wjybxx.fastjgame.misc.HttpResponseBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpResponse;

/**
 * http会话信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 9:49
 * @github - https://github.com/hl845740757
 */
public final class HttpSession {
    /**
     * session对应的channel
     */
    private final Channel channel;
    /**
     * 会话超时时间
     */
    private int sessionTimeout;

    public HttpSession(Channel channel) {
        this.channel = channel;
    }

    /**
     * 发送一个响应
     * @param response
     * @return 注意相同的警告，建议使用{@link ChannelFuture#await()} 和{@link ChannelFuture#isSuccess()}
     * 代替{@link ChannelFuture#sync()}
     */
    public ChannelFuture writeAndFlush(HttpResponse response){
        return channel.writeAndFlush(response);
    }

    /**
     * 发送一个响应
     * @param <T> builder自身
     * @param builder 建造者
     * @return  注意相同的警告，建议使用{@link ChannelFuture#await()} 和{@link ChannelFuture#isSuccess()}
     * 代替{@link ChannelFuture#sync()}
     */
    public <T extends HttpResponseBuilder<T>> ChannelFuture writeAndFlush(HttpResponseBuilder<T> builder){
        HttpResponse response = builder.build();
        return writeAndFlush(response);
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
