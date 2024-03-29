
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
import com.wjybxx.fastjgame.constants.NetConstants;
import com.wjybxx.fastjgame.core.CenterInSceneInfo;
import com.wjybxx.fastjgame.core.SceneProcessType;
import com.wjybxx.fastjgame.core.SceneRegion;
import com.wjybxx.fastjgame.misc.PlatformType;
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

import java.util.EnumMap;
import java.util.Map;

import static com.wjybxx.fastjgame.protobuffer.p_center_scene.*;

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
     * scene接收其它服务器的连接，因此它作为连接的服务器方
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
     * 平台 -> serverId -> 信息
     */
    private final Map<PlatformType,Int2ObjectMap<CenterInSceneInfo>> platInfoMap=new EnumMap<>(PlatformType.class);

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
        Int2ObjectMap<CenterInSceneInfo> serverId2InfoMap = platInfoMap.computeIfAbsent(centerInSceneInfo.getPlatformType(),
                        platformType -> new Int2ObjectOpenHashMap<>());
        serverId2InfoMap.put(centerInSceneInfo.getServerId(),centerInSceneInfo);

        logger.info("connect center server {}-{}",centerInSceneInfo.getPlatformType(),centerInSceneInfo.getServerId());
    }

    private void removeInfo(CenterInSceneInfo centerInSceneInfo){
        guid2InfoMap.remove(centerInSceneInfo.getCenterProcessGuid());
        platInfoMap.get(centerInSceneInfo.getPlatformType()).remove(centerInSceneInfo.getServerId());

        logger.info("remove center server {}-{}",centerInSceneInfo.getPlatformType(),centerInSceneInfo.getServerId());
    }

    /**
     * 检测到center服进程会话断开
     * @param centerProcessGuid center服务器进程guid
     */
    public void onDisconnect(long centerProcessGuid, SceneWorld sceneWorld){
        CenterInSceneInfo centerInSceneInfo = guid2InfoMap.get(centerProcessGuid);
        if (null == centerInSceneInfo){
            return;
        }
        removeInfo(centerInSceneInfo);

        // 跨服场景，收到某个center服宕机，无所谓(跨服场景会链接很多个center服)
        if (sceneWorldInfoMrg.getSceneProcessType() == SceneProcessType.CROSS){
            // 将该服的玩家下线
            offlineSpecialCenterPlayer(centerInSceneInfo.getPlatformType(),centerInSceneInfo.getServerId());

            // TODO 如果所有服都断开了，且一段时间内没有服务器接入，需要关闭
            return;
        }

        // 单服场景(讲道理单服场景只会连接自己的服，因此这里必须成立)，自己的center服宕机，需要通知所有玩家下线，然后退出
        assert centerInSceneInfo.getPlatformType() == sceneWorldInfoMrg.getPlatformType();
        assert centerInSceneInfo.getServerId() == sceneWorldInfoMrg.getServerId();
        offlineAllOnlinePlayer();
        sceneWorld.requestShutdown();
    }

    /**
     * 将制定平台制定服的玩家下线
     * @param platformType 平台
     * @param serverId 区服
     */
    private void offlineSpecialCenterPlayer(PlatformType platformType, int serverId){

    }

    /**
     * 踢掉当前场景所有在线玩家
     */
    private void offlineAllOnlinePlayer(){
        // TODO 踢掉所有玩家，shutdown
    }

    /**
     * 收到center打的招呼(认为我是单服节点)
     * @param session 会话信息
     * @param hello 简单信息
     */
    public void p_center_single_scene_hello_handler(S2CSession session, p_center_single_scene_hello hello) {
        PlatformType platformType=PlatformType.forNumber(hello.getPlatfomNumber());
        assert !guid2InfoMap.containsKey(session.getClientGuid());
        assert !platInfoMap.containsKey(platformType) || !platInfoMap.get(platformType).containsKey(hello.getServerId());

        CenterInSceneInfo centerInSceneInfo=new CenterInSceneInfo(session.getClientGuid(), platformType, hello.getServerId());
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
        PlatformType platformType=PlatformType.forNumber(hello.getPlatfomNumber());
        assert !guid2InfoMap.containsKey(session.getClientGuid());
        assert !platInfoMap.containsKey(platformType) || !platInfoMap.get(platformType).containsKey(hello.getServerId());

        CenterInSceneInfo centerInSceneInfo=new CenterInSceneInfo(session.getClientGuid(), platformType, hello.getServerId());
        addInfo(centerInSceneInfo);

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

    public long getCenterGuid(PlatformType platformType,int serverId){
        Int2ObjectMap<CenterInSceneInfo> serverId2InfoMap = platInfoMap.get(platformType);
        if (null == serverId2InfoMap){
            return NetConstants.INVALID_SESSION_ID;
        }
        CenterInSceneInfo centerInSceneInfo = serverId2InfoMap.get(serverId);
        return null == centerInSceneInfo? NetConstants.INVALID_SESSION_ID : centerInSceneInfo.getCenterProcessGuid();
    }
}
