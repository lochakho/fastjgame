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
