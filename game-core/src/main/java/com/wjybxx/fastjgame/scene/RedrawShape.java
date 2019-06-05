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

package com.wjybxx.fastjgame.scene;

/**
 * 可重绘的图形，由于需要的参数不尽相同，定义方法不是很方便，因此是个标记接口。
 * 子类需要提供一个redraw方法，并返回自己。
 * 该接口只是个契约。
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 15:17
 * @github - https://github.com/hl845740757
 */
public interface RedrawShape {

    // redraw and return this

}
