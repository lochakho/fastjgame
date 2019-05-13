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

package com.wjybxx.fastjgame.configwrapper;

import javax.annotation.concurrent.Immutable;

/**
 * 空配置文件Wrapper
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 15:23
 * @github - https://github.com/hl845740757
 */
@Immutable
public class EmptyConfigWrapper extends ConfigWrapper {

    public static EmptyConfigWrapper INSTANCE=new EmptyConfigWrapper();

    private EmptyConfigWrapper() {

    }

    @Override
    public String getAsString(String key) {
        return null;
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        return MapConfigWrapper.EMPTY_MAP_WRAPPER;
    }

    @Override
    public String toString() {
        return "EmptyConfigWrapper{}";
    }
}
