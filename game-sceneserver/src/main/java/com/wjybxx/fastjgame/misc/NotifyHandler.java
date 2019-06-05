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

import com.wjybxx.fastjgame.scene.ViewGrid;
import com.wjybxx.fastjgame.scene.gameobject.GameObject;

import java.util.List;

/**
 * 视野通知策略；
 * （来源于之前做项目时做的笔记）
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 22:04
 * @github - https://github.com/hl845740757
 */
@Stateless
public interface NotifyHandler<T extends GameObject> {

    // 我要准备执行的逻辑
    /**
     * 通知gameObject，newVisibleGrids这些格子进入了我的视野；
     *
     * @param gameObject 游戏对象
     * @param newVisibleGrids gameObject当前能看见的新格子
     *                        视野视野list的原因是为了减少构建的消息数；
     */
    void notifyGameObjectOthersIn(T gameObject, List<ViewGrid> newVisibleGrids);
    
    /**
     * 通知gameObject，range这些格子离开了我的视野；
     *
     * @param gameObject 游戏对象
     * @param range gameObject离开了这些格子
     *              视野视野list的原因是为了减少构建的消息数；
     */
    void notifyGameObjectOthersOut(T gameObject,List<ViewGrid> range);

    // 其他人要执行的逻辑
    /**
     * 通知(range)这些格子里的对象，有一个gameObject进入了它们的视野格子
     *
     * @param range 视野格子
     * @param gameObject 进入这些视野格子的对象
     *                   视野视野list的原因是为了减少构建的消息数；
     */
    void notifyOthersGameObjectIn(List<ViewGrid> range, T gameObject);

    /**
     * 通知(range)这些格子里的对象，有一个gameObject离开了它们的视野格子
     *
     * @param range 视野格子
     * @param gameObject 离开这些视野格子的对象
     *                   视野视野list的原因是为了减少构建的消息数；
     */
    void notifyOthersGameObjectOut(List<ViewGrid> range, T gameObject);

}
