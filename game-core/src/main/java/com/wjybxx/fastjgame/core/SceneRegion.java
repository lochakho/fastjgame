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

package com.wjybxx.fastjgame.core;

import com.wjybxx.fastjgame.enummapper.NumberEnum;
import com.wjybxx.fastjgame.enummapper.NumberEnumMapper;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

/**
 * 场景区域划分。
 * 建议每个区域至少拥有一个城镇，通过城镇进入其它类型的地图(普通副本，活动副本)。
 * 没有城镇也是可以的，传图时需要指定要传入的地图id。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 11:33
 * @github - https://github.com/hl845740757
 */
public enum SceneRegion implements NumberEnum {

    /**
     * 本服普通区域，不互斥，大多数地图都应该属于它。
     */
    LOCAL_NORMAL(1, SceneProcessType.SINGLE,false),
    /**
     * 本服竞技场(DNF玩习惯了，习惯叫PKC)，互斥
     */
    LOCAL_PKC(2, SceneProcessType.SINGLE,true),
    /**
     * 安徒恩，跨服，不互斥。
     */
    WARZONE_ANTON(3, SceneProcessType.CROSS, false),
    /**
     * 卢克，跨服，不互斥。
     */
    WARZONE_LUKE(4, SceneProcessType.CROSS, false);

    /**
     * 区域所在的进程类型(是否是跨服区域)
     */
    private final SceneProcessType sceneProcessType;

    /**
     * 数字标记，不使用ordinal
     */
    private final int number;
    /**
     * 该区域是否互斥，只能存在一个
     */
    private final boolean mutex;

    SceneRegion(int number, SceneProcessType sceneProcessType, boolean mutex) {
        this.number=number;
        this.sceneProcessType = sceneProcessType;
        this.mutex = mutex;
    }

    /**
     * 数字id到枚举的映射
     */
    private static final NumberEnumMapper<SceneRegion> mapper = ReflectionUtils.indexNumberEnum(values());

    public static SceneRegion forNumber(int number){
        SceneRegion sceneRegion = mapper.forNumber(number);
        assert null!=sceneRegion:"invalid number " + number;
        return sceneRegion;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public SceneProcessType getSceneProcessType(){
        return sceneProcessType;
    }

    public boolean isMutex(){
        return mutex;
    }

}
