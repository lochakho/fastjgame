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

/**
 * 场景类型
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/5 20:22
 * @github - https://github.com/hl845740757
 */
public enum SceneType {

    /**
     * 城镇；
     * 一旦创建，不会销毁。
     */
    TOWN,

    /**
     * 副本(地下城)；
     * 当一段时间没有玩家后，删除；
     */
    DUNGEON,

}
