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

package com.wjybxx.fastjgame.shape.shape2d;

/**
 * 默认直线实现，如果不想使用lambda表达式
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/6 11:04
 * @github - https://github.com/hl845740757
 */
public class StraightLineImp implements StraightLine{

    private final float k;

    private final float b;

    public StraightLineImp(float k, float b) {
        this.k = k;
        this.b = b;
    }

    @Override
    public float apply(float x) {
        return k *x + b;
    }
}
