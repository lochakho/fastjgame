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
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.net.sync.SyncS2CSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

import static com.wjybxx.fastjgame.protobuffer.p_sync_center_scene.*;

/**
 * 场景区域管理器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/16 11:35
 * @github - https://github.com/hl845740757
 */
public class SceneRegionMrg {

    private static final Logger logger= LoggerFactory.getLogger(SceneRegionMrg.class);

    private final SceneWorldInfoMrg sceneWorldInfoMrg;
    /**
     * 已激活的场景区域
     */
    private final Set<SceneRegion> activeRegions= EnumSet.noneOf(SceneRegion.class);

    @Inject
    public SceneRegionMrg(SceneWorldInfoMrg sceneWorldInfoMrg) {
        this.sceneWorldInfoMrg = sceneWorldInfoMrg;
    }

    public void onWorldStart(){
        if (sceneWorldInfoMrg.getSceneProcessType() == SceneProcessType.SINGLE){
            activeSingleSceneNormalRegions();
        }else {
            activeAllCrossSceneRegions();
        }
    }

    /**
     * 启动所有跨服场景(目前跨服场景不做互斥)
     */
    private void activeAllCrossSceneRegions(){
        for (SceneRegion sceneRegion:sceneWorldInfoMrg.getConfiguredRegions()){
            activeOneRegion(sceneRegion);
        }
    }

    /**
     * 启动所有本服普通场景
     */
    private void activeSingleSceneNormalRegions(){
        for (SceneRegion sceneRegion:sceneWorldInfoMrg.getConfiguredRegions()){
            // 互斥区域等待centerserver通知再启动
            if (sceneRegion.isMutex()){
                continue;
            }
            activeOneRegion(sceneRegion);
        }
    }

    /**
     * 激活一个region，它就做一件事，创建该region里拥有的城镇。
     * @param sceneRegion 场景区域
     */
    private void activeOneRegion(SceneRegion sceneRegion){
        logger.info("try active region {}.",sceneRegion);
        try{
            // TODO 激活区域
            logger.info("active region {} success.",sceneRegion);
        }catch (Exception e){
            // 这里一定不能出现异常
            logger.error("active region {} caught exception.",sceneRegion,e);
        }
    }

    /**
     * 收到game的启动命令
     * @param session 与game的会话
     * @param command game发来的启动命令
     * @return 启动成功
     */
    @Nonnull
    public p_center_command_single_scene_start_result p_center_command_single_scene_start_handler(SyncS2CSession session, p_center_command_single_scene_start command){
        assert sceneWorldInfoMrg.getSceneProcessType() == SceneProcessType.SINGLE;
        for (int regionId:command.getActiveMutexRegionsList()){
            SceneRegion sceneRegion=SceneRegion.forNumber(regionId);
            if (activeRegions.contains(sceneRegion)){
                continue;
            }
            activeOneRegion(sceneRegion);
        }
        return p_center_command_single_scene_start_result.newBuilder().build();
    }

    /**
     * 收到game的激活区域命名(宕机恢复，挂载其他场景进程宕掉的区域)
     * @param session 与game的会话
     * @param command game发来的命令
     * @return 启动成功
     */
    @Nonnull
    public p_center_command_single_scene_active_regions_result p_center_command_scene_active_regions_handler(SyncS2CSession session,p_center_command_single_scene_active_regions command){
        assert sceneWorldInfoMrg.getSceneProcessType() == SceneProcessType.SINGLE;
        for (int regionId:command.getActiveRegionsList()){
            SceneRegion sceneRegion=SceneRegion.forNumber(regionId);
            if (activeRegions.contains(sceneRegion)){
                continue;
            }
            activeOneRegion(sceneRegion);
        }
        return p_center_command_single_scene_active_regions_result.newBuilder().build();
    }
}
