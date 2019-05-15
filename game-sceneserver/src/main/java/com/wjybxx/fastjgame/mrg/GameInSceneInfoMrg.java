
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
import com.wjybxx.fastjgame.mrg.async.C2SSessionMrg;
import com.wjybxx.fastjgame.mrg.async.S2CSessionMrg;
import com.wjybxx.fastjgame.mrg.sync.SyncS2CSessionMrg;

/**
 * GameServer在SceneServer中的连接管理等。
 * SceneServer总是作为GameServer的同步Rpc调用服务器；
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 22:08
 * @github - https://github.com/hl845740757
 */
public class GameInSceneInfoMrg {

    private final S2CSessionMrg s2CSessionMrg;
    private final C2SSessionMrg c2SSessionMrg;
    private final SyncS2CSessionMrg syncS2CSessionMrg;

    @Inject
    public GameInSceneInfoMrg(S2CSessionMrg s2CSessionMrg, C2SSessionMrg c2SSessionMrg, SyncS2CSessionMrg syncS2CSessionMrg) {
        this.s2CSessionMrg = s2CSessionMrg;
        this.c2SSessionMrg = c2SSessionMrg;
        this.syncS2CSessionMrg = syncS2CSessionMrg;
    }
}
