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

/**
 * 跳跃点寻路策略
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/3 15:34
 * @github - https://github.com/hl845740757
 */
public class JumpPointSearchStrategy implements FindPathStrategy{

    @Override
    public List<MapGrid> findPath(MapGrid[][] allMapGrids, MapGrid startGrid, MapGrid endGrid, EnumSet<GridObstacle> movableGrids) {
        return null;
    }
}
