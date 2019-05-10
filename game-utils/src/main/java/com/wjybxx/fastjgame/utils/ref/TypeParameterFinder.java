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

package com.wjybxx.fastjgame.utils.ref;

import io.netty.handler.codec.http.HttpObject;

/**
 * 泛型参数具体类型查找器。
 * 哪些泛型参数可以被找到？
 * 需要满足下面两个条件：
 * 1.指定泛型参数是在该对象的类所在的类层次的上层定义的。
 * 2.在定义该泛型参数的类/接口的下层被具体化了。
 *
 * 举个栗子：
 * 在{@link io.netty.handler.codec.MessageToMessageDecoder}中定义了泛型参数I，
 * 而其子类 {@link io.netty.handler.codec.http.HttpContentDecoder}将其指定为{@link HttpObject},
 * 那么可以通过HttpContentDecoder的实例查找到MessageToMessageDecoder上的泛型参数I为HttpObject类型。
 *
 * 反面栗子：
 * 在{@link java.util.List}中定义了泛型参数E，
 * 在{@link java.util.ArrayList}中声明了新的泛型参数E，并将List中的E指定为新声明的E(这是两个泛型参数)。
 * 那么无法通过ArrayList的实例查找到List上泛型参数E的具体类型的。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 10:32
 * @github - https://github.com/hl845740757
 */
public interface TypeParameterFinder {

    /**
     * 从instance所属的类开始，查找在superClazzOrInterfaced定义的泛型参数typeParamName的具体类型
     *
     * @param instance 实例对象
     * @param superClazzOrInterface 声明泛型参数typeParamName的类,class或interface
     * @param typeParamName 泛型参数名字
     * @param <T> 约束必须有继承关系或实现关系
     * @return 如果定义的泛型存在，则返回对应的泛型clazz
     */
    <T> Class<?> findTypeParameter(T instance, Class<? super T> superClazzOrInterface, String typeParamName) throws Exception;

}
