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

package com.wjybxx.fastjgame.mrg;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.scene.GridObstacle;
import com.wjybxx.fastjgame.scene.MapData;
import com.wjybxx.fastjgame.scene.MapGrid;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * 地图资源加载器，按需加载；
 * 由于地图资源信息可能很大，加载到内存浪费内存，也会拖慢启动速度；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 14:13
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class MapDataLoadMrg {

    /**
     * 已加载的地图信息
     */
    private final Int2ObjectMap<MapData> loadCache = new Int2ObjectOpenHashMap<>(32);

    @Inject
    public MapDataLoadMrg() {

    }

    /**
     * 加载地图资源
     * @param mapId
     * @return
     */
    public MapData loadMapData(int mapId){
        MapData mapData = loadCache.get(mapId);
        if (null == mapData){
            mapData = loadMapDataImp(mapId);
            loadCache.put(mapId, mapData);
        }
        return mapData;
    }

    /**
     * 加载地图资源
     * @param mapId 地图资源id
     * @return mapData
     */
    @Nonnull
    private MapData loadMapDataImp(int mapId){
        // TODO 真正加载地图资源，现在随机生成吧
        final int rowCount = 100;
        final int colCount = 80;

        MapGrid[][] allMapGrids = new MapGrid[rowCount][colCount];
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++){
            for (int colIndex = 0; colIndex < colCount; colIndex++){
                allMapGrids[rowIndex][colIndex] = new MapGrid(rowIndex,colIndex,new int[]{GridObstacle.FREE.getNumber()});
            }
        }

        return new MapData(mapId,allMapGrids);
    }
}
