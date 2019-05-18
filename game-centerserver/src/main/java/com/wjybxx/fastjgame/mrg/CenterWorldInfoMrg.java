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
import com.wjybxx.fastjgame.configwrapper.MapConfigWrapper;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:30
 * @github - https://github.com/hl845740757
 */
public class CenterWorldInfoMrg extends WorldCoreInfoMrg{

    private final CuratorMrg curatorMrg;
    /**
     * 真实服信息配置
     */
    private ConfigWrapper actualServerConfig;
    /**
     * 逻辑服信息配置
     */
    private ConfigWrapper logicServerConfig;
    /**
     * 从属的战区
     */
    private int warzoneId;
    /**
     * 所属的运营平台
     */
    private PlatformType platformType;
    /**
     * 服id
     */
    private int serverId;

    @Inject
    public CenterWorldInfoMrg(GuidMrg guidMrg, CuratorMrg curatorMrg) {
        super(guidMrg);
        this.curatorMrg = curatorMrg;
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        platformType=PlatformType.valueOf(startArgs.getAsString("platform"));
        serverId=startArgs.getAsInt("serverId");

        String actualServerPath= ZKPathUtils.actualServerConfigPath(platformType,serverId);
        this.actualServerConfig =new MapConfigWrapper(GameUtils.newJsonMap(curatorMrg.getData(actualServerPath)));
        String logicServerPath=ZKPathUtils.logicServerConfigPath(platformType,serverId);
        this.logicServerConfig =new MapConfigWrapper(GameUtils.newJsonMap(curatorMrg.getData(logicServerPath)));

        // 战区通过zookeeper节点获取
        warzoneId= actualServerConfig.getAsInt("warzoneId");
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.CENTER;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    public int getServerId() {
        return serverId;
    }

    public ConfigWrapper getActualServerConfig() {
        return actualServerConfig;
    }

    public ConfigWrapper getLogicServerConfig() {
        return logicServerConfig;
    }
}
