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

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 17:00
 * @github - https://github.com/hl845740757
 */
public class PortRange {
    /**
     * 起始端口，包含。
     * inclusive
     */
    public final int startPort;
    /**
     * 截止端口，包含。
     * inclusive
     */
    public final int endPort;

    public PortRange(int startPort, int endPort) {
        this.startPort = startPort;
        this.endPort = endPort;
    }

    @Override
    public String toString() {
        return "PortRange{" +
                "startPort=" + startPort +
                ", endPort=" + endPort +
                '}';
    }
}
