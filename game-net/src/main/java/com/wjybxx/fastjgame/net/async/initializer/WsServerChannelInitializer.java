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
import com.wjybxx.fastjgame.net.async.codec.ServerCodec;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.async.codec.wb.BinaryWebSocketFrameToBytesDecoder;
import com.wjybxx.fastjgame.net.async.codec.wb.BytesToBinaryWebSocketFrameEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 使用websocket时使用
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:25
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class WsServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * url路径(eg: "http://127.0.0.1:8888/ws" 中的ws)
     */
    private final String websocketPath;
    private final int maxFrameLength;
    private final CodecHelper codecHelper;
    private final DisruptorMrg disruptorMrg;

    public WsServerChannelInitializer(String websocketPath, int maxFrameLength, CodecHelper codecHelper, DisruptorMrg disruptorMrg) {
        this.websocketPath = websocketPath;
        this.maxFrameLength = maxFrameLength;
        this.disruptorMrg = disruptorMrg;
        this.codecHelper = codecHelper;
    }

    /**
     * 解码流程(自上而下)
     * 编码流程(自下而上)
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();

        appendHttpCodec(pipeline);

        appendWebsocketCodec(pipeline);

        appendCustomProtocolCodec(pipeline);
    }

    private void appendHttpCodec(ChannelPipeline pipeline) {
        // http支持 webSocket是建立在http上的
        pipeline.addLast(new HttpServerCodec());
        // http请求和响应可能被分段，利用聚合器将http请求合并为完整的Http请求
        pipeline.addLast(new HttpObjectAggregator(65535));
    }

    private void appendWebsocketCodec(ChannelPipeline pipeline) {
        // websocket 解码流程
        // websocket协议处理器(握手、心跳等)
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath));
        pipeline.addLast(new BinaryWebSocketFrameToBytesDecoder());

        // websocket 编码流程
        // Web socket clients must set this to true to mask payload.
        // Server implementations must set this to false.
        pipeline.addLast(new WebSocket13FrameEncoder(false));
        // 将ByteBuf转换为websocket二进制帧
        pipeline.addLast(new BytesToBinaryWebSocketFrameEncoder());
    }

    private void appendCustomProtocolCodec(ChannelPipeline pipeline) {
        pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,0,4,0,4));
        pipeline.addLast(new ServerCodec(codecHelper,disruptorMrg));
    }
}
