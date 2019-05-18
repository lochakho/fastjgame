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
import com.wjybxx.fastjgame.core.CenterInWarzoneInfo;
import com.wjybxx.fastjgame.misc.PlatformType;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.net.async.S2CSession;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

import static com.wjybxx.fastjgame.protobuffer.p_center_warzone.p_center_warzone_hello;
import static com.wjybxx.fastjgame.protobuffer.p_center_warzone.p_center_warzone_hello_result;

/**
 * Center在Warzone中的控制器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 15:43
 * @github - https://github.com/hl845740757
 */
public class CenterInWarzoneInfoMrg {

    private static final Logger logger= LoggerFactory.getLogger(CenterInWarzoneInfoMrg.class);
    /**
     * 由于是center发起的连接，因此warzone作为服务方。
     */
    private final S2CSessionMrg s2CSessionMrg;
    /**
     * guid -> info
     */
    private final Long2ObjectMap<CenterInWarzoneInfo> guid2InfoMap=new Long2ObjectOpenHashMap<>();
    /**
     * plat -> serverId -> info
     */
    private final Map<PlatformType,Int2ObjectMap<CenterInWarzoneInfo>> platInfoMap=new EnumMap<>(PlatformType.class);

    @Inject
    public CenterInWarzoneInfoMrg(S2CSessionMrg s2CSessionMrg) {
        this.s2CSessionMrg = s2CSessionMrg;
    }

    private void addInfo(CenterInWarzoneInfo centerInWarzoneInfo){
        guid2InfoMap.put(centerInWarzoneInfo.getGameProcessGuid(),centerInWarzoneInfo);

        Int2ObjectMap<CenterInWarzoneInfo> serverId2InfoMap = platInfoMap.computeIfAbsent(centerInWarzoneInfo.getPlatformType(),
                platformType -> new Int2ObjectOpenHashMap<>());
        serverId2InfoMap.put(centerInWarzoneInfo.getServerId(),centerInWarzoneInfo);

        logger.info("server {}-{} register success.",centerInWarzoneInfo.getPlatformType(),centerInWarzoneInfo.getServerId());
    }

    private void removeInfo(CenterInWarzoneInfo centerInWarzoneInfo){
        guid2InfoMap.remove(centerInWarzoneInfo.getGameProcessGuid());
        platInfoMap.get(centerInWarzoneInfo.getPlatformType()).remove(centerInWarzoneInfo.getServerId());

        logger.info("server {}-{} disconnect.",centerInWarzoneInfo.getPlatformType(),centerInWarzoneInfo.getServerId());
    }

    public void p_center_warzone_hello_handler(S2CSession session, p_center_warzone_hello hello) {
        PlatformType platformType=PlatformType.forNumber(hello.getPlatfomNumber());
        assert !guid2InfoMap.containsKey(session.getClientGuid());
        assert !platInfoMap.containsKey(platformType) || !platInfoMap.get(platformType).containsKey(hello.getServerId());

        CenterInWarzoneInfo centerInWarzoneInfo=new CenterInWarzoneInfo(session.getClientGuid(), platformType, hello.getServerId());
        addInfo(centerInWarzoneInfo);
        s2CSessionMrg.send(session.getClientGuid(), p_center_warzone_hello_result.newBuilder().build());
    }

    public void onCenterServerDisconnect(S2CSession session){
        CenterInWarzoneInfo centerInWarzoneInfo = guid2InfoMap.get(session.getClientGuid());
        if (null==centerInWarzoneInfo){
            return;
        }
        removeInfo(centerInWarzoneInfo);
    }
}
