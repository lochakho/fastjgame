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

import com.wjybxx.fastjgame.net.async.event.NetEventType;
import com.wjybxx.fastjgame.net.async.transferobject.AckPingPongMessageTO;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.ConnectResponseTO;
import com.wjybxx.fastjgame.net.async.transferobject.LogicMessageTO;
import com.wjybxx.fastjgame.net.common.CodecHelper;
import com.wjybxx.fastjgame.net.common.MessageMapper;
import com.wjybxx.fastjgame.net.common.MessageSerializer;
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

/**
 * 最开始时为分离的Encoder和Decoder。
 * 那样的问题是不太容易标记channel双方的guid。
 * (会导致协议冗余字段，或使用不必要的同步{@link io.netty.util.AttributeMap})
 *
 * 使用codec会使得协议更加精炼，性能也更好，此外也方便阅读。
 * 它不是线程安全的，也不可共享。
 *
 * baseCodec作为解码过程的最后一步和编码过程的第一步
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/7 12:26
 * @github - https://github.com/hl845740757
 */
public abstract class BaseCodec extends ChannelDuplexHandler {

    private static final Logger logger= LoggerFactory.getLogger(BaseCodec.class);

    protected final MessageMapper messageMapper;
    protected final MessageSerializer messageSerializer;

    public BaseCodec(CodecHelper codecHelper) {
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
            // 任何编解码出现问题都会在上层消息判断哪里出现问题，这里并不处理channel数据是否异常
            byte pkgTypeNumber=msg.readByte();
            NetEventType netEventType = NetEventType.forNumber(pkgTypeNumber);
            if (null== netEventType){
                // 约定之外的包类型
                closeCtx(ctx,"null==netEventType " + pkgTypeNumber);
                return;
            }
            readMsg(ctx, netEventType,msg);
        }finally {
            // 解码结束，释放资源
            msg.release();
        }
    }

    /**
     * 子类真正的读取数据
     * @param ctx
     * @param netEventType
     * @param msg
     */
    protected abstract void readMsg(ChannelHandlerContext ctx, NetEventType netEventType, ByteBuf msg) throws Exception;

    /**
     * 编码协议1 - 连接请求
     * @param ctx
     * @param msgTO
     * @param promise
     * @throws Exception
     */
    protected final void writeConnectRequest(ChannelHandlerContext ctx, ConnectRequestTO msgTO, ChannelPromise promise) {
        byte[] encryptedToken=msgTO.getTokenBytes();
        int contentLength = 8 + 8 + 4 + 8 + encryptedToken.length;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength,NetEventType.CLIENT_CONNECT_REQUEST);

        byteBuf.writeLong(msgTO.getClientGuid());
        byteBuf.writeLong(msgTO.getServerGuid());
        byteBuf.writeInt(msgTO.getSndTokenTimes());
        byteBuf.writeLong(msgTO.getAck());
        byteBuf.writeBytes(encryptedToken);
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 解码协议1 - 连接请求
     * @param msg
     * @return
     */
    protected final ConnectRequestTO readConnectRequest(ByteBuf msg) {
        long clientGuid=msg.readLong();
        long serverGuid=msg.readLong();
        int sndTokenTimes=msg.readInt();
        long ack=msg.readLong();
        byte[] encryptedToken= NetUtils.readRemainToBytes(msg);

        return new ConnectRequestTO(clientGuid, serverGuid, sndTokenTimes, ack, encryptedToken);
    }

    /**
     * 编码协议2 - 连接响应
     */
    protected final void writeConnectResponse(ChannelHandlerContext ctx, ConnectResponseTO msgTO,ChannelPromise promise) {
        byte[] encryptedToken=msgTO.getEncrytedToken();

        int contentLength = 4 + 1 + 8 + encryptedToken.length;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength,NetEventType.SERVER_CONNECT_RESPONSE);
        byteBuf.writeInt(msgTO.getSndTokenTimes());
        byteBuf.writeByte(msgTO.isSuccess()?1:0);
        byteBuf.writeLong(msgTO.getAck());
        byteBuf.writeBytes(msgTO.getEncrytedToken());
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 解码协议2 - 连接响应
     * @param msg
     * @return
     */
    protected final ConnectResponseTO readConnectResponse(ByteBuf msg) {
        int sndTokenTimes=msg.readInt();
        boolean success=msg.readByte()==1;
        long ack=msg.readLong();
        byte[] encrytedToken=NetUtils.readRemainToBytes(msg);

        return new ConnectResponseTO(sndTokenTimes, success, ack,encrytedToken);
    }

    /**
     * 编码协议3、4 - 业务逻辑包
     * @throws Exception
     */
    protected final void writeLogicMessage(ChannelHandlerContext ctx, LogicMessageTO msgTO,
                                           ChannelPromise promise, NetEventType eventType) throws Exception{
        // 发送的时候不可能为null
        Object message = msgTO.getMessage();
        assert null!=message;
        int messageId = messageMapper.getMessageId(message.getClass());
        byte[] messageBytes= messageSerializer.serialize(message);

        int contentLength = 8 + 8 + 4 + messageBytes.length;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength,eventType);

        byteBuf.writeLong(msgTO.getAck());
        byteBuf.writeLong(msgTO.getSequence());
        byteBuf.writeInt(messageId);
        byteBuf.writeBytes(messageBytes);
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 解析协议3、4 - 业务逻辑包
     * @param msg
     */
    protected final LogicMessageTO readLogicMessage(ByteBuf msg) {
        long ack=msg.readLong();
        long sequence=msg.readLong();
        int messageId=msg.readInt();
        byte[] messageBytes=NetUtils.readRemainToBytes(msg);

        try {
            Class<?> messageClazz= messageMapper.getMessageClazz(messageId);
            assert null!=messageClazz:"messageId " + messageId + " clazz not found";
            Object message = messageSerializer.deserialize(messageClazz,messageBytes);
            return new LogicMessageTO(ack, sequence, message);
        }catch (Exception e){
            // 为了不影响该连接上的其它消息，需要捕获异常
            logger.warn("deserialize messageId {} caught exception",messageId,e);
            return new LogicMessageTO(ack, sequence, null);
        }
    }

    /**
     * 编码协议5、6 - ack心跳包
     */
    protected final void writeAckPingPongMessage(ChannelHandlerContext ctx, AckPingPongMessageTO msgTO,
                                                 ChannelPromise promise, NetEventType eventType) {
        int contentLength = 8 + 8;
        ByteBuf byteBuf = newInitializedByteBuf(ctx, contentLength,eventType);

        byteBuf.writeLong(msgTO.getAck());
        byteBuf.writeLong(msgTO.getSequence());
        appendSumAndWrite(ctx,byteBuf,promise);
    }

    /**
     * 解码协议5、6 - ack心跳包
     * @param msg
     */
    protected final AckPingPongMessageTO readAckPingPongMessage(ByteBuf msg) {
        long ack=msg.readLong();
        long sequence=msg.readLong();
        return new AckPingPongMessageTO(ack, sequence);
    }

    /**
     * 关闭channel
     * @param ctx
     * @param reason
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

    /**
     * 创建一个初始化好的byteBuf
     * 设置包总长度 和 校验和
     * @param ctx
     * @param contentLength
     * @return
     */
    protected final ByteBuf newInitializedByteBuf(ChannelHandlerContext ctx, int contentLength,NetEventType netEventType){
        return NetUtils.newInitializedByteBuf(ctx,contentLength,netEventType.pkgType);
    }

    /**
     * 添加校验和并发送
     * @param ctx
     * @param byteBuf
     * @param promise
     * @throws Exception
     */
    protected final void appendSumAndWrite(ChannelHandlerContext ctx, ByteBuf byteBuf, ChannelPromise promise) {
        NetUtils.appendCheckSum(byteBuf);
        ctx.write(byteBuf,promise);
    }
}
