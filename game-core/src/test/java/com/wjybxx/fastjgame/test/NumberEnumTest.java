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

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.enummapper.NumberEnum;
import com.wjybxx.fastjgame.enummapper.NumberEnumMapper;
import com.wjybxx.fastjgame.net.async.event.NetEventType;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.scene.GridObstacle;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 13:54
 * @github - https://github.com/hl845740757
 */
public class NumberEnumTest {

    public static void main(String[] args) {
        // 需要排除加载ReflectionUtils的时间了。
        System.out.println(ReflectionUtils.class.getSimpleName());
        // 第一次加载的时候有点慢啊
        System.out.println(EEE.forNumber(1));

        // 老式的看看
        System.out.println(NetEventType.forNumber((byte)1));
        System.out.println(NetEventType.forNumber((byte)2));

        System.out.println(GridObstacle.forNumber(0));
        System.out.println(GridObstacle.forNumber(1));

        System.out.println(RoleType.forNumber(0));
        System.out.println(RoleType.forNumber(1));
    }

    private static enum EEE implements NumberEnum {

        A,B,C,D;

        @Override
        public int getNumber() {
            return ordinal() + 1;
        }

        private static final NumberEnumMapper<EEE> mapping = ReflectionUtils.indexNumberEnum(values());

        public static EEE forNumber(int number){
            return mapping.forNumber(number);
        }
    }
}
