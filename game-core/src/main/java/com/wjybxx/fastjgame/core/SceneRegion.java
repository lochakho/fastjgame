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

/**
 * 场景区域划分。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 11:33
 * @github - https://github.com/hl845740757
 */
public enum SceneRegion {

    /**
     * 本服普通区域，不互斥，大多数地图都应该属于它。
     */
    LOCAL_NORMAL(SceneProcessType.LOCAL,false),
    /**
     * 本服竞技场(DNF玩习惯了，习惯叫PKC)，互斥
     */
    LOCAL_PKC(SceneProcessType.LOCAL,true),
    /**
     * 安徒恩，跨服，不互斥。
     */
    WARZONE_ANTON(SceneProcessType.CROSS, false),
    /**
     * 卢克，跨服，不互斥。
     */
    WARZONE_LUKE(SceneProcessType.CROSS, false);

    /**
     * 区域所在的进程类型(是否是跨服区域)
     */
    private final SceneProcessType processType;
    /**
     * 该区域是否互斥，只能存在一个
     */
    private final boolean mutex;

    SceneRegion(SceneProcessType processType, boolean mutex) {
        this.processType = processType;
        this.mutex = mutex;
    }

    public SceneProcessType getProcessType(){
        return processType;
    }

    public boolean isMutex(){
        return mutex;
    }

}
