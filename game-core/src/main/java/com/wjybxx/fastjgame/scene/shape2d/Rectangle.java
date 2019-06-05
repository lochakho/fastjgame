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

package com.wjybxx.fastjgame.scene.shape2d;

import com.wjybxx.fastjgame.scene.Point2D;
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
     * <pre>
     *         direction
     *            |
     *            |
     *            |
     *   ......bottom......
     * </pre>
     * 实现上两种方式，一种可读性好，一种性能好
     * <pre>
     *     {@code
     *         // 左右旋转90° 获得 a b两点 左加右减(可读性是很好的)
     *         Point2D a = MathUtils.directionPoint(bottomCenter,MathUtils.radAngleAdd(direction,MathUtils.HALF_PI),width/2);
     *         Point2D b = MathUtils.directionPoint(bottomCenter,MathUtils.radAngleSub(direction,MathUtils.HALF_PI),width/2);
     *
     *         // 由b得到c a得到d
     *         Point2D c = MathUtils.directionPoint(b,direction,height);
     *         Point2D d = MathUtils.directionPoint(a,direction,height);
     *     }
     * </pre>
     *
     * 通过弧度角朝向创建矩形
     * @param bottomCenter 矩形底部中心点
     * @param direction 矩形朝向(底部边的高所在方向)，弧度角
     * @param width 矩形的宽
     * @param height 矩形的高
     * @return
     */
    public static Rectangle newRectangle(Point2D bottomCenter, float direction, float width, float height){
        // 减少MathUtils.directionPoint调用，计算cos和sin的消耗
        // 最好拿纸画一下图，这个理解难度高不少
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);

        float widthDX = (float) (width/2 * sin);
        float widthDY = (float) (width/2 * cos);

        Point2D a = Point2D.newPoint2D(bottomCenter.getX() - widthDX, bottomCenter.getY() + widthDY);
        Point2D b = Point2D.newPoint2D(bottomCenter.getX() + widthDX, bottomCenter.getY() - widthDY);

        float heightDX = (float) (height * cos);
        float heightDY = (float) (height * sin);

        // 由b得到c a得到d
        Point2D c = Point2D.newPoint2D(b.getX() + heightDX, b.getY() + heightDY);
        Point2D d = Point2D.newPoint2D(a.getX() + heightDX, a.getY() + heightDY);
        return new Rectangle(a,b,c,d);
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
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);

        float widthDX = (float) (width/2 * sin);
        float widthDY = (float) (width/2 * cos);

        Point2D a = Point2D.newPoint2D(bottomCenter.getX() - widthDX, bottomCenter.getY() + widthDY);
        Point2D b = Point2D.newPoint2D(bottomCenter.getX() + widthDX, bottomCenter.getY() - widthDY);

        float heightDX = (float) (height * cos);
        float heightDY = (float) (height * sin);

        // 由b得到c a得到d
        Point2D c = Point2D.newPoint2D(b.getX() + heightDX, b.getY() + heightDY);
        Point2D d = Point2D.newPoint2D(a.getX() + heightDX, a.getY() + heightDY);

        // 不可以对超类的实现做出假设
        return redraw(a,b,c,d);
    }

    private void refreshCache(){
        this.width=MathUtils.distance(getPointA(),getPointD());
        this.height=MathUtils.distance(getPointA(),getPointB());
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
