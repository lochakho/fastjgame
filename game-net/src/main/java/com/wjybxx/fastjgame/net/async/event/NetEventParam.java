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

package com.wjybxx.fastjgame.net.async.event;

import javax.annotation.concurrent.Immutable;

/**
 * 网络事件参数，提供统一的抽象(窄)视图。
 * 子类实现必须是不可变对象，以保证线程安全性。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 9:16
 */
@Immutable
public interface NetEventParam {

    /**
     * 获取网络事件对应的sessionGuid
     * @return
     */
    long sessionGuid();
}
