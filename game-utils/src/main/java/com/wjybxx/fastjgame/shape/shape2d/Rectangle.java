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

import com.wjybxx.fastjgame.dsl.CoordinateSystem2D;
import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.shape.RectangleVertexHolder;
import com.wjybxx.fastjgame.utils.MathUtils;

/**
 * 矩形(逆时针)，不一定是平行于坐标轴。
 * 数学题来了：如何在指定朝向画一个矩形
 *
 * 两个工厂方法，有助于方便创建矩形对象：
 * {@link #newRectangle(Point2D, float, float, float)}
 * {@link #newRectangleByCentralAngle(Point2D, float, float, float)}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 0:23
 * @github - https://github.com/hl845740757
 */
public class Rectangle extends Quadrilateral{

    // 缓存值 (宽高 -- 长宽)
    /**
     * 矩形的宽
     */
    private float width;
    /**
     * 矩形的高
     */
    private float height;

    public Rectangle(Point2D a, Point2D b, Point2D c, Point2D d) {
        super(a, b, c, d);
        refreshCache();
    }

    /**
     * @see com.wjybxx.fastjgame.dsl.CoordinateSystem2D#calRectangleVertex(Point2D, float, float, float)
     */
    public static Rectangle newRectangle(Point2D bottomCenter, float direction, float width, float height){
        RectangleVertexHolder vertexHolder = CoordinateSystem2D.calRectangleVertex(bottomCenter, direction, width, height);
        return new Rectangle(vertexHolder.getA(), vertexHolder.getB(), vertexHolder.getC(), vertexHolder.getD());
    }

    /**
     * 通过圆形角朝向创建矩形
     * @param bottomCenter 矩形底边中心点
     * @param centralAngle 矩形朝向，用圆心角表示
     * @param width 矩形宽
     * @param height 矩形高
     * @return
     */
    public static Rectangle newRectangleByCentralAngle(Point2D bottomCenter, float centralAngle, float width, float height){
        return newRectangle(bottomCenter,MathUtils.radAngle(centralAngle),width,height);
    }

    @Override
    public Rectangle redraw(Point2D a, Point2D b, Point2D c, Point2D d) {
        super.redraw(a, b, c, d);
        refreshCache();
        return this;
    }

    /**
     * 通过底部中心点，弧度角朝向和宽高重绘矩形
     * 可参考{@link #newRectangleByCentralAngle(Point2D, float, float, float)}
     * @param bottomCenter 底边中点
     * @param direction 矩形朝向，底部边高所在方向
     * @param width 矩形宽
     * @param height 矩形高
     * @return
     */
    public Rectangle redraw(Point2D bottomCenter, float direction, float width, float height) {
        RectangleVertexHolder vertexHolder = CoordinateSystem2D.calRectangleVertex(bottomCenter, direction, width, height);
        // 不可以对超类的实现做出假设
        return redraw(vertexHolder.getA(), vertexHolder.getB(), vertexHolder.getC(), vertexHolder.getD());
    }

    private void refreshCache(){
        float a = MathUtils.distance(getPointA(),getPointD());
        float b = MathUtils.distance(getPointA(),getPointB());
        if (a >= b){
            this.width = a;
            this.height = b;
        }else {
            this.width = b;
            this.height = a;
        }
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "\na=" + getPointA() +
                "\nb=" + getPointB() +
                "\nc=" + getPointC() +
                "\nd=" + getPointD() +
                "\nwidth=" + getWidth() +
                "\nheight=" + getHeight() +
                "}";
    }

}
