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

package com.wjybxx.fastjgame.net.sync.initializer;

import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.sync.codec.ServerSyncRpcCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 服务器不关注读写空闲，因为服务器只关注session超时。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 22:47
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class ServerSyncRpcInitializer extends ChannelInitializer<SocketChannel> {

    private final int maxFrameLength;
    private final CodecHelper codecHelper;
    private final SyncS2CSessionMrg syncS2CSessionMrg;

    public ServerSyncRpcInitializer(int maxFrameLength, CodecHelper codecHelper, SyncS2CSessionMrg syncS2CSessionMrg) {
        this.maxFrameLength = maxFrameLength;
        this.codecHelper = codecHelper;
        this.syncS2CSessionMrg = syncS2CSessionMrg;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 解码流程
        pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,0,4,0,4));
        pipeline.addLast(new ServerSyncRpcCodec(codecHelper, syncS2CSessionMrg));
    }
}
