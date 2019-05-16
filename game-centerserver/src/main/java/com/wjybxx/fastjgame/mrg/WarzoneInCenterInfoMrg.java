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
import com.wjybxx.fastjgame.core.ZKOnlineWarzoneNode;
import com.wjybxx.fastjgame.core.parserresult.WarzoneNodeName;

/**
 * Warzone在Game中的连接管理等控制器
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/15 23:11
 * @github - https://github.com/hl845740757
 */
public class WarzoneInCenterInfoMrg {

    @Inject
    public WarzoneInCenterInfoMrg() {

    }

    /**
     * 发现战区出现(zk上出现了该服务器对应的战区节点)
     * @param warzoneNodeName 战区节点名字信息
     * @param zkOnlineWarzoneNode  战区其它信息
     */
    public void onDiscoverWarzone(WarzoneNodeName warzoneNodeName, ZKOnlineWarzoneNode zkOnlineWarzoneNode){

    }

    /**
     * 发现战区断开连接(异步tcp会话断掉，或zk节点消失)
     * @param warzoneNodeName 战区节点名字信息
     */
    public void onWarzoneNodeRemoved(WarzoneNodeName warzoneNodeName){

    }
}
