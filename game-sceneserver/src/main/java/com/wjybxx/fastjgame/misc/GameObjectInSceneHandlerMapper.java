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

import com.wjybxx.fastjgame.scene.gameobject.GameObject;

/**
 * 场景对象在场景中的生命周期处理器映射
 * (名字有点长)
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 19:31
 * @github - https://github.com/hl845740757
 */
public class GameObjectInSceneHandlerMapper extends GameObjectHandlerMapper<GameObjectInSceneHandler<?>>{

    @SuppressWarnings("unchecked")
    @Override
    public <U extends GameObject> GameObjectInSceneHandler<U> getHandler(U gameObject) {
        return (GameObjectInSceneHandler<U>) super.getHandler(gameObject);
    }
}
