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

import com.wjybxx.fastjgame.mrg.CuratorMrg;
import com.wjybxx.fastjgame.mrg.GameConfigMrg;
import org.apache.zookeeper.CreateMode;

/**
 * 有序节点测试，以知道如何解析它
 *
 * 输出结果为：/online/channel/lock-0000000000
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 21:08
 * @github - https://github.com/hl845740757
 */
public class SequentialNodeTest {

    public static void main(String[] args) throws Exception {
        GameConfigMrg gameConfigMrg = new GameConfigMrg();
        CuratorMrg curatorMrg = new CuratorMrg(gameConfigMrg);
        curatorMrg.start();

        String nodeName = curatorMrg.createNode("/online/channel/lock-", CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(nodeName);
        curatorMrg.shutdown();
    }
}
