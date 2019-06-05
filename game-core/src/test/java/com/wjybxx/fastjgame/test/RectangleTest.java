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

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.scene.Point2D;
import com.wjybxx.fastjgame.scene.shape2d.Rectangle;

/**
 * 矩形创建测试
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/3 20:10
 * @github - https://github.com/hl845740757
 */
public class RectangleTest {

    public static void main(String[] args) {
        Rectangle rectangle=Rectangle.newRectangleByCentralAngle(Point2D.newPoint2D(0,0),90,6,3);
        System.out.println(rectangle);

        Rectangle rectangle2=Rectangle.newRectangleByCentralAngle(Point2D.newPoint2D(0,0),45,6,3);
        System.out.println(rectangle2);
    }

}
