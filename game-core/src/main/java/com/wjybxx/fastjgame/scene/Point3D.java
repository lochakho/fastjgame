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

import javax.annotation.concurrent.NotThreadSafe;

/**
 *
 * 默认3D坐标点实现，可修改坐标。
 *
 * 3D游戏中y是高度,为啥y是高？
 * 因为2d引擎和3d引擎一般是共用屏幕所在的平面的，也就是x,y所在的面，那么从里到外只能用z了。
 * 而角色移动的时候是在 x，z所在的平面。
 * （和平时数学课本的xyz不一致，一般书本中z是高）
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 22:30
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class Point3D implements Point<Point3D>{

    public static final Point3D EMPTY = newPoint3D().unmodifiable();

    @Override
    public final void updateLocation(Point3D anotherPoint) {
        this.updateLocation(anotherPoint.getX(), anotherPoint.getY(), anotherPoint.getZ());
    }

    /**
     * 更新坐标
     */
    public abstract void updateLocation(float x, float y, float z);

    public abstract void setX(float x);

    public abstract void setY(float y);

    public abstract void setZ(float z);

    /**
     * 重新绘制坐标，并返回自己，用于复用对象时。
     */
    public final Point3D redraw(float x, float y,float z){
        updateLocation(x,y,z);
        return this;
    }

    /**
     * x 从左到右
     * @return float
     */
    public abstract float getX();

    /**
     * y 从上到下
     * @return
     */
    public abstract float getY();

    /**
     * z 从里到外
     * @return
     */
    public abstract float getZ();

    /**
     * 创建一个默认坐标的3d坐标点
     * @return new instance
     */
    public static Point3D newPoint3D(){
        return new DefaultPoint3D();
    }

    /**
     * 创建一个指定初始值的3d坐标点
     * @param x 初始x
     * @param y 初始y
     * @param z 初始z
     * @return new instance
     */
    public static Point3D newPoint3D(float x,float y,float z){
        return new DefaultPoint3D(x,y,z);
    }

    /**
     * 返回一个不可修改的3d坐标视图
     * @param point3D 原始3d坐标点
     * @return a view of point3d
     */
    static Point3D unmodifiable(Point3D point3D){
        return new UnmodifiablePoint3D(point3D);
    }
}
