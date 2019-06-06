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

package com.wjybxx.fastjgame.shape;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 2D坐标点。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 22:30
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public abstract class Point2D implements Point<Point2D>{

    public static final Point2D EMPTY = newPoint2D().unmodifiable();

    @Override
    public final void updateLocation(Point2D anotherPoint) {
        updateLocation(anotherPoint.getX(),anotherPoint.getY());
    }

    /**
     * 更新坐标
     */
    public abstract void updateLocation(float x, float y);

    public abstract void setX(float x);

    public abstract void setY(float y);

    /**
     * 重新绘制，并返回自己，用于重用对象时。
     * @return this
     */
    public final Point2D redraw(float x, float y){
        updateLocation(x,y);
        return this;
    }

    /**
     * x 从左到右
     * @return float
     */
    public abstract float getX();

    /**
     * y 从上到下
     * @return float
     */
    public abstract float getY();

    /**
     * 创建一个可修改的2d坐标点
     * @return new instance
     */
    public static Point2D newPoint2D(){
        return new DefaultPoint2D();
    }

    /**
     * 创建一个可修改的2d坐标点
     * @param x 初始x
     * @param y 初始y
     * @return new instance
     */
    public static Point2D newPoint2D(float x,float y){
        return new DefaultPoint2D(x,y);
    }

    /**
     * 返回一个不可修改的2d坐标视图
     * @param point2D 原始坐标点
     * @return a view of point2d
     */
    static Point2D unmodifiable(Point2D point2D){
        return new UnmodifiablePoint2D(point2D);
    }
}
