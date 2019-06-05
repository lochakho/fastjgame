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

package com.wjybxx.fastjgame.scene.gameobject;

import com.wjybxx.fastjgame.scene.Point2D;
import com.wjybxx.fastjgame.scene.ViewGrid;

import javax.annotation.Nonnull;

/**
 * 场景对象顶层类。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 22:48
 * @github - https://github.com/hl845740757
 */
public abstract class GameObject {

    /**
     * 每一个场景对象都有一个唯一的guid。
     */
    private final long guid;
    /**
     * 游戏对象的坐标
     * 先写波2D的，练手AOI
     */
    private final Point2D position=Point2D.newPoint2D();
    /**
     * 对象当前所在的视野格子;
     * 缓存的视野格子，用于降低视野更新频率；
     * 游戏对象进入视野时需要立即初始化，离开后需要删除；
     */
    private ViewGrid viewGrid;

    protected GameObject(long guid) {
        this.guid = guid;
    }

    public long getGuid() {
        return guid;
    }

    public Point2D getPosition() {
        return position;
    }

    @Nonnull
    public ViewGrid getViewGrid() {
        return viewGrid;
    }

    public void setViewGrid(ViewGrid viewGrid) {
        this.viewGrid = viewGrid;
    }

    public abstract GameObjectType getObjectType();
}
