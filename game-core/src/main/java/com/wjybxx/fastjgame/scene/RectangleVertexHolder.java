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
 * 矩形顶点holder类
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 17:54
 * @github - https://github.com/hl845740757
 */
public class RectangleVertexHolder {

    private final Point2D a;

    private final Point2D b;

    private final Point2D c;

    private final Point2D d;

    public RectangleVertexHolder(Point2D a, Point2D b, Point2D c, Point2D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Point2D getA() {
        return a;
    }

    public Point2D getB() {
        return b;
    }

    public Point2D getC() {
        return c;
    }

    public Point2D getD() {
        return d;
    }
}
