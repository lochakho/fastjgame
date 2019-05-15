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

package com.wjybxx.fastjgame.utils;

/**
 * 数学计算辅助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 16:08
 * @github - https://github.com/hl845740757
 */
public class MathUtils {

    /**
     * 两个int安全相乘，返回一个long，避免越界；
     * 相乘之后再强转可能越界。
     * @param a int
     * @param b int
     * @return long
     */
    public static long safeMultiplyInt(int a, int b){
        return (long)a * b;
    }

    /**
     * 两个short安全相乘，返回一个int，避免越界；
     * 相乘之后再强转可能越界。
     * @param a short
     * @param b short
     * @return integer
     */
    public static int safeMultiplyShort(short a, short b){
        return (int)a * b;
    }
}
