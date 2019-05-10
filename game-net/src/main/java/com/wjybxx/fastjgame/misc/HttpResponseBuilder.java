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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

/**
 * 用于构建复杂的http响应的建造类。
 * 目前它设计用于逻辑线程。部分操作其实更适合放在IO线程，如json序列化。
 * @param <T> the Type of this
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 22:51
 * @github - https://github.com/hl845740757
 */
public abstract class HttpResponseBuilder<T extends HttpResponseBuilder<T>> {

    protected HttpHeaders httpHeaders=EmptyHttpHeaders.INSTANCE;

    /**
     * 添加一个HttpHeader
     * @param headerName
     * @param headerValue
     * @return
     */
    public T addHttpHeader(CharSequence headerName,Object headerValue){
        if (httpHeaders == EmptyHttpHeaders.INSTANCE){
            httpHeaders = new DefaultHttpHeaders();
        }
        httpHeaders.set(headerName,headerValue);
        return castThis();
    }

    /**
     * 返回类型强转的自身
     * @return 返回具体类型的子类自身
     */
    @SuppressWarnings("unchecked")
    protected final T castThis() {
        return (T) this;
    }

    /**
     * 构建最终响应。
     * 这是一个模板方法(若真不满足需要可重写)。
     * @return
     */
    public HttpResponse build(){
        beforeBuild();

        ByteBuf content = buildContent();
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE,contentType());
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH,content.readableBytes());

        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,responseStatus(),
                content,httpHeaders,new DefaultHttpHeaders(false));
    }

    /**
     * 在开始执行构建之前是否有什么事情要做，比如添加header
     */
    protected abstract void beforeBuild();
    /**
     * 子类指定响应状态
     * @return
     */
    protected abstract HttpResponseStatus responseStatus();

    /**
     * 获取内容类型
     * @return
     */
    protected abstract String contentType();

    /**
     * 构建内容
     * @return
     */
    protected abstract ByteBuf buildContent();
}
