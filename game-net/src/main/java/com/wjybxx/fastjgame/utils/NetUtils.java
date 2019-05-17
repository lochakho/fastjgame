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

package com.wjybxx.fastjgame.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DefaultSocketChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * 网络包工具类
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:28
 * @github - https://github.com/hl845740757
 */
public class NetUtils {

    private static final Logger logger= LoggerFactory.getLogger(NetUtils.class);
    /**
     * 本机内网地址
     */
    private static final String localIp=findLocalIp();
    /**
     * 本机外网地址
     */
    private static final String outerIp=findOuterIp();

    static {
        logger.info("localIp {}",localIp);
        logger.info("outerIp {}",outerIp);
    }

    // close
    private NetUtils() {

    }

    /**
     * 安静的关闭channel,不产生任何影响
     */
    public static void closeQuietly(Channel channel){
        if (null!=channel){
            try{
                channel.close();
            }catch (Exception e){
                // ignore
            }
        }
    }

    /**
     * 安静的关闭future，不产生任何影响
     */
    public static void closeQuietly(ChannelFuture channelFuture){
        if (null != channelFuture) {
            try {
                channelFuture.cancel(true);
                channelFuture.channel().close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 安静的关闭ctx，不产生任何影响
     */
    public static void closeQuietly(ChannelHandlerContext ctx) {
        if (null!=ctx){
            try {
                ctx.close();
            }catch (Exception e){
                // ignore
            }
        }
    }

    /**
     * 计算一个hash值。
     * (目前就是字符串自身的hashcode计算方式)
     * @param str
     * @return
     */
    public static int uniqueHash(String str){
        if (null==str){
            throw new NullPointerException("str");
        }
        int hasCode=0;
        for (int index=0;index<str.length();index++){
            hasCode = 31*hasCode + str.charAt(index);
        }
        return hasCode;
    }

    /**
     * 计算byteBuf指定区域字节的校验和
     * @param byteBuf byteBuf
     * @param offset 偏移量
     * @param length 有效长度，不可越界
     * @return
     */
    public static long calChecksum(ByteBuf byteBuf, int offset, int length){
        long checkSum=0;
        for (int index=offset,end=offset+length;index<end;index++){
            checkSum += (byteBuf.getByte(index) & 255);
        }
        return checkSum;
    }

    /**
     * 获取机器内网ip
     * @return
     */
    public static String getLocalIp(){
        return localIp;
    }

    /**
     * 获取机器外网ip
     * @return
     */
    public static String getOuterIp(){
        return outerIp;
    }

    /**
     * 查找内网ip。
     * @return
     */
    private static String findLocalIp() {
        String hostAddress="127.0.0.1";
        try {
            hostAddress= Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("get localHost caught exception,use {} instead.",hostAddress,e);
        }
        return hostAddress;
    }

    /**
     * 获取本机外网ip，如果不存在则返回内网ip。
     *
     * 个人对此不是很熟，参考自以下文章
     * - http://www.voidcn.com/article/p-rludpgmk-yb.html
     * - https://rainbow702.iteye.com/blog/2066431
     *
     * {@link Inet4Address#isSiteLocalAddress()} 是否是私有网段
     * 10/8 prefix ，172.16/12 prefix ，192.168/16 prefix
     * {@link Inet4Address#isLoopbackAddress()} 是否是本机回环ip (127.x.x.x)
     * {@link Inet4Address#isAnyLocalAddress()} 是否是通配符地址 (0.0.0.0)
     * @return
     * @throws SocketException 并不知晓具体类型
     */
    private static String findOuterIp() {
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!(ip instanceof Inet4Address)){
                        continue;
                    }
                    // 回环地址
                    Inet4Address inet4Address= (Inet4Address) ip;
                    if (inet4Address.isLoopbackAddress()){
                        continue;
                    }
                    // 通配符地址
                    if (inet4Address.isAnyLocalAddress()){
                        continue;
                    }
                    // 私有地址(内网地址)
                    if (ip.isSiteLocalAddress()){
                        continue;
                    }
                    return inet4Address.getHostAddress();
                }
            }
        } catch (SocketException e){
            logger.error("find outerIp caught exception,will use localIp instead.",e);
            return findLocalIp();
        }
        logger.error("can't find outerIp, will use localIp instead.");
        return findLocalIp();
    }

    /**
     * 创建一个初始化好的byteBuf
     * 预分配消息长度 校验和 和包类型字段
     * @param ctx 获取allocator
     * @param contentLength 有效内容长度
     * @param pkgType 包类型
     * @return
     */
    public static ByteBuf newInitializedByteBuf(ChannelHandlerContext ctx, int contentLength, byte pkgType){
        // 消息长度字段 + 校验和 + 包类型
        ByteBuf byteBuf = ctx.alloc().buffer(4 + 8 + 1 + contentLength);
        byteBuf.writeInt(8 + 1 + contentLength);
        byteBuf.writeLong(0);
        byteBuf.writeByte(pkgType);
        return byteBuf;
    }

    /**
     * 添加校验和
     */
    public static void appendCheckSum(ByteBuf byteBuf) {
        long sum= NetUtils.calChecksum(byteBuf,12,byteBuf.readableBytes()-12);
        byteBuf.setLong(4,sum);
    }

    /**
     * 将byteBuf中剩余的字节读取到一个字节数组中。
     * @param byteBuf 方法返回之后 readableBytes == 0
     * @return new instance
     */
    public static byte[] readRemainToBytes(ByteBuf byteBuf){
        byte[] result=new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(result);
        return result;
    }

    /**
     * 设置channel性能偏好
     * @param channel
     */
    public static void setChannelPerformancePreferences(Channel channel) {
        ChannelConfig channelConfig = channel.config();
        if (channelConfig instanceof DefaultSocketChannelConfig){
            DefaultSocketChannelConfig socketChannelConfig= (DefaultSocketChannelConfig) channelConfig;
            socketChannelConfig.setPerformancePreferences(0,1,2);
            socketChannelConfig.setAllocator(PooledByteBufAllocator.DEFAULT);
        }
    }
}
