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

package com.wjybxx.gamefast.test;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * FastUtil的Map的加载因子;
 *
 * JDK的hashMap 和 FastUtil的hashMap都是当容量大于阈值的时候触发扩容；
 * <pre>
 * {@code
 *      // 放入值
 *      putVal(k,v);
 *      // 检查是否需要扩容
 *      if(size > threshold){
 *          resize();
 *      }
 * }
 * 不过当 loadFactor为1的时候， JDK的hashMap的最大threshold为{@code initCapacity}，
 * {@code
 *      float ft = (float)newCap * loadFactor;
 * }
 *
 * 而FastUtil的hashMap的最大threshold为 {@code initCapacity - 1}
 * {@link it.unimi.dsi.fastutil.HashCommon#maxFill(int, float)}
 * {@code
 *      Math.min((long)Math.ceil(n * f), n - 1)
 * }
 * 原因在于解决hash冲突的方式不一样，JDK的HashMap使用数组+链表解决冲突；
 * 而FastUtil使用线性探测解决冲突，因此FastUtil无法存放大于底层数组的数据
 *
 * </pre>
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 23:26
 * @github - https://github.com/hl845740757
 */
public class LoadFactorTest {

    private static int nextSize(int cap){
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return n;
    }

    public static void main(String[] args) {
        System.out.println(nextSize(16));
        System.out.println(nextSize(17));

        testFastMap();

        testHashMap();

        System.out.println("打断点看");
    }

    private static int getMaxFill() {
        return 1;
    }

    /**
     * 测试fastUtilMap的扩容
     *
     * size > maxFill 扩容
     */
    private static void testFastMap() {
        int size = 0;
        if (size++ >= getMaxFill()){
            System.out.println("inner " + size);
        }
        System.out.println("outer " + size);

        // if (size ++ >= maxFill) 是判定再加，这种写法容易搞晕，一不小心就晕
        // 表示插入之前的容量已经到达阈值则扩容，<=> 插入后的size大于阈值则扩容
        // 等价于JDK的  ++size > maxFill
        Long2ObjectOpenHashMap<String> map3 = new Long2ObjectOpenHashMap<>(3,1);
        putN(map3,4);

        Long2ObjectOpenHashMap<String> map4 = new Long2ObjectOpenHashMap<>(4,1);
        putN(map4,5);
    }

    /**
     * 测试hashMap扩容
     *
     * 插入后的size大于阈值则扩容
     */
    private static void testHashMap(){
        int size = 0;
        if (++size >= getMaxFill()){
            System.out.println("inner " + size);
        }
        System.out.println("outer " + size);

        //  hashMap 又是 ++ size,是先加再判定
        //  if (++size > threshold)
        HashMap<Long,String> map4 = new HashMap<>(4,1);
        putN(map4,5);
    }

    /**
     * 放入n个数
     * @param map
     * @param n
     */
    private static void putN (Map<Long,String> map, int n){
        for (int index=0; index<n; index++){
            long key = map.size() + 1;
            map.put(key,""+key);
            System.out.println("break point");
        }
    }
}
