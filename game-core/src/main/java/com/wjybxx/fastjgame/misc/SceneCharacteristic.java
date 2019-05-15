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
 * Scene进程特征值，表示Scene进程开启了哪些服务
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/14 22:08
 * @github - https://github.com/hl845740757
 */
public enum SceneCharacteristic {
    /**
     * 全服活动玩法，如竞技场；
     * 如果scene进程带有该特征值，表示该scene进程支持全服活动玩法，参加活动时，本服玩家都将传送到该进程。
     * 当多个进程带有此特征时，只有一个scene进程有效。
     *
     * 当前scene进程宕机时，如果剩余的scene进程中有存在该特征值的scene进程，
     * 则会挑选一个scene进程生效。
     */
    WHOLE_SERVER_ACT(1),
    /**
     * 独立活动玩法，如基本城镇，独立副本。
     * 如果scene进程带有该特征值，表示该scene支持所有的本服普通玩法，所有这些玩法
     * 都可以在当前进程完成，而不需要跨进程。
     *
     * 拥有该特征值的scene进程之间是不影响的，可以同时存在多个，一个scene宕机不会影响其它scene。
     */
    INDEPENDENT_ACT(2);

    /**
     * 枚举序列化时使用的数字标记
     */
    public final int number;

    SceneCharacteristic(int number) {
        this.number = number;
    }

    public static SceneCharacteristic forNumber(int number){
        return values()[number-1];
    }
}
