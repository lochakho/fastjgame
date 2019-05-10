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
import com.wjybxx.fastjgame.net.async.codec.wb.BinaryWebSocketFrameToBytesDecoder;
import com.wjybxx.fastjgame.net.async.codec.wb.BytesToBinaryWebSocketFrameEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import javax.annotation.concurrent.ThreadSafe;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 使用websocket时使用
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 10:59
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class WsClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 触发升级为websocket的url (eg: http://localhost:8088/ws)
     */
    private final String websocketUrl;
    private final int maxFrameLength;
    private final DisruptorMrg disruptorMrg;
    private final CodecHelper codecHelper;

    public WsClientChannelInitializer(String websocketUrl, int maxFrameLength, DisruptorMrg disruptorMrg, CodecHelper codecHelper) {
        this.websocketUrl = websocketUrl;
        this.maxFrameLength = maxFrameLength;
        this.disruptorMrg = disruptorMrg;
        this.codecHelper = codecHelper;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();

        appendHttpCodec(pipeline);

        appendWebsocketCodec(pipeline);

        appendCustomProtocolCodec(pipeline);
    }

    /**
     * http协议支持
     * @param pipeline
     */
    private void appendHttpCodec(ChannelPipeline pipeline) {
        // http支持 webSocket是建立在http上的
        pipeline.addLast(new HttpClientCodec());
        // http请求和响应可能被分段，利用聚合器将http请求合并为完整的Http请求
        pipeline.addLast(new HttpObjectAggregator(65535));
    }

    /**
     * websocket协议支持
     * @param pipeline
     * @throws URISyntaxException
     */
    private void appendWebsocketCodec(ChannelPipeline pipeline) throws URISyntaxException {
        // websocket 解码流程
        URI uri=new URI(websocketUrl);
        pipeline.addLast(new WebSocketClientProtocolHandler(uri, WebSocketVersion.V13,
                null,true,new DefaultHttpHeaders(),maxFrameLength));
        pipeline.addLast(new BinaryWebSocketFrameToBytesDecoder());

        // websocket 编码流程
        // Web socket clients must set this to true to mask payload.
        // Server implementations must set this to false.
        pipeline.addLast(new WebSocket13FrameEncoder(true));
        // 将ByteBuf转换为websocket二进制帧
        pipeline.addLast(new BytesToBinaryWebSocketFrameEncoder());
    }

    /**
     * 自定义二进制协议支持
     * @param pipeline
     */
    private void appendCustomProtocolCodec(ChannelPipeline pipeline) {
        pipeline.addLast(new LengthFieldBasedFrameDecoder(maxFrameLength,0,4,0,4));
        pipeline.addLast(new ClientCodec(codecHelper,disruptorMrg));
    }
}
