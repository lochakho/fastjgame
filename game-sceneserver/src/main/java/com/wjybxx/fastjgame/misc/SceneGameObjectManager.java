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

import com.wjybxx.fastjgame.scene.GameObjectContainer;
import com.wjybxx.fastjgame.scene.InitCapacityHolder;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * 对外提供访问场景内当前对象的接口
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:03
 * @github - https://github.com/hl845740757
 */
public class SceneGameObjectManager extends GameObjectContainer {

    public SceneGameObjectManager() {
        this(InitCapacityHolder.EMPTY);
    }

    public SceneGameObjectManager(InitCapacityHolder capacityHolder) {
        super(capacityHolder);
    }
}
