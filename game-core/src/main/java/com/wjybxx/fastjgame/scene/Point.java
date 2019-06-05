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

/**
 * 游戏中的一个点，也作为向量使用。
 *
 * @param <T> the type of this
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 22:29
 * @github - https://github.com/hl845740757
 */
public interface Point<T extends Point<T>> {

    /**
     * 将自己的坐标修改为和参数坐标点一致
     * @param anotherPoint 另一个与自己同类型的坐标点
     */
    void updateLocation(T anotherPoint);

    /**
     * 返回一个不可修改的视图
     * @return an unmodifiable view
     */
    T unmodifiable();
}
