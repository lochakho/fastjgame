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

package com.wjybxx.fastjgame.net.async;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * http响应处理器。
 * 所有响应都发生在逻辑线程(游戏世界线程)。
 * 注意查看注释：
 * {@link #onFailure(Call, IOException)}
 * {@link #onResponse(Call, Response)}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/28 19:53
 * @github - https://github.com/hl845740757
 */
public interface OkHttpResponseHandler extends Callback {
    /**
     * @see Callback#onFailure(Call, IOException)
     */
    @Override
    void onFailure(@Nonnull Call call, @Nonnull IOException e);

    /**
     * @see Callback#onResponse(Call, Response)
     */
    @Override
    void onResponse(@Nonnull Call call, @Nonnull Response response) throws IOException;
}
