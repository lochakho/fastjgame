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

import com.wjybxx.fastjgame.NetBootstrap;
import com.wjybxx.fastjgame.module.CenterModule;

import java.io.File;

/**
 * 启动参数：
 warzoneId=1 serverId=1
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 21:29
 * @github - https://github.com/hl845740757
 */
public class CenterWorldTest {

    public static void main(String[] args) throws Exception {
        String logDir=new File("").getAbsolutePath() + File.separator + "log";
        String logFilePath = logDir + File.separator + "center.log";
        System.setProperty("logFilePath",logFilePath);

        new NetBootstrap<>()
                .setArgs(args)
                .setFramesPerSecond(5)
                .addModule(new CenterModule())
                .start();
    }
}
