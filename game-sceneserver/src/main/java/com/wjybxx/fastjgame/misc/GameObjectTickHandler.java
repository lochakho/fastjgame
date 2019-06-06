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

import com.wjybxx.fastjgame.scene.gameobject.GameObject;
import com.wjybxx.fastjgame.utils.MathUtils;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/6 11:48
 * @github - https://github.com/hl845740757
 */
public abstract class GameObjectTickHandler<T extends GameObject> {

    /**
     * 每秒几帧
     */
    private final int framePerSecond;

    /**
     * tick间隔(帧间隔)
     */
    private final long tickInterval;

    /**
     * 下次tick的时间戳
     */
    private long nextTickTimeMills;

    protected GameObjectTickHandler(int framePerSecond) {
        this.framePerSecond = framePerSecond;
        this.tickInterval = MathUtils.frameInterval(framePerSecond);
    }

    /**
     * tick刷帧
     * @param gameObject 游戏场景对象
     *
     */
    public abstract void tick(T gameObject);

    public int getFramePerSecond() {
        return framePerSecond;
    }

    public long getTickInterval() {
        return tickInterval;
    }

    public long getNextTickTimeMills() {
        return nextTickTimeMills;
    }

    public void setNextTickTimeMills(long nextTickTimeMills) {
        this.nextTickTimeMills = nextTickTimeMills;
    }
}
