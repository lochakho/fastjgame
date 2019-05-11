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

package com.wjybxx.fastjgame.utils.configwrapper;

import jdk.nashorn.internal.ir.annotations.Immutable;

/**
 * 配置包装对象，子类实现必须为不可变对象，以实现线程安全。
 * 提供一些便利方法。
 * 但是觉得这个名好像没取好
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 13:30
 * @github - https://github.com/hl845740757
 */
@Immutable
public abstract class ConfigWrapper extends ConfigHelper{

    /**
     * @see ConfigHelper#getAsString(String)
     */
    @Override
    public abstract String getAsString(String key);

    /**
     * 基于map的配置是最容易理解的，最容易使用的，需要提供转换方法。
     * @return
     */
    public abstract MapConfigWrapper convert2MapWrapper();

    /**
     * 主要用于打印自己的数据结构，用于debug
     * @return
     */
    @Override
    public abstract String toString();
}
