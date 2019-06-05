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
 * 不可修改的3D坐标点，代理对象。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:44
 * @github - https://github.com/hl845740757
 */
public class UnmodifiablePoint3D extends Point3D{

    private final Point3D point3D;

    public UnmodifiablePoint3D(Point3D point3D) {
        this.point3D = point3D;
    }

    @Override
    public float getX() {
        return point3D.getX();
    }

    @Override
    public float getY() {
        return point3D.getY();
    }

    @Override
    public float getZ() {
        return point3D.getZ();
    }

    @Override
    public void updateLocation(float x, float y, float z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setX(float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setY(float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setZ(float z) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Point3D unmodifiable() {
        return this;
    }
}
