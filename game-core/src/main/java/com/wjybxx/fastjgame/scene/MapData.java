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
     * 地图宽
     */
    private final int mapWidth;

    /**
     * 地图高
     */
    private final int mapHeight;

    /**
     * 所有的格子数据
     */
    private final MapGrid[][] allMapGrids;

    public MapData(int mapId, int mapWidth, int mapHeight, MapGrid[][] allMapGrids) {
        this.mapId = mapId;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.allMapGrids = allMapGrids;
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
        return allMapGrids.length;
    }

    public int getColCount(){
        return allMapGrids[0].length;
    }

    public MapGrid getGrid(Point2D point2D){
        // 需要注意上界溢出问题
        int rowIndex = Math.min(getRowCount() - 1,(int)point2D.getY() / GameConstant.MAP_GRID_WIDTH);
        int colIndex = Math.min(getColCount() - 1,(int)point2D.getX() / GameConstant.MAP_GRID_WIDTH);
        return allMapGrids[rowIndex][colIndex];
    }

    public MapGrid getGrid(int rowIndex, int colIndex){
        return allMapGrids[rowIndex][colIndex];
    }

    /**
     * 纠正算出来的坐标值
     * @param point2D 地图内的一坐标值
     */
    public void correctLocation(Point2D point2D){
        // 检查x左边溢出
        if (point2D.getX() < 0){
            point2D.setX(1);
        } else if (point2D.getX() > mapWidth){
            point2D.setX(mapWidth - 1);
        }
        // 检查Y坐标溢出
        if (point2D.getY() < 0){
            point2D.setY(1);
        } else if (point2D.getY() > mapHeight){
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
