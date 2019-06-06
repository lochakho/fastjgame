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
import com.wjybxx.fastjgame.shape.RedrawShape;
import com.wjybxx.fastjgame.utils.MathUtils;

import javax.annotation.Nonnull;

/**
 * 扇形。
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 14:46
 * @github - https://github.com/hl845740757
 */
public class Sector implements Shape2D, RedrawShape {

    private static final Point2D cacheP =Point2D.newPoint2D();

    private final Circle circle;
    /**
     * 扇形中心朝向，弧度角
     */
    private float direction;
    /**
     * 扇形角度，弧度角
     */
    private float angle;

    // 扇形的起始向量，都是以center为起点的向量
    private final Point2D startVector = Point2D.newPoint2D();
    private final Point2D endVector = Point2D.newPoint2D();

    /**
     * new instance
     * @param center 扇形中心点
     * @param radius 扇形半径
     * @param radiansDirection 扇形朝向，弧度角
     * @param centralAngle 扇形圆心角
     */
    public Sector(Point2D center, float radius,float radiansDirection,float centralAngle) {
        this.circle = new Circle(center,radius);
        this.direction = radiansDirection;
        this.angle = MathUtils.radAngle(centralAngle);
        refreshCache();
    }

    /**
     * 通过圆心角创建扇形
     * @param center 扇形中心点
     * @param radius 扇形半径
     * @param centralDirection 扇形朝向，圆心角表示
     * @param centralAngle 扇形圆心角
     * @return
     */
    public static Sector newSector(Point2D center, float radius, float centralDirection, float centralAngle){
        float radiansDirection=MathUtils.radAngle(centralDirection);
        return new Sector(center,radius,radiansDirection,centralAngle);
    }

    /**
     * 求解一点是否在扇形内。
     * 1.可以求夹角和
     * 2.可以同向法(p在start的逆时针方向，end在p的逆时针方向)
     *
     * <pre>
     *     end
     *     *      p
     *     *     *     start
     *     *    *    *
     *     *   *   *
     *     *  *  *
     *     * *
     *     center
     * </pre>
     *
     * - https://stackoverflow.com/questions/13652518/efficiently-find-points-inside-a-circle-sector
     * @param point2D 2d平面的一点
     * @return
     */
    @Override
    public boolean hasPoint(@Nonnull Point2D point2D) {
        // 不在圆内
        if (!circle.hasPoint(point2D)){
            return false;
        }
        // 转换为以center为起点的向量
        Point2D p = MathUtils.sub(point2D, getCenter(), cacheP);
        // 同向法，start到p为逆时针，p到end也为逆时针
        return CoordinateSystem2D.isCounterClockwiseOrOrCollinear(startVector,p) &&
                CoordinateSystem2D.isCounterClockwiseOrOrCollinear(p,endVector);
    }

    /**
     * 根据参数重绘
     * @param center 中心点
     * @param ratio 半径
     * @param direction 扇形中心朝向
     * @param centralAngle 扇形存在的圆心角
     * @return
     */
    public Sector redraw(Point2D center, float ratio,float direction,float centralAngle){
        this.circle.redraw(center,ratio);
        this.direction = direction;
        this.angle = MathUtils.radAngle(centralAngle);
        refreshCache();
        return this;
    }

    private void refreshCache(){
        float delta = this.angle / 2;
        Point2D startPoint = MathUtils.directionPoint(getCenter(), MathUtils.radAngleSub(direction, delta), getRadius());
        // 转换为以center为起点的向量
        MathUtils.sub(startPoint, getCenter(), startVector);

        Point2D endPoint = MathUtils.directionPoint(getCenter(), MathUtils.radAngleAdd(direction, delta), getRadius());
        // 转换为以center为起点的向量
        MathUtils.sub(endPoint, getCenter(), endVector);
    }

    public Point2D getCenter() {
        return circle.getCenter();
    }

    public float getRadius() {
        return circle.getRadius();
    }

    public float getDirection() {
        return direction;
    }

    public float getAngle() {
        return angle;
    }

}
