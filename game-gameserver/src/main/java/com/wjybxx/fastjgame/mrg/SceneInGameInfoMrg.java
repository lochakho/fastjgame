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
import com.wjybxx.fastjgame.core.SceneInGameInfo;
import com.wjybxx.fastjgame.core.ZKOnlineSceneNode;
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncC2SSessionMrg;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * SceneServer在GameServer中的连接等控制器。
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:11
 * @github - https://github.com/hl845740757
 */
public class SceneInGameInfoMrg {

    private final S2CSessionMrg s2CSessionMrg;
    private final C2SSessionMrg c2SSessionMrg;
    private final SyncC2SSessionMrg syncC2SSessionMrg;
    /**
     * scene信息集合
     * sceneGuid->sceneInfo
     */
    private final Long2ObjectMap<SceneInGameInfo> guid2InfoMap=new Long2ObjectOpenHashMap<>();
    /**
     * channelId->sceneInfo
     */
    private final Int2ObjectMap<SceneInGameInfo> channelId2InfoMap=new Int2ObjectOpenHashMap<>();

    @Inject
    public SceneInGameInfoMrg(S2CSessionMrg s2CSessionMrg, C2SSessionMrg c2SSessionMrg, SyncC2SSessionMrg syncC2SSessionMrg) {

        this.s2CSessionMrg = s2CSessionMrg;
        this.c2SSessionMrg = c2SSessionMrg;
        this.syncC2SSessionMrg = syncC2SSessionMrg;
    }

    /**
     * 当在zk上发现scene节点
     * @param onlineSceneNode scene注册的信息
     */
    public void onDiscoverScene(ZKOnlineSceneNode onlineSceneNode){

    }

    /**
     * 当与scene断开连接(异步tcp会话断掉，或zk节点消失)
     */
    public void onSceneDisconnect(long sceneProcessGuid){

    }
}
