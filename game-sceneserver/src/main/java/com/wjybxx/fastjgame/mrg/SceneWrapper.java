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
 * 封装scene需要的控制器
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 19:33
 * @github - https://github.com/hl845740757
 */
public class SceneWrapper {

    private final SceneSendMrg sendMrg;
    private final MapDataLoadMrg mapDataLoadMrg;

    @Inject
    public SceneWrapper(SceneSendMrg sendMrg, MapDataLoadMrg mapDataLoadMrg) {
        this.sendMrg = sendMrg;
        this.mapDataLoadMrg = mapDataLoadMrg;
    }

    public SceneSendMrg getSendMrg() {
        return sendMrg;
    }

    public MapDataLoadMrg getMapDataLoadMrg() {
        return mapDataLoadMrg;
    }
}
