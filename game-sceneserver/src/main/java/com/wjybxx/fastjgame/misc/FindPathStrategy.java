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

package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.scene.GridObstacle;
import com.wjybxx.fastjgame.scene.MapGrid;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 寻路算法策略。
 * <p>
 * 跳点寻路：
 * - https://zerowidth.com/2013/05/05/jump-point-search-explained.html
 * (强烈建议看看这篇文章，包含跳点寻路 和 A*寻路的展示demo)
 *
 * 中文版：
 * - https://blog.csdn.net/yuxikuo_1/article/details/50406651
 * </p>
 *
 * <p>
 * 另一篇参考文章：
 * - https://gamedevelopment.tutsplus.com/tutorials/how-to-speed-up-a-pathfinding-with-the-jump-point-search-algorithm--gamedev-5818
 * </p>
 *
 * 此外，在github上可以有很多参考实现。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/3 15:18
 * @github - https://github.com/hl845740757
 */
public interface FindPathStrategy {

    /**
     * 寻找一条可移动的路线
     * @param allMapGrids 地图数据
     * @param startGrid 起始点
     * @param endGrid 目的地
     * @param movableGrids 可行走的格子类型
     * @return 如果不可达，返回null 或 emptyList
     */
    List<MapGrid> findPath(MapGrid[][] allMapGrids, MapGrid startGrid, MapGrid endGrid, EnumSet<GridObstacle> movableGrids);

}
