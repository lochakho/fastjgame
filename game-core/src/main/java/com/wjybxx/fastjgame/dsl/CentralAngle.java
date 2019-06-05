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

package com.wjybxx.fastjgame.dsl;

/**
 * 圆心角 [0,360)；
 *
 * 一般没有额外说明时都是弧度角。
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/2 15:01
 * @github - https://github.com/hl845740757
 */
public class CentralAngle {

    /**
     * [0,360)
     * 0 inclusive
     * 360 exclusive
     */
    private float angle;

    public CentralAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public RadiansAngle toRadAngle(){
        return new RadiansAngle((float) (angle/180.0f * Math.PI));
    }
}
