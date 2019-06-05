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

package com.wjybxx.fastjgame.utils;

import com.wjybxx.fastjgame.misc.FStraightLine;
import com.wjybxx.fastjgame.scene.Point2D;
import com.wjybxx.fastjgame.scene.Point3D;

/**
 * 数学计算辅助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 16:08
 * @github - https://github.com/hl845740757
 */
public class MathUtils {

    public static final float PI = (float) Math.PI;
    /**
     * 2PI这个变量名不好取 因此用下划线开头
     */
    public static final float DOUBLE_PI = PI * 2;
    /**
     * 二分之一 PI
     */
    public static final float HALF_PI = PI / 2;
    /**
     * float类型可忽略误差值
     * 当两个float的差值小于该值的时候，我们可以认为两个float相等
     */
    public static final float FLOAT_DEVIATION = 0.0001f;
    /**
     * double类型可忽略误差值
     * 当两个double的差值小于该值的时候，我们可以认为两个double相等
     */
    public static final double DOUBLE_DEVIATION = 0.0000001d;

    private MathUtils() {
    }

    /**
     * 两个int安全相乘，返回一个long，避免越界；
     * 相乘之后再强转可能越界。
     * @param a int
     * @param b int
     * @return long
     */
    public static long safeMultiplyInt(int a, int b){
        return (long)a * b;
    }

    /**
     * 两个short安全相乘，返回一个int，避免越界；
     * 相乘之后再强转可能越界。
     * @param a short
     * @param b short
     * @return integer
     */
    public static int safeMultiplyShort(short a, short b){
        return (int)a * b;
    }

    /**
     * 求两个点的距离
     * @param a 坐标a
     * @param b 坐标b
     * @return 开方后的真实距离
     */
    public static float distance(Point2D a,Point2D b){
        return (float) Math.sqrt(distanceWithoutSqrt(a,b));
    }

    /**
     * 求两个点的快速坐标距离(不开方)
     * @param a 坐标a
     * @param b 坐标b
     * @return x^2 + y^2
     */
    public static float distanceWithoutSqrt(Point2D a, Point2D b){
        float x=a.getX() - b.getX();
        float y=a.getY() - b.getY();
        // 暂时不考虑越界问题，游戏地图不应该存在这种情况
        return x*x + y*y;
    }

    /**
     * 两个int相除，如果余数大于0，则进一
     * @param a int
     * @param b int
     * @return int
     */
    public static int divideIntCeil(int a, int b){
        int remainder = a % b;
        if (remainder > 0){
            return a/b + 1;
        }else {
            return a/b;
        }
    }

    /**
     * 是否在区间段内，包含边界值
     * @param start 区间起始值 inclusive
     * @param end 区间结束值 inclusive
     * @param value 待检测的值
     * @return
     */
    public static boolean withinRange(int start, int end, int value){
        return value >= start && value <= end;
    }

    /**
     * 是否在区间段内，不包含边界值
     * @param start 区间起始值 exclusive
     * @param end 区间结束值 exclusive
     * @param value 待检测的值
     * @return
     */
    public static boolean betweenRange(int start, int end, int value){
        return value > start && value < end;
    }

    /**
     * 计算直线函数
     * @param start 起始点
     * @param end 结束点
     * @return 两点所在的直线
     */
    public static FStraightLine calStraightLine(Point2D start,Point2D end){
        float dx = end.getX() - start.getX();
        // x的差值不能为0
        if (Float.compare(dx,0.0f) == 0){
            throw new IllegalArgumentException("bad line");
        }

        float dy = end.getY() - start.getY();
        if (Float.compare(dy,0.0f) == 0){
            // y的差值为0表示平行于x轴
            // 将y存为临时变量，避免捕获start对象
            float y = start.getY();
            return x -> y;
        }

        // y = kx + b
        float k = dy / dx;
        float b = start.getY() - k * start.getX();
        return x -> k * x + b;
    }

    /**
     * 计算直线上一点
     * @param straightLine 直线函数
     * @param x x坐标
     * @return Point2D(x,y)
     */
    public static Point2D calStraightLine(FStraightLine straightLine,float x){
        return Point2D.newPoint2D(x,straightLine.apply(x));
    }

    /**
     * 计算一个点的行索引
     * @param point2D
     * @param gridHeight 格子高度
     * @return
     */
    public static int rowIndex(Point2D point2D,int gridHeight){
        return (int)(point2D.getY()) / gridHeight;
    }

