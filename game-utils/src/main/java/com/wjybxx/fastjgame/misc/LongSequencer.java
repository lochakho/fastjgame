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

import javax.annotation.concurrent.NotThreadSafe;

/**
 * long类型的序号分配器。
 * 采用递增方式分配。
 * 由于使用的地方还挺多，整合出一个类来。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 20:42
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public final class LongSequencer {
    /**
     * 上一次分配的序号,也就是当前Sequence
     */
    private long value;

    public LongSequencer(long initSequence) {
        this.value = initSequence;
    }

    /**
     * 获取当前值
     * @return
     */
    public long get(){
        return value;
    }

    /**
     * 设置序号
     * @param sequence
     */
    public void set(long sequence){
        this.value=sequence;
    }

    /**
     * 返回之后+1
     * @return
     */
    public long getAndInc(){
        return value++;
    }

    /**
     * +1之后返回
     * @return
     */
    public long incAndGet(){
        return ++value;
    }

    @Override
    public String toString() {
        return "LongSequencer{" +
                "value=" + value +
                '}';
    }
}
