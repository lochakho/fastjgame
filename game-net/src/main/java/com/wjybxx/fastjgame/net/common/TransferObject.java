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

package com.wjybxx.fastjgame.net.common;

import javax.annotation.concurrent.Immutable;

/**
 * 传输对象，用于将逻辑线程的数据传输到IO线程 或 将IO线程的数据传输到逻辑线程。
 * 收发消息必须使用{@link TransferObject}。
 * 要求子类实现都是不可变的，以保证线程安全。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 9:23
 */
@Immutable
public interface TransferObject {

}
