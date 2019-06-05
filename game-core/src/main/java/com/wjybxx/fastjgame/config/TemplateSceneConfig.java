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

package com.wjybxx.fastjgame.config;

import com.wjybxx.fastjgame.core.SceneRegion;

/**
 * 游戏场景配置信息，和地图配置信息组合，可以更好的复用地图资源；
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 13:14
 * @github - https://github.com/hl845740757
 */
public class TemplateSceneConfig {

    /**
     * 对应的地图资源
     */
    public final int mapId;

    /**
     * 视野范围
     */
    public final int viewableRange;

    /**
     * 所属的场景区域
     */
    public final SceneRegion sceneRegion;

    // 地图元数据，遮挡信息等，由于文件较大，按需加载，不在加载配置阶段进行加载

    public TemplateSceneConfig(int mapId, int viewableRange, SceneRegion sceneRegion) {
        this.sceneRegion = sceneRegion;
        this.viewableRange = viewableRange;
        this.mapId = mapId;
    }

}
