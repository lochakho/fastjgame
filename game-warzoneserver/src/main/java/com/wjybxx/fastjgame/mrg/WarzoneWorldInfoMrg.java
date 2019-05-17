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

import com.google.inject.Inject;
import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.net.common.RoleType;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 15:40
 * @github - https://github.com/hl845740757
 */
public class WarzoneWorldInfoMrg extends WorldCoreInfoMrg{

    private int warzoneId;

    @Inject
    public WarzoneWorldInfoMrg(GuidMrg guidMrg) {
        super(guidMrg);
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        warzoneId=startArgs.getAsInt("warzoneId");
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.WARZONE_SERVER;
    }

    public int getWarzoneId() {
        return warzoneId;
    }
}
