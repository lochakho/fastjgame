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
import com.wjybxx.fastjgame.scene.MapData;

/**
 * @author wjybxx
 * @version 1.0
 * @date 2019/6/4 13:14
 * @github - https://github.com/hl845740757
 */
public class TemplateMapConfig {

    /**
     * 所属的场景区域
     */
    public final SceneRegion sceneRegion;

    /**
     * 视野范围
     */
    public final int viewableRange;

    /**
     * 地图元数据，应该由地图编辑器导出
     */
    public final MapData mapData;

    public TemplateMapConfig(SceneRegion sceneRegion, int viewableRange, MapData mapData) {
        this.sceneRegion = sceneRegion;
        this.viewableRange = viewableRange;
        this.mapData = mapData;
    }

}
