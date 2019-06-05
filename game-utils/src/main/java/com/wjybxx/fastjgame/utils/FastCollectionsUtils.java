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

import com.wjybxx.fastjgame.function.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.Map;

/**
 * 针对fastUtil集合的帮助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/9 12:36
 * @github - https://github.com/hl845740757
 */
public class FastCollectionsUtils {

    private FastCollectionsUtils() {

    }

    /**
     * 移除map中符合条件的元素，并对删除的元素执行后续的操作
     *
     * @param map       必须是可修改的map
     * @param predicate 过滤条件，为真的删除
     * @param then      元素删除之后执行的逻辑
     * @param <V>       the type of value
     * @return 删除的元素数量
     */
    public static <V> int removeIfAndThen(final Long2ObjectMap<V> map, final LongObjPredicate<? super V> predicate, LongObjConsumer<V> then) {
        ObjectIterator<Long2ObjectMap.Entry<V>> itr = map.long2ObjectEntrySet().iterator();
        int removeNum = 0;
        Long2ObjectMap.Entry<V> entry;
        long k;
        V v;
        while (itr.hasNext()){
            entry = itr.next();
            k=entry.getLongKey();
            v=entry.getValue();
            if (predicate.test(k,v)){
                itr.remove();
                removeNum++;
                then.accept(k,v);
            }
        }
        return removeNum;
    }

    /**
     * 移除map中符合条件的元素，并对删除的元素执行后续的操作
     *
     * @param map       必须是可修改的map
     * @param predicate 过滤条件，为真的删除
     * @param then      元素删除之后执行的逻辑
     * @param <V>       the type of value
     * @return 删除的元素数量
     */
    public static <V> int removeIfAndThen(final Int2ObjectMap<V> map, final IntObjPredicate<? super V> predicate, IntObjConsumer<V> then) {
        ObjectIterator<Int2ObjectMap.Entry<V>> itr = map.int2ObjectEntrySet().iterator();
        int removeNum = 0;
        Int2ObjectMap.Entry<V> entry;
        int k;
        V v;
        while (itr.hasNext()){
            entry = itr.next();
            k=entry.getIntKey();
            v=entry.getValue();
            if (predicate.test(k,v)){
                itr.remove();
                removeNum++;
                then.accept(k,v);
            }
        }
        return removeNum;
    }

    /**
     * 移除map中符合条件的元素，并对删除的元素执行后续的操作
     *
     * @param map       必须是可修改的map
     * @param predicate 过滤条件，为真的删除
     * @param then      元素删除之后执行的逻辑
     * @param <V>       the type of value
     * @return 删除的元素数量
     */
    public static <V> int removeIfAndThen(final Short2ObjectMap<V> map, final ShortObjPredicate<? super V> predicate, ShortObjConsumer<V> then) {
        ObjectIterator<Short2ObjectMap.Entry<V>> itr = map.short2ObjectEntrySet().iterator();
        int removeNum = 0;
        Short2ObjectMap.Entry<V> entry;
        short k;
        V v;
        while (itr.hasNext()){
            entry = itr.next();
            k=entry.getShortKey();
            v=entry.getValue();
            if (predicate.test(k,v)){
                itr.remove();
                removeNum++;
                then.accept(k,v);
            }
        }
        return removeNum;
    }

    // region 要求制定键值不存在 或 存在

    public static <V> void requireNotContains(Int2ObjectMap<V> map,int key,String msg){
        if (map.containsKey(key)){
            throw new IllegalArgumentException("duplicate " + msg + "-" + key);
        }
    }

    public static <V> void requireNotContains(Long2ObjectMap<V> map,long key,String msg){
        if (map.containsKey(key)){
            throw new IllegalArgumentException("duplicate " + msg + "-" + key);
        }
    }

    public static <V> void requireNotContains(Short2ObjectMap<V> map,short key,String msg){
        if (map.containsKey(key)){
            throw new IllegalArgumentException("duplicate " + msg + "-" + key);
        }
    }

    public static <V> void requireContains(Int2ObjectMap<V> map,int key,String msg){
        if (!map.containsKey(key)){
            throw new IllegalArgumentException("nonexistent " + msg + "-" + key);
        }
    }

    public static <V> void requireContains(Long2ObjectMap<V> map,long key,String msg){
        if (!map.containsKey(key)){
            throw new IllegalArgumentException("nonexistent " + msg + "-" + key);
        }
    }

    public static <V> void requireContains(Short2ObjectMap<V> map,short key,String msg){
        if (!map.containsKey(key)){
            throw new IllegalArgumentException("nonexistent " + msg + "-" + key);
        }
    }

    // endregion

    // region 创建足够容量的Map

    /**
     * 创建足够容量的Map，容量到达指定容量之后才会开始扩容；
     * 适合用在能估算最大容量的时候;
     * 和JDK的loadFactor有区别，FastUtil无法使得大于{@code initCapacity}时才扩容
     * @param constructor map的构造器函数
     * @param initCapacity 初始容量 大于0有效
     * @param <K> key的类型
     * @param <V> value的类型
     * @return M
     */
    public static <K,V,M extends Map<K,V>> M newEnoughCapacityMap(MapConstructor<M> constructor, int initCapacity){
        // fastUtil 需要 + 1,JDK的不需要
        return initCapacity > 0 ? constructor.newMap(initCapacity + 1, 1) : constructor.newMap(16, 0.75f);
    }


    /**
     * @see #newEnoughCapacityMap(MapConstructor, int)
     */
    public static <V> Long2ObjectMap<V> newEnoughCapacityLongMap(int initCapacity) {
        return newEnoughCapacityMap(Long2ObjectOpenHashMap::new,initCapacity);
    }

    /**
     * @see #newEnoughCapacityMap(MapConstructor, int)
     */
    public static <V> Int2ObjectMap<V> newEnoughCapacityIntMap(int initCapacity) {
        return newEnoughCapacityMap(Int2ObjectOpenHashMap::new,initCapacity);
    }

    /**
     * @see #newEnoughCapacityMap(MapConstructor, int)
     */
    public static <V> Short2ObjectMap<V> newEnoughCapacityShortMap(int initCapacity) {
        return newEnoughCapacityMap(Short2ObjectOpenHashMap::new,initCapacity);
    }


    // endregion
}
