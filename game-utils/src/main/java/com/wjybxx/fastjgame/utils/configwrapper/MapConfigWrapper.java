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

package com.wjybxx.fastjgame.utils.configwrapper;

import javax.annotation.concurrent.Immutable;
import java.util.*;

/**
 * 基于map的配置的包装器
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 13:35
 * @github - https://github.com/hl845740757
 */
@Immutable
public class MapConfigWrapper extends ConfigWrapper {
    /**
     * 空map配置包装对象
     */
    @SuppressWarnings("unchecked")
    public static MapConfigWrapper EMPTY_MAP_WRAPPER=new MapConfigWrapper(Collections.EMPTY_MAP);

    private final Map<String,String> configMap;

    public MapConfigWrapper(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    @Override
    public String getAsString(String key) {
        return configMap.get(key);
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        return this;
    }

    /**
     * 使用{@code other}中的参数替换当前Config中的属性。
     * 它不会修改当前对象，返回的是拥有两者参数的一个新对象。
     * @param other
     * @return
     */
    public final MapConfigWrapper replaceAll(MapConfigWrapper other){
        Map<String,String> map=new HashMap<>(configMap);
        map.putAll(other.configMap);
        return new MapConfigWrapper(map);
    }

    /**
     * 获取所有用于的属性名。
     * @return unmodifiableSet
     */
    public final Set<String> keys(){
        return Collections.unmodifiableSet(configMap.keySet());
    }

    @Override
    public String toString() {
        return "MapConfigWrapper{" +
                "configMap=" + configMap +
                '}';
    }
}
