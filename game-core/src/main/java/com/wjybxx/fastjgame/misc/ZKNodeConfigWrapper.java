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

package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.configwrapper.ConfigWrapper;
import com.wjybxx.fastjgame.configwrapper.MapConfigWrapper;
import com.wjybxx.fastjgame.utils.ZKPathUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * zookeeper节点下的配置包装器。
 * 可以方便的直接使用子节点的<b>节点名字</b>来获取数据。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/21 17:09
 * @github - https://github.com/hl845740757
 */
public class ZKNodeConfigWrapper extends ConfigWrapper {

    private final Map<String,byte[]> childrenData;

    public ZKNodeConfigWrapper(Map<String, byte[]> childrenData) {
        this.childrenData = getRealMap(childrenData);
    }

    @Override
    public String getAsString(String key) {
        if (childrenData.containsKey(key)){
            return new String(childrenData.get(key), StandardCharsets.UTF_8);
        }else {
            return null;
        }
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        Map<String,String> resultMap=new HashMap<>();
        for (Map.Entry<String,byte[]> entry:childrenData.entrySet()){
            resultMap.put(entry.getKey(),new String(entry.getValue(),StandardCharsets.UTF_8));
        }
        return new MapConfigWrapper(resultMap);
    }


    @Override
    public String toString() {
        return "ZKNodeConfigWrapper{" +
                "children=" + childrenData.keySet() +
                '}';
    }

    private static Map<String, byte[]> getRealMap(Map<String,byte[]> childrenData){
        Map<String,byte[]> realChildrenData=new HashMap<>(childrenData.size()+1,1);
        for (Map.Entry<String,byte[]> entry:childrenData.entrySet()){
            String childName = ZKPathUtils.findNodeName(entry.getKey());
            byte[] childData = entry.getValue();
            realChildrenData.put(childName, childData);
        }
        return realChildrenData;
    }
}
