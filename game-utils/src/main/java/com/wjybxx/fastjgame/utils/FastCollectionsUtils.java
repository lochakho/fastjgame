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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;

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
}
