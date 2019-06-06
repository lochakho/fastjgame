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

package com.wjybxx.fastjgame.shape.shape2d;

import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.shape.RedrawShape;

import javax.annotation.Nonnull;

/**
 * 任意四边形
 * a------- d
 *  \       \
 *   \       \
 *    b-------c
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 14:39
 * @github - https://github.com/hl845740757
 */
public class Quadrilateral implements Shape2D, RedrawShape {

    private final Triangle abc;

    private final Triangle adc;

    public Quadrilateral(Point2D a, Point2D b, Point2D c, Point2D d) {
        this.abc=new Triangle(a, b, c);
        this.adc=new Triangle(a, d, c);
    }

    /**
     * 重新设置坐标，不更新引用，只更新数值
     * {@link Point2D#updateLocation(Point2D)}
     */
    public Quadrilateral redraw(Point2D a, Point2D b, Point2D c, Point2D d){
        this.abc.redraw(a, b, c);
        this.adc.redraw(a, d, c);
        return this;
    }

    /**
     * 拆分成两个三角形而言，对于凹四边形和凸四边形都适用。
     * 对于凸四边形，可以采用向量法(同侧原理)。
     * - https://blog.csdn.net/San_Junipero/article/details/79172260
     * @param point2D 2d平面的一点
     * @return true/false
     */
    @Override
    public boolean hasPoint(@Nonnull Point2D point2D) {
        return abc.hasPoint(point2D) || adc.hasPoint(point2D);
    }

    public Point2D getPointA(){
        return abc.getPointA();
    }

    public Point2D getPointB(){
        return abc.getPointB();
    }

    public Point2D getPointC(){
        return abc.getPointC();
    }

    public Point2D getPointD(){
        return adc.getPointB();
    }

    @Override
    public String toString() {
        return "Quadrilateral{" +
                "\na=" + getPointA() +
                "\nb=" + getPointB() +
                "\nc=" + getPointC() +
                "\nd=" + getPointD() +
                "}";
    }
}
