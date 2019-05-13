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

package com.wjybxx.fastjgame.configwrapper;


import com.wjybxx.fastjgame.constants.UtilConstants;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 基于数组的键值对配置的包装器。
 * 数组的每一个元素都是一个键值对 key=value，如：[a=1,b=2]
 * @author wjybxx
 * @version 1.0
 * @date 2019/4/27 13:36
 * @github - https://github.com/hl845740757
 */
@Immutable
public class ArrayConfigWrapper extends ConfigWrapper {

    private final String[] pairsArray;

    public ArrayConfigWrapper(String[] pairsArray) {
        this.pairsArray = pairsArray;
    }

    @Override
    public String getAsString(String key) {
        for (String pair:pairsArray){
            String[] keyValuePair = pair.split(UtilConstants.KEY_VALUE_DELIMITER,2);
            if (keyValuePair[0].equals(key)){
                return keyValuePair.length==2?keyValuePair[1]:null;
            }
        }
        return null;
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        HashMap<String,String> map=new HashMap<>();
        for (String pair:pairsArray){
            String[] keyValuePair = pair.split(UtilConstants.KEY_VALUE_DELIMITER,2);
            if (keyValuePair.length==2){
                map.put(keyValuePair[0],keyValuePair[1]);
            }else {
                map.put(keyValuePair[0],null);
            }
        }
        return new MapConfigWrapper(map);
    }

    @Override
    public String toString() {
        return "ArrayConfigWrapper{" +
                "pairsArray=" + Arrays.toString(pairsArray) +
                '}';
    }
}
