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

import com.google.inject.Inject;
import com.wjybxx.fastjgame.net.async.C2SSession;
import com.wjybxx.fastjgame.net.async.ClientMessageHandler;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.net.async.ServerMessageHandler;
import com.wjybxx.fastjgame.net.common.MessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 消息分发器，用于业务逻辑线程。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 22:04
 * @github - https://github.com/hl845740757
 */
public class MessageDispatcherMrg {

    private static final Logger logger= LoggerFactory.getLogger(MessageDispatcherMrg.class);
    /**
     * 收到客户端的消息时的消息处理器(连接是对方发起的)
     */
    private final Map<Class<?>, ClientMessageHandler<?>> clientMessageHandlerMap =new IdentityHashMap<>();
    /**
     * 收到服务器的消息时的消息处理器(连接是我发起的)
     */
    private final Map<Class<?>, ServerMessageHandler<?>> serverMessageHandlerMap =new IdentityHashMap<>();

    @Inject
    public MessageDispatcherMrg() {

    }

    /**
     * 注册一个客户端消息处理器
     * @param messageClazz 消息类必须存在于{@link MessageMapper}
     * @param messageHandler 消息处理器
     * @param <T> 消息的类型
     */
    public <T> void registerClientMessageHandler(Class<T> messageClazz, ClientMessageHandler<? super T> messageHandler){
        if (clientMessageHandlerMap.containsKey(messageClazz)){
            throw new IllegalArgumentException(messageClazz.getSimpleName() + " already registered.");
        }
        clientMessageHandlerMap.put(messageClazz,messageHandler);
    }

    /**
     * 注册一个服务器消息处理器
     * @param messageClazz 消息类型
     * @param messageHandler 消息的处理器
     * @param <T> 消息的类型
     */
    public <T> void registerServerMessageHandler(Class<T> messageClazz, ServerMessageHandler<? super T> messageHandler){
        if (serverMessageHandlerMap.containsKey(messageClazz)){
            throw new IllegalArgumentException(messageClazz.getSimpleName() + " already registered.");
        }
        serverMessageHandlerMap.put(messageClazz,messageHandler);
    }

    /**
     * 处理服务器发来的消息
     * @param session 会话信息
     * @param message 如果解码失败，则可能出现null
     */
    public <T> void handleClientMessage(S2CSession session, @Nullable T message) {
        // 未成功解码的消息，做个记录并丢弃(不影响其它请求)
        if (null==message){
            logger.warn("roleType={} sessionId={} send null message",
                    session.getRoleType(),session.getClientGuid());
            return;
        }
        // 未注册的消息，做个记录并丢弃(不影响其它请求)
        @SuppressWarnings("unchecked")
        ClientMessageHandler<T> messageHandler = (ClientMessageHandler<T>) clientMessageHandlerMap.get(message.getClass());
        if (null==messageHandler){
            logger.warn("roleType={} sessionId={} send unregistered message {}",
                    session.getRoleType(),session.getClientGuid(),message.getClass().getSimpleName());
            return;
        }
        try {
            messageHandler.handle(session,message);
        } catch (Exception e) {
            logger.warn("handle message caught exception,sessionId={},roleType={},message clazz={}",
                    session.getRoleType(),session.getClientGuid(),message.getClass().getSimpleName(),e);
        }
    }
    /**
     * 处理服务器发来的消息
     * @param session 会话消息
     * @param message 消息内容，如果解码失败，则可能为null
     */
    public <T> void handleServerMessage(C2SSession session, @Nullable T message) {
        // 未成功解码的消息，做个记录并丢弃(不影响其它请求)
        if (null==message){
            logger.warn("roleType={} sessionId={} send null message",
                    session.getRoleType(),session.getServerGuid());
            return;
        }
        // 未注册的消息，做个记录并丢弃(不影响其它请求)
        @SuppressWarnings("unchecked")
        ServerMessageHandler<T> messageHandler = (ServerMessageHandler<T>) serverMessageHandlerMap.get(message.getClass());
        if (null==messageHandler){
            logger.warn("roleType={} sessionId={} send unregistered message {}",
                    session.getRoleType(),session.getServerGuid(),message.getClass().getSimpleName());
            return;
        }
        try {
            messageHandler.handle(session,message);
        } catch (Exception e) {
            logger.warn("handle message caught exception,sessionId={},roleType={},message clazz={}",
                    session.getRoleType(),session.getServerGuid(),message.getClass().getSimpleName(),e);
        }
    }

}
