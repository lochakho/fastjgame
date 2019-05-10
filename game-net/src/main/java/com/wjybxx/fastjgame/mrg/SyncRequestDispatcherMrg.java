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
import com.wjybxx.fastjgame.net.sync.SyncRequestHandler;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 同步消息处理器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 15:03
 * @github - https://github.com/hl845740757
 */
public class SyncRequestDispatcherMrg {

    private static final Logger logger= LoggerFactory.getLogger(SyncRequestDispatcherMrg.class);

    /**
     * 消息处理器
     */
    private final Map<Class<?>, SyncRequestHandler<?,?>> handlerMap=new IdentityHashMap<>();

    @Inject
    public SyncRequestDispatcherMrg() {
    }

    public <T,R> void registerHandler(Class<T> messageClazz,SyncRequestHandler<T,R> handler){
        if (handlerMap.containsKey(messageClazz)){
            throw new IllegalArgumentException("messageClazz " + messageClazz.getSimpleName() + " already registered.");
        }
        handlerMap.put(messageClazz,handler);
    }

    /**
     * 处理客户端请求
     * @param session 会话信息
     * @param message 请求消息
     * @param <T> 输入类型
     * @param <R> 结果类型
     * @return 出现任何异常，返回null，否则返回对应的处理结果
     */
    public <T,R> R handleRequest(SyncS2CSession session, @Nonnull T message){
        @SuppressWarnings("unchecked")
        SyncRequestHandler<T, R> syncRequestHandler = (SyncRequestHandler<T, R>) handlerMap.get(message.getClass());
        if (null==syncRequestHandler){
            logger.warn("syncRequest {} is not registered",message.getClass().getSimpleName());
            return null;
        }
        try {
            return syncRequestHandler.handle(session, message);
        }catch (Exception e){
            // 同步调用一定要注意，不可以轻易出现异常，同步调用出现异常的破坏性较大，因此使用error级别。
            logger.error("handle syncLogicRequest caught exception.",e);
            return null;
        }
    }
}
