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

/**
 * token禁用信息，禁止特定时间点之前的token使用
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/8 22:58
 * @github - https://github.com/hl845740757
 */
public class ForbiddenTokenInfo {
    /**
     * 被禁用的token创建时间，该时间戳及之前的token都无效。
     */
    private int forbiddenCreateTime;
    /**
     * 被释放的时间(过期时间)
     */
    private int releaseTime;

    public ForbiddenTokenInfo(int forbiddenCreateTime, int releaseTime) {
        this.forbiddenCreateTime = forbiddenCreateTime;
        this.releaseTime = releaseTime;
    }

    public int getForbiddenCreateTime() {
        return forbiddenCreateTime;
    }

    public int getReleaseTime() {
        return releaseTime;
    }

    public void update(int forbiddenCreateTime, int releaseTime) {
        this.forbiddenCreateTime = forbiddenCreateTime;
        this.releaseTime = releaseTime;
    }

}
