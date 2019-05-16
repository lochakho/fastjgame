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

/**
 * WorldCore的processGuid通过guidMrg生成
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/13 10:30
 * @github - https://github.com/hl845740757
 */
public abstract class WorldCoreInfoMrg extends WorldInfoMrg{
    /**
     * 游戏世界进程guid
     */
    private long processGuid;

    @Inject
    public WorldCoreInfoMrg(GuidMrg guidMrg) {
        processGuid =guidMrg.generateGuid();
    }

    @Override
    public final long processGuid() {
        return processGuid;
    }
}
