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

import com.wjybxx.fastjgame.scene.gameobject.Player;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 场景广播使用的过滤器
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 18:38
 * @github - https://github.com/hl845740757
 */
public class SceneBroadcastFilters {

    // 使用lambda表达式的，是因为是无状态的，不捕获任何属性，编译后是静态方法，性能可以接受
    // 匿名内部类性能不好

    /**
     * 所有玩家
     */
    public static final Predicate<Player> ALL = player -> true;

    public static Predicate exceptSelf(Player player){
        return new ExceptSelf(player);
    }

    public static Predicate except(Player... players){
        return new Except(Arrays.asList(players));
    }

    public static Predicate except(List<Player> playerList){
        return new Except(playerList);
    }

    private static final class ExceptSelf implements Predicate<Player>{

        private final Player player;

        private ExceptSelf(Player player) {
            this.player = player;
        }

        @Override
        public boolean test(Player player) {
            return this.player != player;
        }
    }

    private static final class Except implements Predicate<Player>{

        private final List<Player> excepts;

        private Except(List<Player> excepts) {
            this.excepts=excepts;
        }

        @Override
        public boolean test(Player player) {
            return excepts.indexOf(player) >= 0;
        }
    }
}
