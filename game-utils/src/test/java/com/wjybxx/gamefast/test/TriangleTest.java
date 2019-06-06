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

package com.wjybxx.gamefast.test;

import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.shape.shape2d.Triangle;

/**
 * 主要测试三角形的计算点是否ok
 *
 * A(0,0),B(2,8),C(8,4)
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 21:38
 * @github - https://github.com/hl845740757
 */
public class TriangleTest {

    public static void main(String[] args) {
        Point2D a=Point2D.newPoint2D(0,0);
        Point2D b=Point2D.newPoint2D(2,8);
        Point2D c=Point2D.newPoint2D(8,4);

        Triangle triangle=new Triangle(a,b,c);

        System.out.println("a=" + triangle.hasPoint(a));
        System.out.println("b=" + triangle.hasPoint(b));
        System.out.println("c=" + triangle.hasPoint(c));

        Point2D p1 = Point2D.newPoint2D(3, 7.3333f);
        Point2D p2 = Point2D.newPoint2D(3, 7.3334f);
        // true false
        System.out.println("(3,7.3333f)=" + triangle.hasPoint(p1));
        System.out.println("(3,7.3334f)=" + triangle.hasPoint(p2));

        long startTime=System.currentTimeMillis();
        for (int index=0;index<100*10000;index++){
            triangle.hasPoint(p1);
            triangle.hasPoint(p2);
        }
        System.out.println("costMillTimes=" + (System.currentTimeMillis()-startTime));
    }

}
