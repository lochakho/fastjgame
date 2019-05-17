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
import com.wjybxx.fastjgame.core.CenterInLoginInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * CenterServer在LoginServer端的信息
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/17 21:23
 * @github - https://github.com/hl845740757
 */
public class CenterInLoginInfoMrg {

    /**
     * 服id->服信息的映射，暂时未涉及跨平台
     */
    private final Int2ObjectMap<CenterInLoginInfo> serverId2InfoMap=new Int2ObjectOpenHashMap<>();

    @Inject
    public CenterInLoginInfoMrg() {

    }
}
