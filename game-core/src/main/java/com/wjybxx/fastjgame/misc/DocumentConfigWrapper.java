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
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 适用于全部是字符串键值对的{@link Document}
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/22 11:13
 * @github - https://github.com/hl845740757
 */
public class DocumentConfigWrapper extends ConfigWrapper {

    private final Document document;

    public DocumentConfigWrapper(Document document) {
        this.document = document;
    }

    @Override
    public String getAsString(String key) {
        Object value = document.get(key);
        return null==value?null:String.valueOf(value);
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        Map<String,String> result=new LinkedHashMap<>();
        for (String key:document.keySet()){
            result.put(key,getAsString(key));
        }
        return new MapConfigWrapper(result);
    }

    @Override
    public String toString() {
        return document.toString();
    }
}
