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
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.sync.event.SyncConnectRequestEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncLogicRequestEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncPingEvent;
import com.wjybxx.fastjgame.net.sync.event.SyncPkgType;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectResponseTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicResponseTO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * 同步RPC调用服务器解码器。
 * 不处理太多逻辑，只管理读顺序。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 19:18
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class ServerSyncRpcCodec extends BaseSyncRpcCodec {

    private final SyncS2CSessionMrg syncS2CSessionMrg;

    private long clientGuid= NetConstants.INVALID_SESSION_ID;

    public ServerSyncRpcCodec(CodecHelper codecHelper, SyncS2CSessionMrg syncS2CSessionMrg) {
        super(codecHelper);
        this.syncS2CSessionMrg = syncS2CSessionMrg;
    }

    /**
     * 是否已收到过客户端的连接请求,主要关系到后续需要使用的clientGuid
     * @return
     */
    private boolean inited(){
        return clientGuid != NetConstants.INVALID_SESSION_ID;
    }

    /**
     * 标记为收到过客户端连接请求了
     * @param clientGuid 客户端请求中的参数
     */
    private void init(long clientGuid) {
        this.clientGuid = clientGuid;
    }

    @Override
    protected void readMsg(ChannelHandlerContext ctx, SyncPkgType pkgType, ByteBuf msg) throws Exception {
        switch (pkgType){
            case SYNC_CONNECT_REQUEST:
                tryReadConnectRequest(ctx,msg);
                break;
            case SYNC_LOGIC_REQUEST:
                tryReadLogicRequest(ctx,msg);
                break;
            case SYNC_PING:
                // ping包没有内容
                tryPublishPingEvent(ctx);
                break;
            default:
                closeCtx(ctx,"unexpected syncPkgType " + pkgType);
                break;
        }
    }

    /**
     * 尝试读取连接请求
     */
    private void tryReadConnectRequest(ChannelHandlerContext ctx, ByteBuf msg) {
        SyncConnectRequestTO connectRequestTO = readConnectRequest(msg);
        SyncConnectRequestEvent syncConnectRequestEvent=new SyncConnectRequestEvent(ctx.channel(), connectRequestTO);
        syncS2CSessionMrg.onRcvConnectRequest(syncConnectRequestEvent);
        // 第一次收到请求时初始化channel
        if (!inited()){
            init(connectRequestTO.getClientGuid());
        }
    }

    /**
     * 尝试读取逻辑请求
     */
    private void tryReadLogicRequest(ChannelHandlerContext ctx, ByteBuf msg) throws IOException {
        if (!inited()){
            closeCtx(ctx,"tryReadLogicRequest,but has not received connect request.");
            return;
        }
        SyncLogicRequestTO syncLogicRequestTO = readLogicRequest(msg);
        SyncLogicRequestEvent syncLogicRequestEvent=new SyncLogicRequestEvent(ctx.channel(), clientGuid,syncLogicRequestTO);
        syncS2CSessionMrg.onRcvLogicRequest(syncLogicRequestEvent);
    }

    /**
     * 尝试发布ping包事件
     */
    private void tryPublishPingEvent(ChannelHandlerContext ctx){
        // 返回一个pong包
        writePingMessage(ctx);
        if (inited()){
            SyncPingEvent syncPingEvent=new SyncPingEvent(ctx.channel(),clientGuid);
            syncS2CSessionMrg.onRcvPingPkg(syncPingEvent);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msgTO, ChannelPromise promise) throws Exception {
        // 按照频率排序
        if (msgTO instanceof SyncLogicResponseTO){
            writeLogicResponse(ctx, (SyncLogicResponseTO) msgTO,promise);
        } else if (msgTO instanceof SyncConnectResponseTO){
            writeConnectResponse(ctx, (SyncConnectResponseTO) msgTO,promise);
        } else {
            super.write(ctx, msgTO, promise);
        }
    }

}
