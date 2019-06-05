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
 * 默认2D坐标实现，支持坐标修改。
 *
 * 建议使用{@link Point2D#newPoint2D()} 和 {@link Point2D#newPoint2D(float, float)}
 * 两个工厂方法代替构造方法创建实例。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:32
 * @github - https://github.com/hl845740757
 */
public class DefaultPoint2D extends Point2D{

    private float x;

    private float y;

    public DefaultPoint2D() {
        this(0,0);
    }

    public DefaultPoint2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void updateLocation(float x,float y){
        this.x=x;
        this.y=y;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public Point2D unmodifiable() {
        return unmodifiable(this);
    }

    @Override
    public String toString() {
        return "DefaultPoint2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
