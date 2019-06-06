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

import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import com.wjybxx.fastjgame.scene.gameobject.GameObjectType;
import com.wjybxx.fastjgame.utils.CollectionUtils;

import java.util.EnumMap;

/**
 * 游戏对象handler映射；
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 18:48
 * @github - https://github.com/hl845740757
 */
public class GameObjectHandlerMapper<T> {

    private final EnumMap<GameObjectType,T> handlerMap = new EnumMap<>(GameObjectType.class);

    /**
     * 注册一个handler，不可以重复注册
     * @param gameObjectType 不可以重复
     * @param handler 对应的处理器
     */
    public void registerHandler(GameObjectType gameObjectType,T handler){
        CollectionUtils.requireNotContains(handlerMap,gameObjectType,"gameObjectType");
        handlerMap.put(gameObjectType,handler);
    }

    /**
     * 替换一个handler，有时候有特殊逻辑的时候可能需要
     * @param gameObjectType 游戏对象类型
     * @param handler 对应的处理器
     */
    public void replaceHandler(GameObjectType gameObjectType,T handler){
        handlerMap.put(gameObjectType,handler);
    }

    /**
     * 获取对应的处理器
     * @param objectType 游戏对象类型
     * @return handler
     */
    public T getHandler(GameObjectType objectType){
        return handlerMap.get(objectType);
    }

    /**
     * 获取对应的处理器；
     * 子类可以覆盖该方法以提供类型转换;
     * @param gameObject 游戏对象
     * @param <U> gameObject的类型，方便实现类进行强制转换
     * @return handler
     */
    public <U extends GameObject> T getHandler(U gameObject){
        return handlerMap.get(gameObject.getObjectType());
    }

}
