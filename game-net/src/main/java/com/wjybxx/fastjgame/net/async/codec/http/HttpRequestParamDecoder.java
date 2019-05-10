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

package com.wjybxx.fastjgame.net.async.codec.http;

import com.wjybxx.fastjgame.utils.configwrapper.MapConfigWrapper;
import com.wjybxx.fastjgame.misc.HttpResponseHelper;
import com.wjybxx.fastjgame.mrg.DisruptorMrg;
import com.wjybxx.fastjgame.net.async.event.NetEventType;
import com.wjybxx.fastjgame.net.async.transferobject.HttpRequestTO;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * http请求参数解码器，为上层提供统一视图
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 17:43
 * @github - https://github.com/hl845740757
 */
public class HttpRequestParamDecoder extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger= LoggerFactory.getLogger(HttpRequestParamDecoder.class);

    private final DisruptorMrg disruptorMrg;

    public HttpRequestParamDecoder(DisruptorMrg disruptorMrg) {
        super(true);
        this.disruptorMrg = disruptorMrg;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        DecoderResult decoderResult = msg.decoderResult();
        if (!decoderResult.isSuccess()){
            ctx.writeAndFlush(HttpResponseHelper.newBadRequestResponse())
                    .addListener(ChannelFutureListener.CLOSE);
            logger.warn("decode failed.");
            return;
        }
        HttpMethod method = msg.method();
        // 仅限get和post请求
        if (method != HttpMethod.GET && method !=HttpMethod.POST){
            ctx.writeAndFlush(HttpResponseHelper.newBadRequestResponse())
                    .addListener(ChannelFutureListener.CLOSE);
            logger.info("unsupported method {}",method);
            return;
        }

        QueryStringDecoder queryStringDecoder=new QueryStringDecoder(msg.uri());
        String path=queryStringDecoder.path();
        Map<String,String> paramsMap=new LinkedHashMap<>();

        if (method==HttpMethod.GET){
            for (Map.Entry<String, List<String>> entry:queryStringDecoder.parameters().entrySet()){
                paramsMap.put(entry.getKey(),entry.getValue().get(0));
            }
        } else {
            // You <strong>MUST</strong> call {@link #destroy()} after completion to release all resources.
            HttpPostRequestDecoder postRequestDecoder = new HttpPostRequestDecoder(msg);
            try {
                for (InterfaceHttpData httpData:postRequestDecoder.getBodyHttpDatas()){
                    if (httpData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute){
                        Attribute attribute= (Attribute) httpData;
                        paramsMap.put(attribute.getName(),attribute.getValue());
                    }
                }
            }finally {
                postRequestDecoder.destroy();
            }
        }
        HttpRequestTO httpRequestTO =new HttpRequestTO(path,new MapConfigWrapper(paramsMap));
        disruptorMrg.publishEvent(ctx.channel(), NetEventType.HTTP_REQUEST,httpRequestTO);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        logger.info("",cause);
    }
}
