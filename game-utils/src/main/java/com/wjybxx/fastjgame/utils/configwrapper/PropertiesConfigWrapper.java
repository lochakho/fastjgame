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
import java.util.HashMap;
import java.util.Properties;

/**
 * 基于Properties的配置的包装器。
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 12:56
 * @github - https://github.com/hl845740757
 */
@Immutable
public final class PropertiesConfigWrapper extends ConfigWrapper {

    private final Properties properties;

    public PropertiesConfigWrapper(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getAsString(String key){
        return properties.getProperty(key);
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        HashMap<String,String> map=new HashMap<>();
        for (String name:properties.stringPropertyNames()){
            map.put(name,properties.getProperty(name));
        }
        return new MapConfigWrapper(map);
    }


    @Override
    public String toString() {
        return "PropertiesConfigWrapper{" +
                "properties=" + properties +
                '}';
    }
}
