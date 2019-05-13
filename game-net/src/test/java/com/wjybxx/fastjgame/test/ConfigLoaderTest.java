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

import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.utils.ConfigLoader;

import java.io.IOException;

/**
 * configLoader测试(测试是否是将两个配置文件合并了)
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/26 22:54
 */
public class ConfigLoaderTest {

    public static void main(String[] args) throws IOException {
        ConfigWrapper configWrapper = ConfigLoader.loadConfig(ConfigLoaderTest.class.getClassLoader(), NetConstants.NET_CONFIG_NAME);

        System.out.println(configWrapper.getAsString("ringBufferSize"));
        System.out.println(configWrapper.getAsInt("maxIoThreadNum"));
    }
}
