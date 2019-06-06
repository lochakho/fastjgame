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

package com.wjybxx.fastjgame.dsl;

import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.shape.RectangleVertexHolder;
import com.wjybxx.fastjgame.shape.shape2d.Rectangle;
import com.wjybxx.fastjgame.utils.MathUtils;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 2D坐标系，采用左下角为(0,0);
 * 涉及到方向的(上下左右，顺时针，逆时针)的东西都需要在这里处理；
 * 因为在不同的坐标系下，左右和顺逆时针不一样；
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/3 17:30
 * @github - https://github.com/hl845740757
 */
@ThreadSafe
public final class CoordinateSystem2D {
    /**
     * 原点
     */
    public static final Point2D origin = Point2D.newPoint2D(0,0).unmodifiable();
    /**
     * x轴正方向单位向量
     */
    public static final Point2D xPositiveDirection = Point2D.newPoint2D(1,0).unmodifiable();
    /**
     * y轴正方向单位向量
     */
    public static final Point2D yPositiveDirection = Point2D.newPoint2D(0,1).unmodifiable();

    /**
     * a是否在b的上面
     * @param a
     * @param b
     * @return
     */
    public static boolean isHigher(Point2D a, Point2D b){
        return a.getY() > b.getY() ;
    }

    /**
     * a是否在b的右边
     * @param a
     * @param b
     * @return
     */
    public static boolean isMoreRight(Point2D a, Point2D b){
        return a.getX() > b.getX();
    }

    /**
     * a到b是否顺时针
     *
     * 假设坐标系x向右，y向上，那么叉乘方向朝向本人。
     * 二维向量的叉乘：cross(a,b) = ax * by - ay * bx = norm(a)* norm(b) * sin<a, b>
     * 叉乘的结果是正数，说明a到b是逆时针，反之顺时针；
     * 结果若是0，则说明a，b共线。
     * - https://blog.csdn.net/hy3316597/article/details/52732963
     *
     * @param a 起始向量
     * @param b 目标向量
     * @return true/false
     */
    public static boolean isClockwise(Point2D a, Point2D b){
        return MathUtils.crossProductValue(a,b) < 0;
    }

    /**
     * a到b是否顺时针或共线
     * @param a 起始向量
     * @param b 目标向量
     * @return
     */
    public static boolean isClockwiseOrCollinear(Point2D a, Point2D b){
        return MathUtils.crossProductValue(a,b) <= 0;
    }

    /**
     * a到b是否逆时针方向
     * @param a 起始向量
     * @param b 目标向量
     * @return true/false
     */
    public static boolean isCounterClockwise(Point2D a, Point2D b){
        return MathUtils.crossProductValue(a,b) > 0;
    }

    /**
     * a到b是否逆时针或共线
     * @param a 起始向量
     * @param b 目标向量
     * @return
     */
    public static boolean isCounterClockwiseOrOrCollinear(Point2D a, Point2D b){
        return MathUtils.crossProductValue(a,b) >= 0;
    }

    /**
     * compareDirection 是否在 centerDirection 右边;
     * 换句话说：顺时针夹角 (0,180)，开区间；
     * (顺时针是减少方向，逆时针是增加方向)
     * @param compareDirection 比较向量弧度角
     * @param centerDirection 基准向量弧度角
     * @return 顺时针方向返回true (重合返回false)
     */
    public static boolean isMoreRight(float compareDirection, float centerDirection){
        if (centerDirection >= 0){
            return compareDirection < centerDirection && compareDirection > (centerDirection - MathUtils.PI);
        }else {
            return compareDirection < centerDirection || compareDirection > (centerDirection + MathUtils.PI);
        }
    }

    /**
     * compareDirection 是否在 centerDirection 右边 或共线
     * @param compareDirection 比较向量弧度角
     * @param centerDirection 基准向量弧度角
     * @return
     */
    public static boolean isMoreRightOrCollinear(float compareDirection, float centerDirection){
       return isMoreRight(compareDirection, centerDirection) ||
               Float.compare(compareDirection, centerDirection) == 0;
    }


    /**
     * 向右旋转一定角度
     * (左加右减)
     * @param angle 当前弧度角
     * @param delta Δ用来表示增量
     * @return float (-PI,PI]
     */
    public static float turnRight(float angle, float delta){
        return MathUtils.radAngleSub(angle,delta);
    }

    /**
     * 向左旋转一定角度
     * (左加右减)
     * @param angle 当前弧度角
     * @param delta Δ用来表示增量
     * @return float (-PI,PI]
     */
    public static float turnLeft(float angle, float delta){
        return MathUtils.radAngleAdd(angle,delta);
    }

    /**
     * 计算矩形的四个顶点。
     * (为何方这里？因为上下左右和坐标系有关系.....)
     * <pre>
     *   d......direction....c
     *   |        |          |
     *   |        |          |
     *   |        |          |
     *   a......bottom.......b
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
    public static RectangleVertexHolder calRectangleVertex(Point2D bottomCenter, float direction, float width, float height){
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
        return new RectangleVertexHolder(a, b, c, d);
    }

    /**
     * 构建矩形(正方形)格子的区域
     * @param rowIndex 行索引
     * @param colIndex 列索引
     * @param gridWidth 格子宽高
     * @return Rectangle
     */
    public static Rectangle buildGridRegion(int rowIndex, int colIndex, int gridWidth){
        Point2D a = MathUtils.gridVertexLocation(rowIndex,colIndex, gridWidth);
        // ab 同y
        Point2D b = Point2D.newPoint2D(a.getX() + gridWidth, a.getY());
        // bc 同x
        Point2D c = Point2D.newPoint2D(b.getX(), b.getY() + gridWidth);
        // ad同x cd同y
        Point2D d = Point2D.newPoint2D(a.getX(), c.getY());
        return new Rectangle(a,b,c,d);
    }
}
