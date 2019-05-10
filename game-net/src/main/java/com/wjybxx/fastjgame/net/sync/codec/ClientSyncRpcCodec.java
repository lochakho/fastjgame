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

package com.wjybxx.fastjgame.net.sync.codec;

import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.sync.event.SyncConnectResponseEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncLogicResponseEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncPkgType;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectResponseTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicResponseTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * 同步RPC调用客户端解码器。
 * 底层只做简单的判断，别的不处理。
 * 只判断发送连接请求与收到消息之间的顺序。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/30 11:54
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class ClientSyncRpcCodec extends BaseSyncRpcCodec {

    private final SyncC2SSessionMrg syncC2SSessionMrg;
    /**
     * 请求建立连接时的服务器guid
     */
    private long serverGuid = NetConstants.INVALID_SESSION_ID;

    public ClientSyncRpcCodec(CodecHelper codecHelper, SyncC2SSessionMrg syncC2SSessionMrg) {
        super(codecHelper);
        this.syncC2SSessionMrg = syncC2SSessionMrg;
    }

    /**
     * 是否已初始化,主要关系到后续需要使用的serverGuid
     */
    private boolean inited(){
        return serverGuid != NetConstants.INVALID_SESSION_ID;
    }

    /**
     * 初始化
     */
    private void init(long serverGuid){
        this.serverGuid=serverGuid;
    }

    @Override
    protected void readMsg(ChannelHandlerContext ctx, SyncPkgType pkgType, ByteBuf msg) throws Exception {
        switch (pkgType){
                // 建立连接结果
            case SYNC_CONNECT_RESPONSE:
                tryReadConnectResponse(ctx,msg);
                break;
                // 业务逻辑响应
            case SYNC_LOGIC_RESPONSE:
                tryReadLogicResponse(ctx,msg);
                break;
            case SYNC_PING:
                // ping包的返回包
                break;
            default:
                closeCtx(ctx,"unexpected syncPkgType " + pkgType);
                break;
        }
    }

    private void tryReadConnectResponse(ChannelHandlerContext ctx, ByteBuf msg) {
        if (!inited()){
            closeCtx(ctx,"tryReadConnectResponse, but has not send request.");
            return;
        }
        SyncConnectResponseTO syncConnectResponseTO = readConnectResponse(msg);
        syncC2SSessionMrg.onRcvConnectResponse(new SyncConnectResponseEvent(ctx.channel(), serverGuid, syncConnectResponseTO));
    }

    private void tryReadLogicResponse(ChannelHandlerContext ctx, ByteBuf msg) throws IOException {
        if (!inited()){
            closeCtx(ctx,"tryReadConnectResponse, but has not send request.");
            return;
        }
        SyncLogicResponseTO logicResponseTO = readLogicResponse(msg);
        SyncLogicResponseEvent syncLogicResponseEvent = new SyncLogicResponseEvent(ctx.channel(), serverGuid, logicResponseTO);;
        syncC2SSessionMrg.onRcvLogicResponse(syncLogicResponseEvent);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 按照频率排序
        if (msg instanceof SyncLogicRequestTO){
            writeLogicRequest(ctx, (SyncLogicRequestTO) msg,promise);
        }else if (msg instanceof SyncConnectRequestTO){
            tryWriteConnectRequest(ctx, (SyncConnectRequestTO) msg,promise);
        } else {
            super.write(ctx, msg, promise);
        }
    }

    /**
     * 尝试发送连接请求
     */
    private void tryWriteConnectRequest(ChannelHandlerContext ctx, SyncConnectRequestTO requestTO, ChannelPromise promise) throws Exception {
        if (!inited()){
            init(requestTO.getServerGuid());
        }
        writeConnectRequest(ctx, requestTO, promise);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            switch (((IdleStateEvent) evt).state()){
                case WRITER_IDLE:
                    tryWritePingMessage(ctx);
                    break;
                case READER_IDLE:
                    // 响应包超时
                    closeCtx(ctx,"ping timeout");
                    break;
                    default:
                        super.userEventTriggered(ctx,evt);
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 尝试发送心跳包
     */
    private void tryWritePingMessage(ChannelHandlerContext ctx){
        if (inited()){
            writePingMessage(ctx);
        }
    }
}
