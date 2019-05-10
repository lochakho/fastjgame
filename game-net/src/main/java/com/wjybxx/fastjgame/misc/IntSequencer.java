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
 * int类型的序号分配器。
 * 类似{@link LongSequencer}。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/6 20:58
 * @github - https://github.com/hl845740757
 */
@NotThreadSafe
public class IntSequencer {
    /**
     * 上一次分配的序号,也就是当前Sequence
     */
    private int value;

    public IntSequencer(int initSequence) {
        this.value = initSequence;
    }

    /**
     * 获取当前值
     * @return
     */
    public int get(){
        return value;
    }

    /**
     * 设置序号
     * @param sequence
     */
    public void set(int sequence){
        this.value=sequence;
    }

    /**
     * 返回之后+1
     * @return
     */
    public int getAndInc(){
        return value++;
    }

    /**
     * +1之后返回
     * @return
     */
    public int incAndGet(){
        return ++value;
    }

    @Override
    public String toString() {
        return "LongSequencer{" +
                "value=" + value +
                '}';
    }
}
