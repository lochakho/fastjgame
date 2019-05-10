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
import com.wjybxx.fastjgame.net.async.event.ConnectResponseEventParam;
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

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * 客户端用编解码器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/7 12:10
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class ClientCodec extends BaseCodec {

    private final DisruptorMrg disruptorMrg;
    /**
     * channel所属的serverGuid
     */
    private long serverGuid = NetConstants.INVALID_SESSION_ID;

    public ClientCodec(CodecHelper codecHelper, DisruptorMrg disruptorMrg) {
        super(codecHelper);
        this.disruptorMrg = disruptorMrg;
    }

    /**
     * 是否发送过连接请求。主要关系到后续需要使用的serverGuid
     * @return
     */
    private boolean isInited(){
        return serverGuid != NetConstants.INVALID_SESSION_ID;
    }

    /**
     * 标记为已发送过连接请求
     * @param serverGuid channel所属的serverGuid
     */
    private void init(long serverGuid){
        this.serverGuid=serverGuid;
    }

    // region 编码消息
    @Override
    public void write(ChannelHandlerContext ctx, Object msgTO, ChannelPromise promise) throws Exception {
        // 按出现的几率判断
        if (msgTO instanceof LogicMessageTO){
            // 客户端业务逻辑包
            writeLogicMessage(ctx,(LogicMessageTO) msgTO,promise,NetEventType.CLIENT_LOGIC_MSG);
        }else if (msgTO instanceof AckPingPongMessageTO){
            // 客户端ack-ping包
            writeAckPingPongMessage(ctx,(AckPingPongMessageTO) msgTO,promise,NetEventType.CLIENT_ACK_PING);
        } else if (msgTO instanceof ConnectRequestTO){
            // 连接请求包(token验证包)
            tryWriteConnectRequest(ctx,(ConnectRequestTO) msgTO,promise);
        } else {
            super.write(ctx, msgTO, promise);
        }
    }

    /**
     * 发送协议1之前需要标记channel
     */
    private void tryWriteConnectRequest(ChannelHandlerContext ctx, ConnectRequestTO msgTO, ChannelPromise promise) {
        if (!isInited()){
            // 还未发送过请求
            init(msgTO.getServerGuid());
        }
        writeConnectRequest(ctx,msgTO,promise);
    }

    // endregion

    // region 读取消息
    @Override
    protected void readMsg(ChannelHandlerContext ctx, NetEventType netEventType, ByteBuf msg) throws Exception {
        switch (netEventType){
            case SERVER_CONNECT_RESPONSE:
                tryReadConnectResponse(ctx,msg);
                break;
            case SERVER_ACK_PONG:
                tryReadAckPongMessage(ctx,msg);
                break;
            case SERVER_LOGIC_MSG:
                tryReadServerLogicMessage(ctx,msg);
                break;
            default:
                closeCtx(ctx,"unexpected netEventType " + netEventType);
                break;
        }
    }

    /**
     * 服务器返回的Token验证结果
     * @param ctx
     * @param msg
     */
    private void tryReadConnectResponse(ChannelHandlerContext ctx, ByteBuf msg) {
        if (!isInited()){
            // 还未发送请求，就收到响应了，不应该发生
            closeCtx(ctx,"tryReadConnectResponse, but not send connectRequest!");
            return;
        }
        ConnectResponseTO responseTO = readConnectResponse(msg);
        ConnectResponseEventParam connectResponseParam=new ConnectResponseEventParam(serverGuid,responseTO);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.SERVER_CONNECT_RESPONSE, connectResponseParam);
    }

    /**
     * 服务器返回的ack-pong包
     *
     * @param ctx
     * @param msg
     */
    private void tryReadAckPongMessage(ChannelHandlerContext ctx, ByteBuf msg) {
        if (!isInited()){
            // 还未发送请求，就收ack心跳包，不应该发生
            closeCtx(ctx,"tryReadAckPongMessage, but not send connectRequest!");
            return;
        }
        AckPingPongMessageTO ackPingPongMessage=readAckPingPongMessage(msg);
        AckPingPongEventParam ackPongParam=new AckPingPongEventParam(serverGuid,ackPingPongMessage);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.SERVER_ACK_PONG,ackPongParam);
    }

    /**
     * 服务器发送的业务逻辑消息
     * @param ctx
     * @param msg
     */
    private void tryReadServerLogicMessage(ChannelHandlerContext ctx, ByteBuf msg) throws IOException {
        if (!isInited()){
            // 还未发送请求，就收到业务逻辑消息，不应该发生
            closeCtx(ctx,"tryReadServerLogicMessage, but not send connectRequest!");
            return;
        }
        LogicMessageTO logicMessage = readLogicMessage(msg);
        LogicMessageEventParam logicMessageEventParam =new LogicMessageEventParam(serverGuid, logicMessage);
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.SERVER_LOGIC_MSG, logicMessageEventParam);
    }
    // endregion

}
