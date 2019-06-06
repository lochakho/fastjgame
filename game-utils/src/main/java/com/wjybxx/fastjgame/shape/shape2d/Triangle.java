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
import com.wjybxx.fastjgame.shape.Point3D;
import com.wjybxx.fastjgame.shape.RedrawShape;
import com.wjybxx.fastjgame.utils.MathUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * 三角形(顺时针)
 * <pre>
 *       a
 *     *  *
 *   *     *
 *  *       *
 * b * * * * c
 * </pre>
 *
 * 同向法：
 * <pre>
 *     {@code
 *     function SameSide(p1,p2, a,b)
 *     cp1 = CrossProduct(b-a, p1-a)
 *     cp2 = CrossProduct(b-a, p2-a)
 *     if DotProduct(cp1, cp2) >= 0 then return true
 *     else return false
 *
 *     function PointInTriangle(p, a,b,c)
 *     if SameSide(p,a, b,c) and SameSide(p,b, a,c)
 *     and SameSide(p,c, a,b) then return true
 *     else return false
 *     }
 * </pre>
 * 重心法：
 * <pre>
 *     {@code
 *     // Compute vectors
 *      v0 = C - A
 *      v1 = B - A
 *      v2 = P - A
 *
 *      // Compute dot products
 *      dot00 = dot(v0, v0)
 *      dot01 = dot(v0, v1)
 *      dot02 = dot(v0, v2)
 *      dot11 = dot(v1, v1)
 *      dot12 = dot(v1, v2)
 *
 *      // Compute barycentric coordinates
 *      invDenom = 1 / (dot00 * dot11 - dot01 * dot01)
 *      u = (dot11 * dot02 - dot01 * dot12) * invDenom
 *      v = (dot00 * dot12 - dot01 * dot02) * invDenom
 *
 *      // Check if point is in triangle
 *      return (u >= 0) && (v >= 0) && (u + v < 1)
 *     }
 * </pre>
 *
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 0:21
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class Triangle implements Shape2D, RedrawShape {

    // 三个缓存向量(为何要做缓存？ 在MMO游戏中，技能等存在大量的选中判断，一个技能可能需要做几十次的判断)
    private static final Point2D cacheV0 =Point2D.newPoint2D();
    private static final Point2D cacheV1 =Point2D.newPoint2D();
    private static final Point2D cacheV2 =Point2D.newPoint2D();

    private static final Point3D cacheV4 = Point3D.newPoint3D();
    private static final Point3D cacheV5 = Point3D.newPoint3D();

    private final Point2D a;

    private final Point2D b;

    private final Point2D c;

    public Triangle(@Nonnull Point2D a,@Nonnull Point2D b,@Nonnull Point2D c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * 这里采用重心法。
     *
     * 内角和法，同向法，重心法，面积法。
     * (同向法是向量计算，应该是较快的，理解起来也容易)
     * （重心法也是向量计算，而且计算量更少）
     *
     * 强烈建议看这篇文章（带有测试用例）：
     * - http://blackpawn.com/texts/pointinpoly/default.html
     * 经过测试也是OK的
     *
     * @param p 2d坐标
     * @return
     */
    @Override
    public boolean hasPoint(@Nonnull Point2D p) {
        // Compute vectors
        Point2D v0 = MathUtils.sub(c,a, cacheV0);
        Point2D v1 = MathUtils.sub(b,a, cacheV1);
        Point2D v2 = MathUtils.sub(p,a, cacheV2);

        float dot00 = MathUtils.dotProduct(v0, v0);
        float dot01 = MathUtils.dotProduct(v0, v1);
        float dot02 = MathUtils.dotProduct(v0, v2);
        float dot11 = MathUtils.dotProduct(v1, v1);
        float dot12 = MathUtils.dotProduct(v1, v2);

        // Compute barycentric coordinates
        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        // Check if point is in triangle
        // 改为小于等于1 表示我们需要取线上的点
        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }

    /**
     * 重新绘制,不会修改内部引用，只会修改数值。
     * {@link Point2D#updateLocation(Point2D)}
     */
    public Triangle redraw(Point2D a, Point2D b, Point2D c){
        this.a.updateLocation(a);
        this.b.updateLocation(b);
        this.c.updateLocation(c);
        return this;
    }

    public Point2D getPointA() {
        return a;
    }

    public Point2D getPointB() {
        return b;
    }

    public Point2D getPointC() {
        return c;
    }

    // 同向法
    /**
     * 判断 p1 和 p2 是否在 ab 向量的同侧
     * @return true/false
     */
    private static boolean sameSide(Point2D p1,Point2D p2,Point2D a,Point2D b){
        Point2D ab = MathUtils.sub(b, a, cacheV0);
        Point2D ap1 = MathUtils.sub(p1, a, cacheV1);
        Point2D ap2 = MathUtils.sub(p2, a, cacheV2);

        Point3D cp1 = MathUtils.crossProduct(ab,ap1,cacheV4);
        Point3D cp2 = MathUtils.crossProduct(ab,ap2,cacheV5);
        return MathUtils.dotProduct(cp1,cp2) >= 0;
    }

    /**
     * 同向法求p是否在三角形内，p和任意顶点都在另外两个顶点构成的向量的同侧。
     * @return true/false
     */
    private static boolean pointInTriangle(Point2D p,Point2D a,Point2D b,Point2D c){
        return sameSide(p,a, b,c)
                && sameSide(p,b, a,c)
                && sameSide(p,c, a,b);
    }
}
