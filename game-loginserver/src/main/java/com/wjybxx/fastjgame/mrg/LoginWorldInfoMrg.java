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

package com.wjybxx.fastjgame.mrg;

import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * 登录服需要启动参数吗？
 * 需要指定端口，如果随机端口的话，得查zookeeper才知道绑定的哪个端口，
 * 而且如果用Nginx，每次都可能要改配置。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 20:18
 * @github - https://github.com/hl845740757
 */
public class LoginWorldInfoMrg extends WorldCoreInfoMrg{

    private int port;

    public LoginWorldInfoMrg(GuidMrg guidMrg) {
        super(guidMrg);
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        port=startArgs.getAsInt("port");
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.LOGIN;
    }

    public int getPort() {
        return port;
    }
}
