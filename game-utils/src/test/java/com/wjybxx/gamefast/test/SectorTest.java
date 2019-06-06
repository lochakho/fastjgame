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
import com.wjybxx.fastjgame.shape.shape2d.Sector;

/**
 * 扇形测试；
 * 其实最好写一个简单的图形化界面。
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 17:28
 * @github - https://github.com/hl845740757
 */
public class SectorTest {

    public static void main(String[] args) {
        // 15° - 75°   x^2 + y^2 = 25
        // 5 * cos 15° = 4.8296   5 * sin 15° = 1.2941
        Sector sector = Sector.newSector(Point2D.newPoint2D(0,0),5,45,60);

        System.out.println("center=" + sector.hasPoint(sector.getCenter()));

        float x = 1.2941f;
        float y = 4.8296f;

        System.out.println("endPoint=" + sector.hasPoint(Point2D.newPoint2D(x, y)));
        System.out.println("startPoint=" + sector.hasPoint(Point2D.newPoint2D(y, x)));

        // true false
        System.out.println("a1=" + sector.hasPoint(Point2D.newPoint2D(x, y-0.001f)));
        System.out.println("a2=" + sector.hasPoint(Point2D.newPoint2D(x, y+0.001f)));

        // false false
        System.out.println("b1=" + sector.hasPoint(Point2D.newPoint2D(y, x-0.001f)));
        System.out.println("b2=" + sector.hasPoint(Point2D.newPoint2D(y, x+0.001f)));

        // true false
        System.out.println("c1=" + sector.hasPoint(Point2D.newPoint2D(y-0.001f, x)));
        System.out.println("c2=" + sector.hasPoint(Point2D.newPoint2D(y+0.001f, x)));

        Point2D p1 = Point2D.newPoint2D(y - 0.001f, x);
        Point2D p2 = Point2D.newPoint2D(y+0.001f, x);
        long startTime = System.currentTimeMillis();
        for (int index=0;index<100*10000;index++){
            sector.hasPoint(p1);
            sector.hasPoint(p2);
        }
        // 我的机器(7年前的本)上大概比三角形少10毫秒 50 VS 60
        System.out.println("costMillTime=" + (System.currentTimeMillis()-startTime));
    }


}
