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
import com.wjybxx.fastjgame.misc.HttpResponseHelper;
import com.wjybxx.fastjgame.net.async.HttpRequestHandler;
import com.wjybxx.fastjgame.net.async.HttpSession;
import com.wjybxx.fastjgame.net.async.transferobject.HttpRequestTO;
import com.wjybxx.fastjgame.net.async.transferobject.OkHttpResponseTO;
import com.wjybxx.fastjgame.trigger.Timer;
import com.wjybxx.fastjgame.utils.CollectionUtils;
import com.wjybxx.fastjgame.utils.NetUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * http消息分发器。
 * 注意：http请求主要是用于GM之类的后台请求的，因此最好在InboundHandler中加过滤。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 19:15
 * @github - https://github.com/hl845740757
 */
public class HttpDispatcherMrg {

    private static final Logger logger= LoggerFactory.getLogger(HttpDispatcherMrg.class);

    private final NetConfigMrg netConfigMrg;
    private final SystemTimeMrg systemTimeMrg;
    /**
     * path->handler
     */
    private final Map<String, HttpRequestHandler> handlerMap =new HashMap<>();
    /**
     * channel->session
     */
    private final Map<Channel,HttpSession> sessionMap=new IdentityHashMap<>();

    @Inject
    public HttpDispatcherMrg(NetConfigMrg netConfigMrg,TimerMrg timerMrg,SystemTimeMrg systemTimeMrg) {
        this.netConfigMrg=netConfigMrg;
        this.systemTimeMrg=systemTimeMrg;
        Timer timer=new Timer(netConfigMrg.httpSessionTimeout()*1000,Integer.MAX_VALUE,this::checkSessionTimeout);
        timerMrg.addTimer(timer,systemTimeMrg.getSystemMillTime());
    }

    /**
     * 注册一个http请求的处理器
     * @param path
     * @param handler
     */
    public void registerHandler(String path,HttpRequestHandler handler){
        Objects.requireNonNull(handler);
        if (handlerMap.containsKey(path)){
            throw new IllegalArgumentException(path);
        }
        handlerMap.put(path,handler);
    }

    /**
     * 分发http请求
     * @param httpRequestTO
     */
    public void handleRequest(final Channel channel, final HttpRequestTO httpRequestTO){
        HttpRequestHandler httpRequestHandler = handlerMap.get(httpRequestTO.getPath());
        if (null == httpRequestHandler){
            logger.warn("unregistered path {}", httpRequestTO.getPath());
            channel.writeAndFlush(HttpResponseHelper.newNotFoundResponse());
            return;
        }

        // 保持session
        HttpSession httpSession= sessionMap.computeIfAbsent(channel,k->new HttpSession(channel));
        httpSession.setSessionTimeout(netConfigMrg.httpSessionTimeout() + systemTimeMrg.getSystemSecTime());

        try {
            httpRequestHandler.handle(httpSession, httpRequestTO.getPath(), httpRequestTO.getParams());
        }catch (Exception e){
            logger.warn("handleHttpRequest caught exception, path={}",httpRequestTO.getPath());
        }
    }

    /**
     * 分发okHttp异步调用结果
     * @param okHttpResponseTO
     */
    public void handleOkHttpResponse(OkHttpResponseTO okHttpResponseTO){
        if (okHttpResponseTO.getCause()!=null){
            okHttpResponseTO.getCallBack().onFailure(okHttpResponseTO.getCall(), okHttpResponseTO.getCause());
        }else {
            try {
                okHttpResponseTO.getCallBack().onResponse(okHttpResponseTO.getCall(), okHttpResponseTO.getResponse());
            }catch (IOException e){
                logger.warn("okHttp onResponse callback caught exception",e);
            } finally {
                okHttpResponseTO.getResponse().close();
            }
        }
    }


    /**
     * 检查session超时
     * @param timer
     */
    private void checkSessionTimeout(Timer timer){
        CollectionUtils.removeIfAndThen(sessionMap,
                (channel, httpSession) -> systemTimeMrg.getSystemSecTime() > httpSession.getSessionTimeout(),
                (channel, httpSession) -> NetUtils.closeQuietly(channel));
    }
}
