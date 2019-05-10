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

package com.wjybxx.fastjgame.misc;

import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * http响应帮助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 9:20
 * @github - https://github.com/hl845740757
 */
public final class HttpResponseHelper {

    /**
     * text/plain（纯文本）
     */
    public static final String TEXT_PLAIN="text/plain;charset=utf-8";
    /**
     * text/json（json文本）
     */
    public static final String TEXT_JSON="text/json;charset=utf-8";
    /**
     * text/html（HTML文档）
     */
    public static final String TEXT_HTML="text/html;charset=utf-8";
    /**
     * image/jpeg（JPEG图像）
     */
    public static final String IMAGE_JPG="image/jpeg";
    /**
     * image/png（PNG图像）
     */
    public static final String IMAGE_PNG="image/png";
    /**
     * 非空字节数组
     */
    private static final byte[] NO_EMPTY_BYTES = "".getBytes(StandardCharsets.UTF_8);

    private HttpResponseHelper(){
        // close
    }

    // region 一些辅助方法
    /**
     * 构建字符串内容时的帮助方法
     * @param content
     * @return
     */
    public static ByteBuf buildStringContent(String content){
        Objects.requireNonNull(content);
        return Unpooled.copiedBuffer(content.getBytes(StandardCharsets.UTF_8));
    }
    // endregion

    // region 一些工厂方法

    /**
     * 纯文本字符串响应
     * @param content
     * @return
     */
    public static DefaultFullHttpResponse newStringResponse(String content){
        return newTextResponse(TEXT_PLAIN,content);
    }

    /**
     * html文本响应
     * @param content
     * @return
     */
    public static DefaultFullHttpResponse newHtmlResponse(String content){
        return newTextResponse(TEXT_HTML,content);
    }

    /**
     * json文本响应
     * @param jsonObj
     * @return
     */
    public static DefaultFullHttpResponse newJsonResponse(Object jsonObj){
        String json = new GsonBuilder().create().toJson(jsonObj);
        return newTextResponse(TEXT_JSON,json);
    }

    /**
     * 文本响应
     * @param contentType 内容类型
     * @param content 内容
     * @return
     */
    private static DefaultFullHttpResponse newTextResponse(String contentType,String content){
        ByteBuf byteBuf = buildStringContent(content);
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        HttpHeaders headers = httpResponse.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE,contentType);
        headers.set(HttpHeaderNames.CONTENT_LENGTH,byteBuf.readableBytes());
        return httpResponse;
    }

    /**
     * 错误请求的响应
     * @return
     */
    public static DefaultFullHttpResponse newBadRequestResponse(){
        return newErrorResponse(HttpResponseStatus.BAD_REQUEST);
    }

    /**
     * 不支持的路径请求
     * @return
     */
    public static DefaultFullHttpResponse newNotFoundResponse(){
        return newErrorResponse(HttpResponseStatus.NOT_FOUND);
    }

    /**
     * 错误响应
     * @param status
     * @return
     */
    private static DefaultFullHttpResponse newErrorResponse(HttpResponseStatus status){
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        HttpHeaders headers = httpResponse.headers();
        headers.set(HttpHeaderNames.CONTENT_TYPE,TEXT_PLAIN);
        headers.set(HttpHeaderNames.CONTENT_LENGTH,0);
        return httpResponse;
    }

    /**
     * 重定向响应
     * @return
     */
    public static DefaultFullHttpResponse newRelocationResponse(String url){
        Objects.requireNonNull(url,"url");
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SEE_OTHER);
        HttpHeaders headers = response.headers();
        headers.set(HttpHeaderNames.LOCATION, url);
        headers.set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.CONTENT_TYPE, TEXT_PLAIN);
        headers.set(HttpHeaderNames.CONTENT_LENGTH,0);
        return response;
    }
    // endregion
}
