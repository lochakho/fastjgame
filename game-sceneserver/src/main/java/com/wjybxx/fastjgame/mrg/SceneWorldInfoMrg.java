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
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.net.common.RoleType;
import com.wjybxx.fastjgame.utils.GameUtils;
import com.wjybxx.fastjgame.utils.ZKPathUtils;
import org.apache.zookeeper.CreateMode;

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

    private static final int SINGLE_SCENE_MIN_CHANNEL_ID=1;
    private static final int CROSS_SCENE_MIN_CHANNEL_ID=10001;

    /**
     * scene进程类型
     */
    private SceneProcessType sceneProcessType;
    /**
     * 所属的战区
     */
    private int warzoneId;

    /**
     * 如果是单服进程，表示它所属的平台
     */
    private PlatformType platformType;
    /**
     * 如果的单服进程，那么表示它所属的服务器
     */
    private int serverId;
    /**
     * 频道id，自动生成会比较好
     */
    private int channelId;
    /**
     * 复合当前进程类型的场景区域
     */
    private Set<SceneRegion> configuredRegions;

    private final CuratorMrg curatorMrg;

    @Inject
    public SceneWorldInfoMrg(GuidMrg guidMrg, CuratorMrg curatorMrg) {
        super(guidMrg);
        this.curatorMrg=curatorMrg;
    }

    @Override
    protected void initImp(ConfigWrapper startArgs) throws Exception {
        // 场景进程类型决定参数配置
        sceneProcessType = SceneProcessType.forName(startArgs.getAsString("sceneType"));

        if(sceneProcessType == SceneProcessType.SINGLE){
            // 本服场景配置平台和服id，战区id由zookeeper配置指定
            platformType = PlatformType.valueOf(startArgs.getAsString("platform"));
            serverId = startArgs.getAsInt("serverId");

            // 查询zookeeper，获取该平台该服对应的战区id
            String actualServerConfigPath = ZKPathUtils.actualServerConfigPath(platformType, serverId);
            ConfigWrapper serverConfig  = new MapConfigWrapper(GameUtils.newJsonMap(curatorMrg.getData(actualServerConfigPath)));
            warzoneId = serverConfig.getAsInt("warzoneId");

            final String originPath = ZKPathUtils.singleChannelPath(warzoneId, serverId);
            this.initChannelId(originPath,SINGLE_SCENE_MIN_CHANNEL_ID);
        }else {
            // 跨服场景的 战区id由启动参数决定
            warzoneId = startArgs.getAsInt("warzoneId");
            serverId = -1;

            String originPath = ZKPathUtils.crossChannelPath(warzoneId);
            this.initChannelId(originPath,CROSS_SCENE_MIN_CHANNEL_ID);
        }

        // 配置的要启动的区域 TODO 这种配置方式不方便配置
        String[] configuredRegionArray = startArgs.getAsStringArray("configuredRegions");
        Set<SceneRegion> modifiableRegionSet=EnumSet.noneOf(SceneRegion.class);
        for (String regionName:configuredRegionArray){
            SceneRegion sceneRegion = SceneRegion.valueOf(regionName);
            if (sceneRegion.getSceneProcessType()!= sceneProcessType){
                throw new IllegalArgumentException(sceneProcessType + " doesn't support " + sceneRegion);
            }
            modifiableRegionSet.add(sceneRegion);
        }
        this.configuredRegions=Collections.unmodifiableSet(modifiableRegionSet);
    }

    @Override
    public RoleType getProcessType() {
        return RoleType.SCENE;
    }

    public SceneProcessType getSceneProcessType() {
        return sceneProcessType;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public int getWarzoneId() {
        return warzoneId;
    }

    /**
     * 获取serverId之前一定要检查SceneType
     * @return 如果是本服场景，则存在，否则抛出异常
     */
    public int getServerId() {
        if (sceneProcessType == SceneProcessType.SINGLE){
            return serverId;
        }
        throw new UnsupportedOperationException("cross scene serverId");
    }

    public Set<SceneRegion> getConfiguredRegions() {
        return configuredRegions;
    }

    public int getChannelId() {
        return channelId;
    }

    /**
     * 初始化channelId
     * @param originPath 创建的节点原始路径，因为临时节点，实际路径名会不一样
     * @param startChannelId 其实channelId
     * @throws Exception zk errors
     */
    private void initChannelId(String originPath,int startChannelId) throws Exception {
        final String parent = ZKPathUtils.findParentPath(originPath);
        final String lockPath = ZKPathUtils.findAppropriateLockPath(parent);
        // 如果父节点存在，且没有子节点，则先删除，让序号初始化为0,再创建节点
        // 整个是一个先检查后执行的逻辑，因此需要加锁，保证整个操作的原子性
        curatorMrg.actionWhitLock(lockPath,lockPath1 -> {
            curatorMrg.deleteNodeIfNoChild(parent);
            String realPath=curatorMrg.createNode(originPath,CreateMode.EPHEMERAL_SEQUENTIAL);
            this.channelId = startChannelId + ZKPathUtils.parseSequentialId(realPath);
        });

    }
}
