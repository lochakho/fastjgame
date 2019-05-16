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
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.net.common.RoleType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 10:47
 * @github - https://github.com/hl845740757
 */
public class SceneWorldInfoMrg extends WorldCoreInfoMrg{

    private SceneProcessType sceneProcessType;

    private int warzoneId;

    private int serverId;
    /**
     * 复合当前进程类型的场景区域
     */
    private Set<SceneRegion> configuredRegions=EnumSet.noneOf(SceneRegion.class);

    @Inject
    public SceneWorldInfoMrg(GuidMrg guidMrg) {
        super(guidMrg);
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        sceneProcessType = SceneProcessType.forName(startArgs.getAsString("sceneType"));
        warzoneId = startArgs.getAsInt("warzoneId");

        // 只有本服场景才支持指定服务器id
        if(sceneProcessType == SceneProcessType.SINGLE){
            serverId = startArgs.getAsInt("serverId");
        }else {
            serverId = -1;
        }

        // 配置的要启动的区域 TODO 这种配置方式不方便配置
        String[] configuredRegionArray = startArgs.getAsStringArray("configuredRegions");
        for (String regionName:configuredRegionArray){
            SceneRegion sceneRegion = SceneRegion.valueOf(regionName);
            if (sceneRegion.getSceneProcessType()!= sceneProcessType){
                throw new IllegalArgumentException(sceneProcessType + " doesn't support " + sceneRegion);
            }
            configuredRegions.add(sceneRegion);
        }
    }

    @Override
    public RoleType processType() {
        return RoleType.SCENE_SERVER;
    }

    public SceneProcessType getSceneProcessType() {
        return sceneProcessType;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    /**
     * 获取serverId之前一定要检查SceneType
     * @return 如果是本服场景，则存在，否则抛出异常
     */
    public int getServerId() {
        if (serverId>0){
            return serverId;
        }
        throw new UnsupportedOperationException("cross scene serverId");
    }

    public Set<SceneRegion> getConfiguredRegions() {
        return Collections.unmodifiableSet(configuredRegions);
    }
}
