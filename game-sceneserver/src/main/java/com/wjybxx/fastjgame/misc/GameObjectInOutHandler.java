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
 * 游戏对象进出场景接口；
 * 在Scene对象内部进行实现，因为可能涉及大量场景对象之间的交互；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 19:35
 * @github - https://github.com/hl845740757
 */
@Stateless
public interface GameObjectInOutHandler<T extends GameObject> {

    /**
     * 执行游戏对象进入场景逻辑
     * @param gameObject 场景对象
     */
    void processEnterScene(T gameObject);

    /**
     * 执行游戏对象离开场景逻辑
     * @param gameObject 场景对象(命名为t的好处是子类实现的时候，ide生成的名字更加有意义)
     */
    void processLeaveScene(T gameObject);

}
