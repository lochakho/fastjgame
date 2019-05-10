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
import com.wjybxx.fastjgame.misc.AbstractThreadLifeCycleHelper;
import com.wjybxx.fastjgame.net.async.OkHttpResponseHandler;
import com.wjybxx.fastjgame.net.async.event.NetEventType;
import com.wjybxx.fastjgame.net.async.transferobject.OkHttpResponseTO;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * http客户端控制器。
 * 该控制器负责请求和响应的线程安全。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 19:56
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public class HttpClientMrg extends AbstractThreadLifeCycleHelper {

    private static final Logger logger= LoggerFactory.getLogger(HttpClientMrg.class);

    private final NetConfigMrg netConfigMrg;
    private final DisruptorMrg disruptorMrg;
    /**
     * 请注意查看{@link Dispatcher#executorService()}默认创建executorService的方式。
     */
    private final OkHttpClient okHttpClient;

    @Inject
    public HttpClientMrg(NetConfigMrg netConfigMrg, DisruptorMrg disruptorMrg) {
        this.netConfigMrg = netConfigMrg;
        okHttpClient=new OkHttpClient.Builder()
                .callTimeout(netConfigMrg.httpRequestTimeout(), TimeUnit.SECONDS)
                .build();
        this.disruptorMrg = disruptorMrg;
    }

    @Override
    protected void startImp() {
        // nothing
    }

    @Override
    protected void shutdownImp() {
        okHttpClient.dispatcher().cancelAll();
        okHttpClient.dispatcher().executorService().shutdown();
    }

    /**
     * 同步get请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public Response syncGet(String url, Map<String,String> params) throws IOException {
        Request request=new Request.Builder().get().url(buildGetUrl(url, params)).build();
        return okHttpClient.newCall(request).execute();
    }

    /**
     * 异步get请求
     * @param url
     * @param params
     * @param responseHandler
     */
    public void asyncGet(String url, Map<String,String> params, OkHttpResponseHandler responseHandler){
        Request request=new Request.Builder().get().url(buildGetUrl(url, params)).build();
        okHttpClient.newCall(request).enqueue(new PublishEventCallBack(disruptorMrg, responseHandler));
    }

    /**
     * 同步post请求
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public Response syncPost(String url, Map<String,String> params) throws IOException {
        Request request=new Request.Builder().post(buildPostBody(params)).build();
        return okHttpClient.newCall(request).execute();
    }

    /**
     * 异步post请求
     * @param url
     * @param params
     * @param responseHandler
     */
    public void asyncPost(String url, Map<String,String> params, OkHttpResponseHandler responseHandler){
        Request request=new Request.Builder().post(buildPostBody(params)).build();
        okHttpClient.newCall(request).enqueue(new PublishEventCallBack(disruptorMrg, responseHandler));
    }

    /**
     * 发布事件回调
     */
    private static class PublishEventCallBack implements Callback{

        private final DisruptorMrg disruptorMrg;
        private final OkHttpResponseHandler responseHandler;

        private PublishEventCallBack(DisruptorMrg disruptorMrg, OkHttpResponseHandler responseHandler) {
            this.disruptorMrg = disruptorMrg;
            this.responseHandler = responseHandler;
        }

        @Override
        public void onFailure(@Nonnull Call call,@Nonnull IOException e) {
            OkHttpResponseTO okHttpResponseTO = OkHttpResponseTO.newFailResponse(responseHandler, call, e);
            disruptorMrg.publishEvent(null, NetEventType.OK_HTTP_RESPONSE,okHttpResponseTO);
        }

        @Override
        public void onResponse(@Nonnull Call call,@Nonnull Response response) throws IOException {
            OkHttpResponseTO okHttpResponseTO = OkHttpResponseTO.newSuccessResponse(responseHandler, call, response);
            disruptorMrg.publishEvent(null, NetEventType.OK_HTTP_RESPONSE,okHttpResponseTO);
        }
    }

    /**
     * 构建get请求的参数部分
     * @param url
     * @param params
     * @return
     */
    private String buildGetUrl(String url,Map<String,String> params){
        String safeUrl;
        if (url.charAt(url.length()-1) == '?'){
            safeUrl = url;
        }else {
            safeUrl = url + "?";
        }
        StringBuilder builder = new StringBuilder(safeUrl);
        // 是否添加&符号
        boolean appendAnd=false;
        for (Map.Entry<String,String> entry:params.entrySet()){
            if (appendAnd){
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
            appendAnd=true;
        }
        return builder.toString();
    }

    /**
     * 构建post请求的数据部分
     * @param params
     * @return
     */
    private RequestBody buildPostBody(Map<String,String> params){
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String,String> entry:params.entrySet()){
            builder.add(entry.getKey(),entry.getValue());
        }
        return builder.build();
    }
}
