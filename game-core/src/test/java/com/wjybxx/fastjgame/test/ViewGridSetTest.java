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

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.scene.ViewGridSet;

import java.util.Arrays;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/3 23:10
 * @github - https://github.com/hl845740757
 */
public class ViewGridSetTest {

    public static void main(String[] args) {
        ViewGridSet viewGridSet = new ViewGridSet(8000,10000,600);
        System.out.println(viewGridSet);
    }

}
