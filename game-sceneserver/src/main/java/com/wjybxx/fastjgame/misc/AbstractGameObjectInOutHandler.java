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

import com.wjybxx.fastjgame.scene.Scene;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;

/**
 * 游戏对象进出场景的骨架实现；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 19:41
 * @github - https://github.com/hl845740757
 */
public abstract class AbstractGameObjectInOutHandler<T extends GameObject> implements GameObjectInOutHandler<T>{

    @Override
    public final void processEnterScene(Scene scene, T gameObject) {
        beforeEnterScene(scene, gameObject);
        enterSceneCore(scene, gameObject);
        afterEnterScene(scene, gameObject);
    }

    /**
     * 进入场景的核心逻辑
     * @param scene gameObject将要进入的场景
     * @param gameObject 场景对象
     */
    private void enterSceneCore(Scene scene, T gameObject){

    }

    /**
     * 在进入场景之前需要进行必要的初始化
     * @param scene gameObject将要进入的场景
     * @param gameObject 场景对象
     */
    protected abstract void beforeEnterScene(Scene scene, T gameObject);

    /**
     * 在成功进入场景之后，可能有额外逻辑
     * @param scene gameObject成功进入的场景
     * @param gameObject 场景对象
     */
    protected abstract void afterEnterScene(Scene scene, T gameObject);


    @Override
    public final void processLeaveScene(Scene scene, T gameObject) {
        beforeLeaveScene(scene, gameObject);
        leaveSceneCore(scene, gameObject);
        afterLeaveScene(scene, gameObject);
    }

    /**
     * 离开场景的核心逻辑(公共逻辑)
     * @param scene gameObject当前所在的场景
     * @param gameObject 场景对象
     */
    private void leaveSceneCore(Scene scene, T gameObject){

    }

    /**
     * 在离开场景之前可能有额外逻辑
     * @param scene gameObject当前所在的场景
     * @param gameObject 场景对象
     */
    protected abstract void beforeLeaveScene(Scene scene, T gameObject);

    /**
     * 在成功离开场景之后可能有额外逻辑
     * @param scene gameobject当前所在的场景
     * @param gameObject 场景对象
     */
    protected abstract  void afterLeaveScene(Scene scene, T gameObject);
}
