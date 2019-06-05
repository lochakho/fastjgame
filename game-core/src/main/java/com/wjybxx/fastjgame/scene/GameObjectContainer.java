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

import com.wjybxx.fastjgame.scene.gameobject.*;
import com.wjybxx.fastjgame.utils.FastCollectionsUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

import java.util.EnumMap;

/**
 * gameObject的容器
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 21:52
 * @github - https://github.com/hl845740757
 */
public class GameObjectContainer {

    /**
     * 所有游戏对象
     */
    private final Long2ObjectMap<GameObject> guid2GameObjectMap;

    /**
     * 玩家集合，用于快速访问;
     * （初始容量不是很好确定，依赖于地图玩法）
     */
    private final Long2ObjectMap<Player> playerMap;
    /**
     * 宠物集合
     */
    private final Long2ObjectMap<Pet> petMap;
    /**
     * npc集合
     */
    private final Long2ObjectMap<Npc> npcMap;

    private int totalInitCapacity=0;
    /**
     * 辅助map，消除switch case；
     * 能改善代码的可读性，可维护性，但可能降低了速度。
     */
    private final EnumMap<GameObjectType,Long2ObjectMap<? extends GameObject>> helperMap = new EnumMap<>(GameObjectType.class);

    public GameObjectContainer() {
        this(InitCapacityHolder.EMPTY);
    }

    public GameObjectContainer(InitCapacityHolder capacityHolder) {
        playerMap = createMap(GameObjectType.PLAYER, capacityHolder.getPlayerSetInitCapacity());
        petMap = createMap(GameObjectType.PET, capacityHolder.getPetSetInitCapacity());
        npcMap = createMap(GameObjectType.NPC, capacityHolder.getNpcSetInitCapacity());

        guid2GameObjectMap = FastCollectionsUtils.newEnoughCapacityLongMap(totalInitCapacity);
    }

    private <V extends GameObject> Long2ObjectMap<V> createMap(GameObjectType gameObjectType,int initCapacity){
        Long2ObjectMap<V> result = FastCollectionsUtils.newEnoughCapacityLongMap(initCapacity);
        helperMap.put(gameObjectType, result);
        totalInitCapacity += initCapacity;
        return result;
    }

    public int getPlayerNum(){
        return playerMap.size();
    }

    public ObjectCollection<Player> getPlayerSet(){
        return playerMap.values();
    }

    public ObjectCollection<Pet> getPetSet(){
        return petMap.values();
    }

    public ObjectCollection<Npc> getNpcSet(){
        return npcMap.values();
    }

    /**
     * 添加一个场景对象到该视野格子
     * @param gameObject 游戏对象
     * @param <T> 对象的类型
     */
    public <T extends GameObject> void addGameObject(T gameObject){
        FastCollectionsUtils.requireNotContains(guid2GameObjectMap,gameObject.getGuid(),"guid");
        guid2GameObjectMap.put(gameObject.getGuid(),gameObject);

        @SuppressWarnings("unchecked")
        Long2ObjectMap<T> objectMap = (Long2ObjectMap<T>) helperMap.get(gameObject.getObjectType());
        assert null != objectMap : gameObject.getObjectType().name();
        objectMap.put(gameObject.getGuid(),gameObject);
    }

    /**
     * 从当前视野格子删除一个对象
     * @param gameObject 游戏对象
     * @param <T> 对象的类型
     */
    public <T extends GameObject> void removeObject(T gameObject){
        GameObject removeObj = guid2GameObjectMap.remove(gameObject.getGuid());
        assert null != removeObj;

        @SuppressWarnings("unchecked")
        Long2ObjectMap<T> objectMap = (Long2ObjectMap<T>) helperMap.get(gameObject.getObjectType());
        assert null != objectMap : gameObject.getObjectType().name();
        objectMap.remove(gameObject.getGuid());
    }
}
