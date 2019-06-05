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

import com.wjybxx.fastjgame.enummapper.NumberEnum;
import com.wjybxx.fastjgame.enummapper.NumberEnumMapper;
import com.wjybxx.fastjgame.utils.ReflectionUtils;

/**
 * 运行平台类型，平台问题最终还是会遇见，这里先处理。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 22:02
 * @github - https://github.com/hl845740757
 */
public enum PlatformType implements NumberEnum {
    /**
     * 测试用的运营平台
     */
    TEST(0),
    ;

    /**
     * 平台数字标记
     */
    private final int number;

    PlatformType(int number) {
        this.number = number;
    }

    @Override
    public int getNumber() {
        return number;
    }

    private static final NumberEnumMapper<PlatformType> mapper = ReflectionUtils.indexNumberEnum(values());

    public static PlatformType forNumber(int number){
        PlatformType platformType = mapper.forNumber(number);
        assert null!=platformType:"invalid number "+number;
        return platformType;
    }
}
