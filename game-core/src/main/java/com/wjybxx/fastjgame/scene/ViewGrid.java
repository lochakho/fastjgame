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

package com.wjybxx.fastjgame.scene;

import com.wjybxx.fastjgame.dsl.CoordinateSystem2D;
import com.wjybxx.fastjgame.scene.shape2d.Rectangle;
import com.wjybxx.fastjgame.scene.shape2d.Shape2D;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

import static com.wjybxx.fastjgame.utils.GameConstant.VIEWABLE_GRID_NUM;

/**
 * 视野格子。
 * 每个视野格子是个正方形。
 * 地图从左下角(0,0)开始，视野格子是很标准的，且平行于坐标系的矩形。
 *
 * 一个地图分为N个地图格子，一个九宫格中的视野格子包含一定个数的地图格子。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/31 23:13
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class ViewGrid extends GameObjectContainer implements Shape2D {

    /**
     * 行索引（y索引）
     */
    private final int rowIndex;

    /**
     * 列索引（x索引）
     */
    private final int colIndex;

    /**
     * 视野格子大小
     */
    private final int gridWidth;

    /**
     * 视野格子对应的区域
     */
    private final Rectangle region;
    /**
     * 周围的视野格子(可见的视野格子，周围的格子和自己)
     */
    private final List<ViewGrid> viewableGrids = new ArrayList<>(VIEWABLE_GRID_NUM);

    public ViewGrid(int rowIndex, int colIndex, int gridWidth) {
        this(rowIndex,colIndex,gridWidth,InitCapacityHolder.EMPTY);
    }

    public ViewGrid(int rowIndex, int colIndex, int gridWidth,InitCapacityHolder initCapacityHolder) {
        super(initCapacityHolder);
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.gridWidth = gridWidth;
        this.region= CoordinateSystem2D.buildGridRegion(rowIndex,colIndex, gridWidth);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public List<ViewGrid> getViewableGrids() {
        return viewableGrids;
    }

    @Override
    public boolean hasPoint(@Nonnull Point2D point2D) {
        return region.hasPoint(point2D);
    }
}
