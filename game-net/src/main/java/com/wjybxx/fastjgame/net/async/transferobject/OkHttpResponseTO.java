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

package com.wjybxx.fastjgame.net.async.transferobject;

import com.wjybxx.fastjgame.net.async.OkHttpResponseHandler;
import com.wjybxx.fastjgame.net.async.event.NetEventParam;
import com.wjybxx.fastjgame.net.common.TransferObject;
import okhttp3.Call;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * okHttpClient的异步响应结果
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/10 8:49
 * @github - https://github.com/hl845740757
 */
public class OkHttpResponseTO implements NetEventParam, TransferObject {

    private final OkHttpResponseHandler callBack;
    private final Call call;
    private final Response response;
    private final IOException cause;

    private OkHttpResponseTO(OkHttpResponseHandler callBack, Call call, Response response, IOException cause) {
        this.callBack = callBack;
        this.call = call;
        this.response = response;
        this.cause = cause;
    }

    public OkHttpResponseHandler getCallBack() {
        return callBack;
    }

    public Call getCall() {
        return call;
    }

    public Response getResponse() {
        return response;
    }

    public IOException getCause() {
        return cause;
    }

    @Override
    public long sessionGuid() {
        throw new UnsupportedOperationException();
    }

    /**
     * 创建一个失败响应
     * @param callBack
     * @param call
     * @param cause 失败原因
     * @return
     */
    public static OkHttpResponseTO newFailResponse(OkHttpResponseHandler callBack, Call call, @Nonnull IOException cause){
        return new OkHttpResponseTO(callBack,call,null,cause);
    }

    /**
     * 创建一个成功响应
     * @param callBack 回调方法
     * @param call 请求参数
     * @param response 响应结果
     * @return
     */
    public static OkHttpResponseTO newSuccessResponse(OkHttpResponseHandler callBack, Call call, @Nonnull Response response){
        return new OkHttpResponseTO(callBack,call,response,null);
    }
}
