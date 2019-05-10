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

import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.sync.codec.ClientSyncRpcCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 客户端通过检测读写空闲判断是否网络异常，同时保持活性。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 22:46
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class ClientSyncRpcInitializer extends ChannelInitializer<SocketChannel> {

    private final int maxFrameLength;
    private final int pingInterval;
    private final CodecHelper codecHelper;
    private final SyncC2SSessionMrg syncC2SSessionMrg;

    public ClientSyncRpcInitializer(int maxFrameLength, int pingInterval, CodecHelper codecHelper, SyncC2SSessionMrg syncC2SSessionMrg) {
        this.maxFrameLength = maxFrameLength;
        this.pingInterval = pingInterval;
        this.codecHelper = codecHelper;
        this.syncC2SSessionMrg = syncC2SSessionMrg;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,0,4,0,4));
        // 监测读写空闲(2个ping周期内未收到响应信息，表示可能断开连接了)
        pipeline.addLast(new IdleStateHandler(pingInterval*2,pingInterval,0));
        pipeline.addLast(new ClientSyncRpcCodec(codecHelper,syncC2SSessionMrg));
    }
}
