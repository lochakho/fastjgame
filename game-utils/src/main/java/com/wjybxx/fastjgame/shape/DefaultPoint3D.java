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

/**
 * 默认的支持修改的3D坐标。
 *
 * 建议使用{@link Point3D#newPoint3D()}和{@link Point3D#newPoint3D(float, float, float)}
 * 两个工厂方法代替构造方法创建实例。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:34
 * @github - https://github.com/hl845740757
 */
public class DefaultPoint3D extends Point3D{

    /**
     * x 从左到右
     */
    private float x;
    /**
     * y 从上到下
     */
    private float y;
    /**
     * z 从里到外
     */
    private float z;

    public DefaultPoint3D() {
        this(0,0,0);
    }

    public DefaultPoint3D(float x, float y, float z) {
        this.updateLocation(x,y,z);
    }

    public void updateLocation(float x, float y, float z){
        this.x=x;
        this.y=y;
        this.z=z;
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
    public void setZ(float z) {
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public Point3D unmodifiable() {
        return unmodifiable(this);
    }
}
