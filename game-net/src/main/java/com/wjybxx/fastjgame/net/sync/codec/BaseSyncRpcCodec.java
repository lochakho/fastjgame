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

import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.common.MessageMapper;
import com.wjybxx.fastjgame.net.common.MessageSerializer;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.sync.event.SyncPkgType;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncConnectResponseTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicRequestTO;
import com.wjybxx.fastjgame.net.sync.transferobject.SyncLogicResponseTO;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * 同步消息解码器基类。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/5 19:19
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class BaseSyncRpcCodec extends ChannelDuplexHandler {

    private static final Logger logger= LoggerFactory.getLogger(BaseSyncRpcCodec.class);

    protected final MessageMapper messageMapper;
    protected final MessageSerializer messageSerializer;

    public BaseSyncRpcCodec(CodecHelper codecHelper) {
        this.messageMapper=codecHelper.getMessageMapper();
        this.messageSerializer=codecHelper.getMessageSerializer();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetUtils.setChannelPerformancePreferences(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object byteBuf) throws Exception {
        ByteBuf msg= (ByteBuf) byteBuf;
        try {
            long realSum=msg.readLong();
            long logicSum= NetUtils.calChecksum(msg,msg.readerIndex(),msg.readableBytes());
            if (realSum!=logicSum){
                // 校验和不一致
                closeCtx(ctx,"realSum="+realSum + ", logicSum="+logicSum);
                return;
            }

            byte pkgTypeNumber=msg.readByte();
            SyncPkgType pkgType = SyncPkgType.forNumber(pkgTypeNumber);
            if (null== pkgType){
                // 约定之外的包类型
                closeCtx(ctx,"null==pkgTypeNumber " + pkgTypeNumber);
                return;
            }
            readMsg(ctx,pkgType,msg);
        }finally {
            msg.release();
        }
    }

    /**
     * 子类负责消息内容的读取
     *
     * @param pkgType 消息类型
     * @param msg 消息内容(不包含前面的检验和等字段)
     */
    protected abstract void readMsg(ChannelHandlerContext ctx, SyncPkgType pkgType, ByteBuf msg) throws Exception;

    /**
     * 关闭channel
     */
    protected final void closeCtx(ChannelHandlerContext ctx,String reason){
        logger.warn("close channel by reason of {}",reason);
        NetUtils.closeQuietly(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        closeCtx(ctx,"decode exceptionCaught.");
        logger.info("",cause);
    }

    // region 读写连接请求包
    /**
     * 发送连接请求
     */
    protected final void writeConnectRequest(ChannelHandlerContext ctx, SyncConnectRequestTO requestTO, ChannelPromise promise) {
        int contentLength= 8+8+4+requestTO.getEncryptTokenBytes().length;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength, SyncPkgType.SYNC_CONNECT_REQUEST);

        byteBuf.writeLong(requestTO.getClientGuid());
        byteBuf.writeLong(requestTO.getServerGuid());
        byteBuf.writeInt(requestTO.getSndTokenTimes());
        byteBuf.writeBytes(requestTO.getEncryptTokenBytes());
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 读取连接请求
     */
    protected final SyncConnectRequestTO readConnectRequest(ByteBuf msg) {
        long clientGuid=msg.readLong();
        long serverGuid=msg.readLong();
        int sndTokenTimes=msg.readInt();
        byte[] encryptTokenBytes= NetUtils.readRemainToBytes(msg);

        return new SyncConnectRequestTO(clientGuid,serverGuid,sndTokenTimes,encryptTokenBytes);
    }
    // endregion

    // region 读写连接响应包
    /**
     * 发送连接响应
     */
    protected void writeConnectResponse(ChannelHandlerContext ctx, SyncConnectResponseTO msgTO, ChannelPromise promise){
        int contentLength= 4 + 1 + msgTO.getEncryptTokenBytes().length;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength, SyncPkgType.SYNC_CONNECT_RESPONSE);

        byteBuf.writeInt(msgTO.getSndTokenTimes());
        byteBuf.writeByte(msgTO.isSuccess()?1:0);
        byteBuf.writeBytes(msgTO.getEncryptTokenBytes());
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 读取连接响应
     */
    protected final SyncConnectResponseTO readConnectResponse(ByteBuf msg) {
        int sndTokenTimes=msg.readInt();
        boolean success = msg.readByte() == 1;
        byte[] encryptTokenBytes= NetUtils.readRemainToBytes(msg);

        return new SyncConnectResponseTO(sndTokenTimes, success,encryptTokenBytes);
    }
    // endregion

    // region 读写逻辑请求包
    /**
     * 发送逻辑请求
     */
    protected final void writeLogicRequest(ChannelHandlerContext ctx, SyncLogicRequestTO logicRequestTO, ChannelPromise promise) throws Exception {
        Object message=logicRequestTO.getRequest();
        int messageId=messageMapper.getMessageId(message.getClass());
        byte[] messageBytes=messageSerializer.serialize(message);

        int contentLength= 8 + 4 + messageBytes.length;
        ByteBuf byteBuf= newInitializedByteBuf(ctx,contentLength, SyncPkgType.SYNC_LOGIC_REQUEST);

        byteBuf.writeLong(logicRequestTO.getRequestGuid());
        byteBuf.writeInt(messageId);
        byteBuf.writeBytes(messageBytes);
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 读取逻辑请求包
     */
    protected SyncLogicRequestTO readLogicRequest(ByteBuf msg) throws IOException {
        long requestGuid=msg.readLong();
        int messageId=msg.readInt();
        byte[] messageBytes=NetUtils.readRemainToBytes(msg);

        // 协议id对应的class(这里不捕获异常，等待客户端重发)
        Class<?> messageClazz= messageMapper.getMessageClazz(messageId);
        assert null!=messageClazz:"messageId " + messageId + " clazz not found";
        Object request = messageSerializer.deserialize(messageClazz,messageBytes);

        return new SyncLogicRequestTO(requestGuid,request);
    }
    // endregion

    // region 读写逻辑响应包
    /**
     * 发送逻辑消息响应
     */
    protected final void writeLogicResponse(ChannelHandlerContext ctx, SyncLogicResponseTO msgTO, ChannelPromise promise) throws IOException {
        Object response=msgTO.getResponse();
        // 处理过程中出现异常导致失败
        if (null == response){
            int contentLength= 8;
            ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength, SyncPkgType.SYNC_LOGIC_RESPONSE);
            byteBuf.writeLong(msgTO.getRequestGuid());
            appendSumAndWrite(ctx,byteBuf,promise);
        }else {
            // 处理成功
            int messageId = messageMapper.getMessageId(response.getClass());
            byte[] messageBytes= messageSerializer.serialize(response);

            int contentLength = 8 + 4 + messageBytes.length;
            ByteBuf byteBuf = NetUtils.newInitializedByteBuf(ctx, contentLength, SyncPkgType.SYNC_LOGIC_RESPONSE.pkgType);

            byteBuf.writeLong(msgTO.getRequestGuid());
            byteBuf.writeInt(messageId);
            byteBuf.writeBytes(messageBytes);
            appendSumAndWrite(ctx,byteBuf,promise);
        }
    }

    /**
     * 读取逻辑响应包
     */
    protected final SyncLogicResponseTO readLogicResponse(ByteBuf msg) throws IOException {
        long requestGuid=msg.readLong();
        if (msg.readableBytes()==0){
            return new SyncLogicResponseTO(requestGuid,null);
        }else {
            int messageId=msg.readInt();
            byte[] messageBytes=NetUtils.readRemainToBytes(msg);

            // 协议id对应的class(不捕获异常，等待服务器重发)
            Class<?> messageClazz= messageMapper.getMessageClazz(messageId);
            assert null!=messageClazz:"messageId " + messageId + " clazz not found";
            Object response = messageSerializer.deserialize(messageClazz,messageBytes);
            return new SyncLogicResponseTO(requestGuid,response);
        }
    }
    // endregion

    /**
     * 发送心跳包
     */
    protected final void writePingMessage(ChannelHandlerContext ctx) {
        ByteBuf byteBuf= NetUtils.newInitializedByteBuf(ctx,0, SyncPkgType.SYNC_PING.pkgType);
        NetUtils.appendCheckSum(byteBuf);
        ctx.channel().writeAndFlush(byteBuf);
    }

    // region utils
    /**
     * 创建一个初始化好的byteBuf
     * 设置包总长度 和 校验和
     */
    protected final ByteBuf newInitializedByteBuf(ChannelHandlerContext ctx, int contentLength, SyncPkgType syncPkgType){
        return NetUtils.newInitializedByteBuf(ctx,contentLength, syncPkgType.pkgType);
    }

    /**
     * 添加校验和并发送
     */
    protected final void appendSumAndWrite(ChannelHandlerContext ctx, ByteBuf byteBuf, ChannelPromise promise) {
        NetUtils.appendCheckSum(byteBuf);
        ctx.write(byteBuf,promise);
    }
    // endregion
}