    /**
     * 计算一个点的列索引
     * @param point2D
     * @param gridWidth 格子宽度
     * @return
     */
    public static int colIndex(Point2D point2D,int gridWidth){
        return (int)(point2D.getX()) / gridWidth;
    }

    /**
     * 格子顶点坐标(针对宽高一样的格子)
     * @param rowIndex 行索引，行索引对应的是Y索引 算出的是Y值
     * @param colIndex 列索引，列索引对应的是X索引 算出的是X值
     * @param gridWidth 格子宽度
     * @return
     */
    public static Point2D gridVertexLocation(int rowIndex, int colIndex, int gridWidth){
        return Point2D.newPoint2D(colIndex * gridWidth,rowIndex * gridWidth);
    }

    /**
     * 格子中心点坐标(针对宽高一样的格子)
     * @param rowIndex 行索引，行索引对应的是Y索引 算出的是Y值
     * @param colIndex 列索引，列索引对应的是X索引 算出的是X值
     * @param gridWidth 格子宽度
     * @return
     */
    public static Point2D gridCenterLocation(int rowIndex, int colIndex, int gridWidth){
        return Point2D.newPoint2D((colIndex + 0.5f) * gridWidth, (rowIndex + 0.5f) * gridWidth);
    }

    // region 弧度角(RadiansAngle)与圆心角(CentralAngle)
    // - https://blog.csdn.net/zoo_king/article/details/51613697
    // atan2值与象限 - https://blog.csdn.net/kingmember/article/details/79782888

    /**
     * 圆心角求弧度角 (180° = PI)
     * <pre>
     * 弧度转为角度 :
     *      degree = radians * 180 / PI
     * 角度转为弧度 :
     *      radians = degree * PI / 180
     * </pre>
     *
     * @param centralAngle 圆心角
     * @return radAngle
     */
    public static float radAngle(float centralAngle){
        // (n * π) / 180
        return (centralAngle * PI) / 180;
    }

    /**
     * 弧度角求圆心角
     *
     * @param radAngle 弧度角
     * @return centralAngle
     */
    public static float centralAngle(float radAngle){
        return (radAngle * 180) / PI;
    }

    public static float radAngleSub(float angle,float delta){
        float v = angle - delta;
        if ( v < - PI){
            // 越界
            return v + DOUBLE_PI;
        }else {
            return v;
        }
    }

    public static float radAngleAdd(float angle,float delta){
        float v = angle + delta;
        if (v > PI){
            // 越界
            return v - DOUBLE_PI;
        }else {
            return v;
        }
    }


    /**
     * 以center为中心，指定朝向，指定长度处的一点。
     * @param center 中心点
     * @param angle 在中心点的某个角度
     * @param len 指定长度
     * @return new point 2D
     */
    public static Point2D directionPoint(Point2D center, float angle, float len) {
        // targetX = x + l * cosθ
        float x = (float) (center.getX() + len * Math.cos(angle));
        // targetY = y + l * sinθ
        float y = (float) (center.getY() + len * Math.sin(angle));
        return Point2D.newPoint2D(x, y);
    }

    /**
     * 计算目标点相对于中心点的朝向。
     *
     * @param center 中心点
     * @param directionPoint 目标点
     * @return radiansAngle 弧度角
     */
    public static float directionBetweenPos(Point2D center, Point2D directionPoint) {
        return (float) Math.atan2(directionPoint.getY() - center.getY(), directionPoint.getX() - center.getX());
    }

    // endregion

    // region 浮点数比较

    /**
     * 判断两个float是否近似相等
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(float a,float b){
        return Math.abs(a-b) < FLOAT_DEVIATION;
    }

    /**
     * 判断两个double是否近似相等
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(double a,double b){
        return Math.abs(a-b) < DOUBLE_DEVIATION;
    }
    // endregion

    // region 二维向量
    /**
     * 向量加
     */
    public static Point2D add(Point2D p1, Point2D p2){
        return add(p1, p2,Point2D.newPoint2D());
    }

    public static Point2D add(Point2D p1, Point2D p2,Point2D result){
       return result.redraw(p1.getX() + p2.getX(), p1.getY() + p2.getY());
    }

    /**
     * 向量减
     */
    public static Point2D sub(Point2D p1, Point2D p2){
        return sub(p1,p2,Point2D.newPoint2D());
    }

