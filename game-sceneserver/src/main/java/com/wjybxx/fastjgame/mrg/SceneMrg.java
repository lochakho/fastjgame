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
import com.wjybxx.fastjgame.scene.Dungeon;
import com.wjybxx.fastjgame.scene.Scene;
import com.wjybxx.fastjgame.scene.Town;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * 管理所有的Scene对象；
 * （创建，寻找，删除）
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 19:56
 * @github - https://github.com/hl845740757
 */
public class SceneMrg {

    private static final Logger logger = LoggerFactory.getLogger(SceneMrg.class);

    // 按照Scene的不同存储在不同的map

    /**
     * 城镇信息
     */
    private final Long2ObjectMap<Town> townMap = new Long2ObjectOpenHashMap<>();

    /**
     * 副本信息；
     * 副本这个词很难翻译的贴切，我们之前项目的词我觉得也不太贴切；
     */
    private final Long2ObjectMap<Dungeon> dungeonMap = new Long2ObjectOpenHashMap<>();

    @Inject
    public SceneMrg() {

    }

    public void tick(long curMillTime){
        tickScene(townMap,curMillTime);
        tickScene(dungeonMap,curMillTime);
    }

    private <T extends Scene> void tickScene(Long2ObjectMap<T> sceneMap,long curMillTime){
        if (sceneMap.size() == 0){
            return;
        }
        for (Scene scene : sceneMap.values()){
            try {
                scene.tick(curMillTime);
            }catch (Exception e){
                logger.error("tick caught exception, sceneType={}",scene.sceneType(),e);
            }
        }
    }


    @Nullable
    public Scene getScene(long guid){
        // 副本中寻找
        Dungeon dungeon = getDungeon(guid);
        if (null != dungeon){
            return dungeon;
        }
        // 城镇中寻找
        Town town = getTown(guid);
        if (null != town){
            return town;
        }
        return null;
    }

    /**
     * 通过guid获取城镇对象
     * @param guid 城镇guid
     * @return Town
     */
    @Nullable
    public Town getTown(long guid) {
        return townMap.get(guid);
    }

    /**
     * 通过guid获取副本对象
     * @param guid 副本guid
     * @return Dungeon
     */
    @Nullable
    public Dungeon getDungeon(long guid){
        return dungeonMap.get(guid);
    }
}
