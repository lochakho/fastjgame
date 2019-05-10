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

package com.wjybxx.fastjgame.net.async.codec;

import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.mrg.DisruptorMrg;
import com.wjybxx.fastjgame.net.async.event.AckPingPongEventParam;
import com.wjybxx.fastjgame.net.async.event.LogicMessageEventParam;
import com.wjybxx.fastjgame.net.async.event.NetEventType;
import com.wjybxx.fastjgame.net.async.transferobject.AckPingPongMessageTO;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectResponseTO;
import com.wjybxx.fastjgame.net.async.transferobject.LogicMessageTO;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.io.IOException;

/**
 * 服务端使用的编解码器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/7 13:23
 * @github - https://github.com/hl845740757
 */
public class ServerCodec extends BaseCodec{

    private final DisruptorMrg disruptorMrg;
    /**
     * 缓存的客户端guid
      */
    private long clientGuid= NetConstants.INVALID_SESSION_ID;

    public ServerCodec(CodecHelper codecHelper, DisruptorMrg disruptorMrg) {
        super(codecHelper);
        this.disruptorMrg = disruptorMrg;
    }

    /**
     * 是否已收到过客户端的连接请求,主要关系到后续需要使用的clientGuid
     * @return
     */
    private boolean inited(){
        return clientGuid != NetConstants.INVALID_SESSION_ID;
    }

    /**
     * 标记为已接收过连接请求
     * @param clientGuid
     */
    private void init(long clientGuid){
        this.clientGuid = clientGuid;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 按出现的几率判断
        if (msg instanceof LogicMessageTO){
            // 服务器发送的正式消息
            writeLogicMessage(ctx,(LogicMessageTO) msg,promise,NetEventType.SERVER_LOGIC_MSG);
        }else if (msg instanceof AckPingPongMessageTO){
            // 服务器ack心跳返回消息
            writeAckPingPongMessage(ctx, (AckPingPongMessageTO) msg,promise,NetEventType.SERVER_ACK_PONG);
        } else if (msg instanceof ConnectResponseTO){
            // 请求连接结果(token验证结果)
            writeConnectResponse(ctx,(ConnectResponseTO) msg,promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    // region 读取消息
    @Override
    protected void readMsg(ChannelHandlerContext ctx, NetEventType netEventType, ByteBuf msg) throws Exception {
        switch (netEventType){
            case CLIENT_CONNECT_REQUEST:
                tryReadConnectRequest(ctx,msg);
                break;
            case CLIENT_ACK_PING:
                tryReadAckPingMessage(ctx,msg);
                break;
            case CLIENT_LOGIC_MSG:
                tryReadClientLogicMessage(ctx,msg);
                break;
            default:
                closeCtx(ctx,"unexpected netEventType " + netEventType);
                break;
        }
    }

    /**
     * 客户端请求验证token
     * @param ctx
     * @param msg
     */
    private void tryReadConnectRequest(ChannelHandlerContext ctx, ByteBuf msg){
        ConnectRequestTO connectRequest = readConnectRequest(msg);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.CLIENT_CONNECT_REQUEST,connectRequest);
        if (!inited()){
            init(connectRequest.getClientGuid());
        }
    }

    /**
     * 读取客户端的ack-ping包
     */
    private void tryReadAckPingMessage(ChannelHandlerContext ctx, ByteBuf msg){
        // 还未收到过服务器的连接请求结果
        if (!inited()){
            closeCtx(ctx,"tryReadAckPingMessage,but has not Received ConnectRequest.");
            return;
        }
        AckPingPongMessageTO ackPingPongMessage=readAckPingPongMessage(msg);
        AckPingPongEventParam ackPingParam=new AckPingPongEventParam(clientGuid, ackPingPongMessage);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.CLIENT_ACK_PING, ackPingParam);
    }

    /**
     * 客户端发送的业务逻辑消息
     */
    private void tryReadClientLogicMessage(ChannelHandlerContext ctx, ByteBuf msg) throws IOException {
        // 还未收到过token信息呢，channel不可用
        if (!inited()){
            closeCtx(ctx,"tryReadClientLogicMessage,but has not Received ConnectRequest.");
            return;
        }
        LogicMessageTO logicMessage=readLogicMessage(msg);
        LogicMessageEventParam logicMessageEventParam =new LogicMessageEventParam(clientGuid, logicMessage);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.CLIENT_LOGIC_MSG, logicMessageEventParam);
    }
    // endregion
}