    /**
     * 向量减 (p1 - p2)。
     * （由于向量减法用的太多，可能某些地方需要做缓存）
     * @param p1 被减数
     * @param p2 减数
     * @param result 结果容器，新对象或缓存对象
     * @return param result
     */
    public static Point2D sub(Point2D p1, Point2D p2,Point2D result){
        return result.redraw(p1.getX() - p2.getX(),
                p1.getY() - p2.getY());
    }

    /**
     * 向量点乘。
     * 点乘的几何意义是可以用来表征或计算两个向量之间的夹角，以及在b向量在a向量方向上的投影
     * <pre>
     *      a·b>0    方向基本相同，夹角在0°到90°之间
     *
     *      a·b=0    正交，相互垂直
     *
     *      a·b<0    方向基本相反，夹角在90°到180°之间
     * </pre>
     * - https://blog.csdn.net/dcrmg/article/details/52416832
     */
    public static float dotProduct(Point2D a, Point2D b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    /**
     * 向量叉乘。
     * 向量积的模（长度）可以解释成以a和b为邻边的平行四边形的面积。
     * （法线？）
     * - https://blog.csdn.net/dcrmg/article/details/52416832
     * @param p1
     * @param p2
     * @return
     */
    public static Point3D crossProduct(Point2D p1, Point2D p2) {
        return Point3D.newPoint3D(0,0, crossProductValue(p1, p2));
    }

    public static Point3D crossProduct(Point2D p1, Point2D p2,Point3D result) {
        return result.redraw(0,0, crossProductValue(p1, p2));
    }

    /**
     * <pre>
     * {@code
     *      cross(a,b) = ax * by - ay * bx = norm(a)* norm(b) * sin< a, b >
     * }
     * </pre>
     * @param a
     * @param b
     * @return
     */
    public static float crossProductValue(Point2D a, Point2D b) {
        // x1*y2 - x2*y1
        return a.getX() * b.getY() - a.getY() * b.getX();
    }

    // endregion

    // region 3维向量

    /**
     * 向量加
     */
    public static Point3D add(Point3D p1, Point3D p2){
        return Point3D.newPoint3D(p1.getX() + p2.getX(),
                p1.getY() + p2.getY(),
                p1.getZ() + p2.getZ());
    }

    /**
     * 向量减
     */
    public static Point3D sub(Point3D p1, Point3D p2){
        return sub(p1,p2,Point3D.newPoint3D());
    }

    /**
     * 计算向量p1 - p2的差，并将结果存入result对象。
     * （减法用的较多）
     * @param p1 被减数
     * @param p2 减数
     * @param result 结果容器
     * @return param result
     */
    public static Point3D sub(Point3D p1, Point3D p2,Point3D result){
        return result.redraw(p1.getX() - p2.getX(),
                p1.getY() - p2.getY(),
                p1.getZ() - p2.getZ());
    }

    /**
     * 向量点乘。
     * <pre>
     * {@code
     *      a = [a1,a2,....an] b = [b1,b2,....bn]
     *      a * b = a1*b1 + a2*b2 + ···· + an*bn
     * }
     * </pre>
     */
    public static float dotProduct(Point3D p1, Point3D p2) {
        return p1.getX() * p2.getX()
                + p1.getY() * p2.getY()
                + p1.getZ() + p2.getZ();
    }

    /**
     * 向量叉乘
     * <pre>
     *     {@code
     *          a = (x1,y1,z1) b = (x2,y2,z2)
     *          a * b = (y1*z2- y2*z1, -(x1*z2 - x2*z1) , x1*y2 - x2*y1)
     *     }
     * </pre>
     *
     * - https://blog.csdn.net/dcrmg/article/details/52416832
     * 在3D图像学中，叉乘的概念非常有用，可以通过两个向量的叉乘，生成第三个垂直于a，b的法向量，从而构建X、Y、Z坐标系。
     */
    public static Point3D crossProduct(Point3D a, Point3D b) {
        return crossProduct(a, b,Point3D.newPoint3D());
    }

    /**
     * 计算 a 与 b 的叉乘，并将结果存入result
     * @param result 结果容器
     * @return result
     */
    public static Point3D crossProduct(Point3D a, Point3D b,Point3D result) {
        // yz所在平面:  ay * bz - by * az
        float x = a.getY() * b.getZ() - b.getY() * a.getZ();
        // xz所在平面： -(ax * bz - bx * az)
        float y = - (a.getX() * b.getZ() - b.getX() * a.getZ());
        // xy所在平面： ax * by - bx * ay
        float z = a.getX() * b.getY() - b.getX() * a.getY();
        return result.redraw(x,y,z);
    }
    // endregion
}
