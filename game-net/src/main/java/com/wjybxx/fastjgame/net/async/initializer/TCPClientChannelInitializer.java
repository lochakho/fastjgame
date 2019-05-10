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

package com.wjybxx.fastjgame.net.async.initializer;

import com.wjybxx.fastjgame.mrg.DisruptorMrg;
import com.wjybxx.fastjgame.net.async.codec.ClientCodec;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 客户端initializer示例
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:20
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class TCPClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final int maxFrameLength;
    private final DisruptorMrg disruptorMrg;
    private final CodecHelper codecHelper;

    public TCPClientChannelInitializer(int maxFrameLength, DisruptorMrg disruptorMrg, CodecHelper codecHelper) {
        this.maxFrameLength = maxFrameLength;
        this.disruptorMrg = disruptorMrg;
        this.codecHelper = codecHelper;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,0,4,0,4));
        pipeline.addLast(new ClientCodec(codecHelper,disruptorMrg));
    }
}
