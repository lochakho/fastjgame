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

package com.wjybxx.fastjgame.mrg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.inject.Inject;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.net.common.Token;
import com.wjybxx.fastjgame.net.common.TokenEncryptStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * token控制器，用于分配token和token校验。
 * 它可以为别的服务器分配token，不只是服务于当前服务器(当前world)
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:12
 * @github - https://github.com/hl845740757
 */
public class TokenMrg {

    private static final Logger logger= LoggerFactory.getLogger(TokenMrg.class);
    /**
     * 初始验证/登录次数
     */
    private static final int INIT_VERIFIED_TIMES = 0;

    private final NetConfigMrg netConfigMrg;
    private final SystemTimeMrg systemTimeMrg;
    /**
     * token加解密策略，允许自定义的复杂策略。
     * 未来可能会放在IO线程中加解密
     */
    private TokenEncryptStrategy tokenEncryptStrategy =new XOREncryptStrategy();

    @Inject
    public TokenMrg(NetConfigMrg netConfigMrg, SystemTimeMrg systemTimeMrg) {
        this.netConfigMrg = netConfigMrg;
        this.systemTimeMrg = systemTimeMrg;
    }

    /**
     * world启动前期可以赋值
     * @param tokenEncryptStrategy 自定义的token加密策略
     */
    public void setTokenEncryptStrategy(TokenEncryptStrategy tokenEncryptStrategy) {
        this.tokenEncryptStrategy =Objects.requireNonNull(tokenEncryptStrategy);
    }

    /**
     * 为指定双方创建一个再也无法验证成功的token
     * @param clientGuid 客户端guid
     * @param serverGuid 服务器guid
     * @return
     */
    public Token newFailToken(long clientGuid, long serverGuid){
        return new Token(clientGuid, RoleType.INVALID, serverGuid, RoleType.INVALID, -1, -1);
    }

    /**
     * 是否是用于标记失败的token
     * @param token
     * @return
     */
    public boolean isFailToken(Token token){
        return token.getClientRoleType() == RoleType.INVALID
                || token.getServerRoleType()== RoleType.INVALID
                || token.getVerifiedTimes() < 0
                || token.getCreateSecTime() < 0;
    }

    /**
     * 分配一个在指定服务器登录用的token
     * @param clientGuid 客户端的guid，为哪个客户端创建的
     * @param clientRoleType 客户端的角色类型
     * @param serverGuid 服务器的guid，要登录的服务器guid
     * @param serverRoleType 服务器的角色类型
     * @return
     */
    public Token newLoginToken(long clientGuid, RoleType clientRoleType, long serverGuid, RoleType serverRoleType){
        return new Token(clientGuid, clientRoleType, serverGuid, serverRoleType, INIT_VERIFIED_TIMES, systemTimeMrg.getSystemSecTime());
    }

    /**
     * 是否是登录token
     * @param token
     * @return
     */
    public boolean isLoginToken(Token token) {
        return token.getVerifiedTimes() == INIT_VERIFIED_TIMES;
    }

    /**
     * 登录token是否超时了
     * @param token
     * @return
     */
    public boolean isLoginTokenTimeout(Token token){
        return systemTimeMrg.getSystemSecTime() > token.getCreateSecTime() + netConfigMrg.loginTokenTimeout();
    }

    /**
     * 登录成功Token
     * @param token
     * @return
     */
    public Token newLoginSuccessToken(Token token){
        // 默认的验证次数不一定是0，不能简单的+1
        return new Token(token.getClientGuid(), token.getClientRoleType(), token.getServerGuid(), token.getServerRoleType(),
                1, systemTimeMrg.getSystemSecTime());
    }

    /**
     * 是否是相同的token,客户端的token是否和服务器token匹配
     */
    public boolean isSameToken(Token existToken, Token token){
        // 不轻易重写equals方法
        return existToken.getClientGuid() == token.getClientGuid()
                && existToken.getClientRoleType() == token.getClientRoleType()
                && existToken.getServerGuid() == token.getServerGuid()
                && existToken.getServerRoleType() == token.getServerRoleType()
                && existToken.getVerifiedTimes() == token.getVerifiedTimes()
                && existToken.getCreateSecTime() == token.getCreateSecTime();
    }

    /**
     * 断线重连之后，分配下一个token，还可以加入更多参数，额外的信息需要传参
     */
    public Token nextToken(Token token){
        return new Token(token.getClientGuid(), token.getClientRoleType(), token.getServerGuid(), token.getServerRoleType(),
                token.getVerifiedTimes()+1, systemTimeMrg.getSystemSecTime());
    }

    /**
     * 加密token，这里最好有自己的实现。
     * @return
     */
    public byte[] encryptToken(Token token){
        return tokenEncryptStrategy.encryptToken(token);
    }

    /**
     * 解密失败则返回null
     * @param encryptedTokenBytes 加密后的token字节数组
     * @return
     */
    public Token decryptToken(byte[] encryptedTokenBytes){
        try {
            return tokenEncryptStrategy.decryptToken(encryptedTokenBytes);
        }catch (Exception e){
            logger.warn("decryptToken caught exception",e);
            return null;
        }
    }

    /**
     * 默认的异或方式的加解密策略
     */
    private class XOREncryptStrategy implements TokenEncryptStrategy{

        @Override
        public byte[] encryptToken(Token token) {
            Gson gson=new GsonBuilder()
                    .registerTypeAdapter(RoleType.class,RoleTypeSerializerAdapter.DEFAULT_INSTANCE)
                    .create();
            byte[] msgBytes = gson.toJson(token).getBytes(StandardCharsets.UTF_8);
            return xorByteArray(msgBytes,netConfigMrg.getTokenKeyBytes());
        }

        @Override
        public Token decryptToken(byte[] encryptedTokenBytes) throws Exception {
            byte[] msgBytes=xorByteArray(encryptedTokenBytes, netConfigMrg.getTokenKeyBytes());
            Gson gson=new GsonBuilder()
                    .registerTypeAdapter(RoleType.class,RoleTypeSerializerAdapter.DEFAULT_INSTANCE)
                    .create();
            String tokenJson=new String(msgBytes,StandardCharsets.UTF_8);
            return gson.fromJson(tokenJson, Token.class);
        }

        /**
         * 异或两个字节数组，并返回一个新的字节数组，其长度为msgBytes的长度
         * @param msgBytes
         * @param keyBytes
         * @return
         */
        private byte[] xorByteArray(byte[] msgBytes,byte[] keyBytes){
            byte[] resultBytes = new byte[msgBytes.length];
            for (int index=0;index<msgBytes.length;index++){
                resultBytes[index]= (byte)(msgBytes[index] ^ keyBytes[index%keyBytes.length]);
            }
            return resultBytes;
        }
    }

    private static class RoleTypeSerializerAdapter extends TypeAdapter<RoleType> {

        private static final RoleTypeSerializerAdapter DEFAULT_INSTANCE=new RoleTypeSerializerAdapter();

        private RoleTypeSerializerAdapter() {
        }

        @Override
        public void write(JsonWriter out, RoleType value) throws IOException {
            if (null==value){
                out.value(RoleType.INVALID.number);
            }else {
                out.value(value.number);
            }
        }

        @Override
        public RoleType read(JsonReader in) throws IOException {
            return RoleType.forNumber(in.nextInt());
        }
    }
}
