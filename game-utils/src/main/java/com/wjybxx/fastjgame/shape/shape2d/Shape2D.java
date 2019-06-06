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

import javax.annotation.Nonnull;

/**
 * 2D图形，采用左下角为(0,0)点；
 * 建议逆时针对点编号，为什么呢？
 * 你写0是逆时针写还是顺时针写？
 * 你画圆是逆时针还是顺时针？
 * 你画三角形是逆时针还是顺时针？
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:18
 * @github - https://github.com/hl845740757
 */
public interface Shape2D {

    /**
     * 该图形内是否存在该点，在边上也算。
     * @param point2D 2d平面的一点
     * @return
     */
    boolean hasPoint(@Nonnull Point2D point2D);
}
