
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
import com.wjybxx.fastjgame.core.CenterInSceneInfo;
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;
import com.wjybxx.fastjgame.net.async.S2CSession;
import com.wjybxx.fastjgame.world.SceneWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.wjybxx.fastjgame.protobuffer.p_center_scene.*;
import static com.wjybxx.fastjgame.protobuffer.p_center_scene.p_center_cross_scene_hello;
import static com.wjybxx.fastjgame.protobuffer.p_center_scene.p_center_single_scene_hello;

/**
 * CenterServer在SceneServer中的连接管理等。
 * SceneServer总是作为CenterServer的同步Rpc调用服务器；
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 22:08
 * @github - https://github.com/hl845740757
 */
public class CenterInSceneInfoMrg {

    private static final Logger logger= LoggerFactory.getLogger(CenterInSceneInfoMrg.class);
    /**
     * scene接收其它服务器的连接，因此它作为连接的服务器放
     */
    private final S2CSessionMrg s2CSessionMrg;
    private final SyncS2CSessionMrg syncS2CSessionMrg;
    private final SceneWorldInfoMrg sceneWorldInfoMrg;
    private final SceneRegionMrg sceneRegionMrg;

    /**
     * 进程guid到信息的映射
     */
    private final Long2ObjectMap<CenterInSceneInfo> guid2InfoMap=new Long2ObjectOpenHashMap<>();
    /**
     * 服务器id到信息的映射
     */
    private final Int2ObjectMap<CenterInSceneInfo> serverId2InfoMap=new Int2ObjectOpenHashMap<>();

    @Inject
    public CenterInSceneInfoMrg(S2CSessionMrg s2CSessionMrg, SyncS2CSessionMrg syncS2CSessionMrg,
                                SceneWorldInfoMrg sceneWorldInfoMrg, SceneRegionMrg sceneRegionMrg) {
        this.s2CSessionMrg = s2CSessionMrg;
        this.syncS2CSessionMrg = syncS2CSessionMrg;
        this.sceneWorldInfoMrg = sceneWorldInfoMrg;
        this.sceneRegionMrg = sceneRegionMrg;
    }

    private void addInfo(CenterInSceneInfo centerInSceneInfo){
        guid2InfoMap.put(centerInSceneInfo.getCenterProcessGuid(),centerInSceneInfo);
        serverId2InfoMap.put(centerInSceneInfo.getServerId(),centerInSceneInfo);

        logger.info("connect center server {}",centerInSceneInfo.getServerId());
    }

    private void removeInfo(CenterInSceneInfo centerInSceneInfo){
        guid2InfoMap.remove(centerInSceneInfo.getCenterProcessGuid());
        serverId2InfoMap.remove(centerInSceneInfo.getServerId());

        logger.info("remove center server {}",centerInSceneInfo.getServerId());
    }

    /**
     * 检测到center服进程会话断开
     * @param centerProcessGuid
     */
    public void onDisconnect(long centerProcessGuid, SceneWorld sceneWorld){
        CenterInSceneInfo centerInSceneInfo = guid2InfoMap.get(centerProcessGuid);
        if (null == centerInSceneInfo){
            return;
        }
        removeInfo(centerInSceneInfo);

        // 跨服场景，收到某个center服宕机，无所谓
        if (sceneWorldInfoMrg.getSceneProcessType() == SceneProcessType.CROSS){
            return;
        }
        // 单服场景，自己的center服宕机，需要通知所有玩家下线，然后退出
        if (centerInSceneInfo.getServerId() == sceneWorldInfoMrg.getServerId()){
            // TODO 踢掉所有玩家，shutdown
            sceneWorld.requestShutdown();
        }
    }

    /**
     * 收到center打的招呼(认为我是单服节点)
     * @param session 会话信息
     * @param hello 简单信息
     */
    public void p_center_single_scene_hello_handler(S2CSession session, p_center_single_scene_hello hello) {
        assert !guid2InfoMap.containsKey(session.getClientGuid());
        CenterInSceneInfo centerInSceneInfo=new CenterInSceneInfo(session.getClientGuid(),hello.getServerId());
        addInfo(centerInSceneInfo);

        p_center_single_scene_hello_result.Builder builder = p_center_single_scene_hello_result.newBuilder();
        for (SceneRegion sceneRegion:sceneWorldInfoMrg.getConfiguredRegions()){
            builder.addConfiguredRegions(sceneRegion.getNumber());
        }
        s2CSessionMrg.send(session.getClientGuid(),builder.build());
    }

    /**
     * 收到center打的招呼(认为我是跨服节点)
     * @param session 会话信息
     * @param hello 简单信息
     */
    public void p_center_cross_scene_hello_handler(S2CSession session,p_center_cross_scene_hello hello){
        assert !guid2InfoMap.containsKey(session.getClientGuid());

        p_center_cross_scene_hello_result.Builder builder = p_center_cross_scene_hello_result.newBuilder();
        // 配置的区域
        for (SceneRegion sceneRegion:sceneWorldInfoMrg.getConfiguredRegions()){
            builder.addConfiguredRegions(sceneRegion.getNumber());
        }
        // 实际激活的区域
        for (SceneRegion sceneRegion:sceneRegionMrg.getActiveRegions()){
            builder.addConfiguredRegions(sceneRegion.getNumber());
        }
        s2CSessionMrg.send(session.getClientGuid(),builder.build());
    }
}
