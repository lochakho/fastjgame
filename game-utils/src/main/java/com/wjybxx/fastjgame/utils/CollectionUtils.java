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

import com.wjybxx.fastjgame.function.MapConstructor;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 集合帮助类
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/29 10:19
 * @github - https://github.com/hl845740757
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * 删除集合中满足条件的元素，并对删除的元素执行后续逻辑。
     * 建议map使用{@link #removeIfAndThen(Map, BiPredicate, BiConsumer)}进行删除
     * 保留entry对象有潜在风险
     *
     * @param collection 可修改的集合
     * @param filter     什么样的元素需要删除
     * @param then       元素删除之后执行逻辑
     * @param <E>        元素的类型。注意：不可以是{@link Map.Entry}
     * @return 返回删除成功的元素
     */
    public static <E> int removeIfAndThen(Collection<E> collection, Predicate<E> filter, Consumer<E> then) {
        Iterator<E> iterator = collection.iterator();
        E e;
        int removeNum = 0;
        while (iterator.hasNext()) {
            e = iterator.next();
            if (filter.test(e)) {
                iterator.remove();
                removeNum++;
                then.accept(e);
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
     * @param <K>       the type of key
     * @param <V>       the type of value
     * @return 删除的元素数量
     */
    public static <K, V> int removeIfAndThen(final Map<K, V> map, final BiPredicate<? super K, ? super V> predicate, BiConsumer<K, V> then) {
        int removeNum = 0;
        // entry在调用remove之后不可再访问,因此需要将key-value保存下来
        Map.Entry<K, V> kvEntry;
        K k;
        V v;
        Iterator<Map.Entry<K, V>> itr = map.entrySet().iterator();
        while (itr.hasNext()) {
            kvEntry = itr.next();
            k = kvEntry.getKey();
            v = kvEntry.getValue();

            if (predicate.test(k, v)) {
                itr.remove();
                removeNum++;
                then.accept(k, v);
            }
        }
        return removeNum;
    }

    /**
     * 删除掉map中满足条件的
     *
     * @param map       必须是可修改的map
     * @param predicate 过滤条件，为真的删除
     * @param <K>       the type of key
     * @param <V>       the type of value
     * @return 删除的元素数量
     */
    public static <K, V> int removeIf(final Map<K, V> map, final BiPredicate<? super K, ? super V> predicate) {
        return removeIfAndThen(map, predicate, (k, v) -> { });
    }

    /**
     * 将并发队列中的元素全部收集到指定list中。
     * 解决的问题是什么？
     * 如果遍历之后调用clear()，clear删除的元素可能比你遍历的并不一致。 可参考竞态条件之 '先检查再执行'
     * @param concurrentLinkedQueue 并发队列
     * @param out 目标list，并发队列弹出的元素会放入该list
     * @param <E> 元素的类型
     */
    public static <E> void pollToList(ConcurrentLinkedQueue<E> concurrentLinkedQueue, List<E> out){
        E e;
        while ((e=concurrentLinkedQueue.poll())!=null){
            out.add(e);
        }
    }

    /**
     * 将并发队列中的元素全部收集到指定list中。
     * 特意重载是为了表明针对并发队列使用的。
     */
    public static <E> void pollToList(BlockingQueue<E> blockingQueue, List<E> out){
        E e;
        while ((e=blockingQueue.poll())!=null){
            out.add(e);
        }
    }

    /**
     * 在blockingQueue中使用poll方式寻找某个元素
     * @param blockingQueue 元素队列，元素中除了期望的一个元素以外都无用
     * @param matcher 匹配函数
     * @param <E> the type of element
     * @return 如果找不到则返回null
     */
    public static <E> E findElementWithPoll(BlockingQueue<E> blockingQueue, Predicate<E> matcher){
        return findElementWithPollImp(blockingQueue,matcher);
    }

    /**
     * 在concurrentLinkedQueue中使用poll方式寻找某个元素
     * @param concurrentLinkedQueue 元素队列，元素中除了期望的一个元素以外都无用
     * @param matcher 匹配函数
     * @param <E> the type of element
     * @return 如果找不到则返回null
     *
     */
    public static <E> E findElementWithPoll(ConcurrentLinkedQueue<E> concurrentLinkedQueue, Predicate<E> matcher){
        return findElementWithPollImp(concurrentLinkedQueue,matcher);
    }

    /**
     * 使用poll方式在队列中寻找元素的真正实现，之所以不暴露，是因为主要是用于解决并发队列问题的。
     * @param queue 元素队列，元素中除了期望的一个元素以外都无用
     * @param matcher 匹配函数
     * @param <E> the type of element
     * @return 如果找不到则返回null，其实函数式方式可以返回 {@link Optional}
     */
    private static <E> E findElementWithPollImp(Queue<E> queue, Predicate<E> matcher){
        E e;
        while ((e=queue.poll())!=null){
            if (matcher.test(e)){
                return e;
            }
        }
        return null;
    }


    /**
     * 使用poll方式等待blockingQueue中出现某个元素
     * @param blockingQueue 元素队列，元素中除了期望的一个元素以外都无用
     * @param matcher 匹配函数
     * @param maxWaitTime 最大等待时间 毫秒
     * @param <E> the type of element
     * @return 未在限定时间内等到期望的元素，则返回null
     */
    public static  <E> E waitElementWithPoll(BlockingQueue<E> blockingQueue, Predicate<E> matcher, int maxWaitTime) {
        boolean interrupted=false;
        long endTime=System.currentTimeMillis()+maxWaitTime;
        try {
            for(long remainTime=maxWaitTime;remainTime>0;remainTime=endTime-System.currentTimeMillis()){
                try{
                    E e =blockingQueue.poll(remainTime, TimeUnit.MILLISECONDS);
                    // 超时
                    if (null == e){
                        return null;
                    }
                    // 期望的元素
                    if (matcher.test(e)){
                        return e;
                    }
                } catch(InterruptedException e){
                    // 收到了中断请求，我们先不处理它，但是要将它存储下来，继续尝试
                    interrupted=true;
                }
            }
            // 超时
            return null;
        }finally {
            // 返回前恢复中断状态
            if(interrupted){
                Thread.currentThread().interrupt();
            }
        }
    }

    public static <K> void requireNotContains(Map<K,?> map,K k,String name){
        if (map.containsKey(k)){
            throw new IllegalArgumentException("duplicate " + name + " " + k);
        }
    }

    /**
     * 创建足够容量的Map，容量到达指定容量之后才会开始扩容；
     * 适合用在能估算最大容量的时候;
     * @param constructor map的构造器函数
     * @param initCapacity 初始容量 大于0有效
     * @param <K> key的类型
     * @param <V> value的类型
     * @return M
     */
    public static <K,V,M extends Map<K,V>> M newEnoughCapacityMap(MapConstructor<M> constructor, int initCapacity){
        return initCapacity > 0 ? constructor.newMap(initCapacity,1) : constructor.newMap(16,0.75f);
    }

    /**
     *
     * 创建足够容量的HashMap，容量到达指定容量之后才会开始扩容；
     * 适合用在能估算最大容量的时候;
     * @param initCapacity 初始容量 大于0有效
     * @param <K> key的类型
     * @param <V> value的类型
     */
    public static <K,V> HashMap<K,V> newEnoughCapacityHashMap(int initCapacity){
        return newEnoughCapacityMap(HashMap::new, initCapacity);
    }

    /**
     * 创建足够容量的LinkecHashMap，容量到达指定容量之后才会开始扩容；
     * 适合用在能估算最大容量的时候;
     * @param initCapacity 初始容量 大于0有效
     * @param <K> key的类型
     * @param <V> value的类型
     * @return
     */
    public static <K,V> LinkedHashMap<K,V> newEnoughCapacityLinkedHashMap(int initCapacity){
        return newEnoughCapacityMap(LinkedHashMap::new, initCapacity);
    }

}