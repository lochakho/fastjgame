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

import com.wjybxx.fastjgame.shape.Point2D;
import com.wjybxx.fastjgame.utils.GameConstant;
import com.wjybxx.fastjgame.utils.MathUtils;

/**
 * 地图数据,从地图编辑器导出的文件加载之后创建。
 * 地图编辑器直接导出json数据是否会更方便。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/1 18:32
 * @github - https://github.com/hl845740757
 */
public class MapData {

    /**
     * 地图id，与资源唯一对应
     */
    private final int mapId;

    /**
     * 所有的格子数据
     */
    private final MapGrid[][] allMapGrids;
    private final int rowCount;
    private final int colCount;

    /**
     * 地图宽
     */
    private final int mapWidth;

    /**
     * 地图高
     */
    private final int mapHeight;

    public MapData(int mapId, MapGrid[][] allMapGrids) {
        this.mapId = mapId;
        this.allMapGrids = allMapGrids;
        this.rowCount = allMapGrids.length;
        this.colCount = allMapGrids[0].length;
        this.mapWidth = colCount * GameConstant.MAP_GRID_WIDTH;
        this.mapHeight = rowCount * GameConstant.MAP_GRID_WIDTH;
    }

    public int getMapId() {
        return mapId;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public MapGrid[][] getAllMapGrids() {
        return allMapGrids;
    }

    public int getRowCount(){
        return rowCount;
    }

    public int getColCount(){
        return colCount;
    }

    public MapGrid getGrid(Point2D point2D){
        int rowIndex = MathUtils.rowIndex(rowCount, GameConstant.MAP_GRID_WIDTH, point2D.getY());
        int colIndex = MathUtils.colIndex(colCount, GameConstant.MAP_GRID_WIDTH, point2D.getX());
        return allMapGrids[rowIndex][colIndex];
    }

    public MapGrid getGrid(int rowIndex, int colIndex){
        return allMapGrids[rowIndex][colIndex];
    }

    /**
     * 纠正算出来的坐标值，修正到地图内部，脱离 边界条件 和 溢出情况；
     * @param point2D 地图内的一坐标值
     */
    public void correctLocation(Point2D point2D){
        int x = (int) point2D.getX();
        // 检查x左边溢出
        if (x <= 0){
            point2D.setX(1);
        } else if (x >= mapWidth){
            point2D.setX(mapWidth - 1);
        }
        // 检查Y坐标溢出
        int y = (int) point2D.getY();
        if (y <= 0){
            point2D.setY(1);
        } else if (y >= mapHeight){
            point2D.setY(mapHeight - 1);
        }
    }

    /**
     * 坐标是否溢出了
     * @param point2D 一个坐标
     */
    public boolean isLocationOverflow(Point2D point2D){
        return !MathUtils.withinRange(0, mapWidth, (int)point2D.getX()) ||
                !MathUtils.withinRange(0, mapHeight, (int)point2D.getY());
    }

}
