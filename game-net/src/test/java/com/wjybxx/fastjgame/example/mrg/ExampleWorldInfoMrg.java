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

package com.wjybxx.fastjgame.example.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.mrg.WorldInfoMrg;

/**
 * 简单的游戏世界信息控制器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 10:52
 * @github - https://github.com/hl845740757
 */
public abstract class ExampleWorldInfoMrg extends WorldInfoMrg {

    /**
     * 游戏世界全局唯一id
     */
    private long processGuid;

    @Inject
    public ExampleWorldInfoMrg() {

    }

    @Override
    protected void initImp(ConfigWrapper startArgs) {
        processGuid =startArgs.getAsLong("processGuid");
    }

    @Override
    public long processGuid() {
        return processGuid;
    }

}
