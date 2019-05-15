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
 * 场景进程类型。
 * 为了尽量使人员分散，本服和跨服还是独立启动好一点，虽然也能设计为既是跨服也是本服。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 11:56
 * @github - https://github.com/hl845740757
 */
public enum SceneProcessType {
    /**
     * 本服场景
     */
    LOCAL,
    /**
     * 跨服进程
     */
    CROSS;

    /**
     * 通过名字获取进程枚举
     * @param name 枚举对应的名字，忽略大小写
     * @return
     */
    public static SceneProcessType forName(String name){
        for (SceneProcessType processType:values()){
            if (processType.name().equalsIgnoreCase(name)){
                return processType;
            }
        }
        throw new IllegalArgumentException("invalid name " + name);
    }

}
