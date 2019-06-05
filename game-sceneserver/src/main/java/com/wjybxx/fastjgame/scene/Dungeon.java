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

package com.wjybxx.fastjgame.scene;

import com.wjybxx.fastjgame.config.TemplateSceneConfig;
import com.wjybxx.fastjgame.misc.SceneType;
import com.wjybxx.fastjgame.mrg.SceneWrapper;

/**
 * 副本场景，当玩家退出后销毁；
 * (副本这个词很难翻译的贴切)
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 19:59
 * @github - https://github.com/hl845740757
 */
public abstract class Dungeon extends Scene{

    public Dungeon(long guid, TemplateSceneConfig sceneConfig, SceneWrapper sceneWrapper) {
        super(guid, sceneConfig, sceneWrapper);
    }

    @Override
    public final SceneType sceneType() {
        return SceneType.DUNGEON;
    }
}
