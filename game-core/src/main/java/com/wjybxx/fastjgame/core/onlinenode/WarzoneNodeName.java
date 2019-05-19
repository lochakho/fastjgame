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

package com.wjybxx.fastjgame.core.onlinenode;

/**
 * 战区服也必须总是生成相同的名字，以使得同时只能有一个战区服存在。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 0:14
 * @github - https://github.com/hl845740757
 */
public class WarzoneNodeName {
    /**
     * 其实可以不要的
     */
    private final int warzoneId;

    public WarzoneNodeName(int warzoneId) {
        this.warzoneId = warzoneId;
    }

    public int getWarzoneId() {
        return warzoneId;
    }


    @Override
    public String toString() {
        return "WarzoneNodeName{" +
                "warzoneId=" + warzoneId +
                '}';
    }
}
