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

/**
 * 游戏常量值；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 21:03
 * @github - https://github.com/hl845740757
 */
public class GameConstant {

    /**
     * 地图格子大小，所有地图必须是统一的。
     */
    public static final int MAP_GRID_WIDTH = 50;

    // region 视野
    /**
     * 一行（列）可见格子数；
     * 必须是奇数，因为除去我当前所在的格子后，仍然需要左右（上下）对称；
     * (测试过5，感觉也还好)
     */
    private static final int VIEWABLE_GRID_NUM_PER_LINE = 3;
    /**
     * 所有可见格子数为一行格子数的平方；
     * （正方形）
     */
    public static final int VIEWABLE_GRID_NUM = VIEWABLE_GRID_NUM_PER_LINE * VIEWABLE_GRID_NUM_PER_LINE;

    /**
     * 可见格子之间的索引增量，格子之间的行列索引都小于该值时可见；
     * (减去自己，左右对称，除以二即可)
     */
    public static final int VIEWABLE_DELTA_INDEX = (VIEWABLE_GRID_NUM_PER_LINE - 1) / 2;
    /**
     * 视野范围对应的格子数,为一行（一列）可见格子数的一半；
     * 由于一行格子数必须是奇数，因此视野范围必定包含半个格子；
     * （视野范围是半径，理想的其实是正方形内的一个圆）
     */
    public static final float VIEWABLE_RANGE_GRID_NUM = VIEWABLE_GRID_NUM_PER_LINE / 2f;
    // endregion

    private GameConstant() {
    }
}
