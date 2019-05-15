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

import com.wjybxx.fastjgame.tablereader.TableRow;
import com.wjybxx.fastjgame.tablereader.TableSheet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于Excel或CSV的配置包装对象。
 * 格式为键值对类型的表格可以转换为该类型
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 9:45
 * @github - https://github.com/hl845740757
 */
public class TableSheetConfigWrapper extends ConfigWrapper{

    private final String fileName;

    private final int sheetIndex;

    private final String keyColName;

    private final String valueColName;

    private final Map<String,String> indexedContent;

    private TableSheetConfigWrapper(String fileName, int sheetIndex,
                                   String keyColName, String valueColName,
                                   Map<String,String> indexedContent) {
        this.fileName = fileName;
        this.sheetIndex = sheetIndex;
        this.keyColName = keyColName;
        this.valueColName = valueColName;
        this.indexedContent = Collections.unmodifiableMap(indexedContent);
    }

    @Override
    public String getAsString(String key) {
        return indexedContent.get(key);
    }

    @Override
    public MapConfigWrapper convert2MapWrapper() {
        return new MapConfigWrapper(indexedContent);
    }

    @Override
    public String toString() {
        return "TableSheetConfigWrapper{" +
                "fileName='" + fileName + '\'' +
                ", sheetIndex=" + sheetIndex +
                ", keyColName='" + keyColName + '\'' +
                ", valueColName='" + valueColName + '\'' +
                ", indexedContentSize=" + indexedContent.size() +
                '}';
    }

    public String getFileName() {
        return fileName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public String getKeyColName() {
        return keyColName;
    }

    public String getValueColName() {
        return valueColName;
    }

    /**
     * 创建一个表格包装对象，只包含tableSheet指定的两列
     * @param tableSheet 表格
     * @param keyColName key所在列列名，key列配置必须唯一
     * @param valueColName value所在列列名
     * @return
     */
    public static TableSheetConfigWrapper create(TableSheet tableSheet, String keyColName, String valueColName){
        Map<String, String> indexedContent = createIndex(tableSheet, keyColName, valueColName);
        return new TableSheetConfigWrapper(tableSheet.getFileName(),tableSheet.getSheetIndex(),
                keyColName,valueColName,
                indexedContent);
    }

    /**
     * 创建key-value的索引，避免使用时的大量遍历
     */
    private static Map<String,String> createIndex(TableSheet tableSheet, String keyColName, String valueColName){
        // 使用LinkedHashMap保持原顺序
        Map<String,String> result=new LinkedHashMap<>();
        for (TableRow tableRow:tableSheet.getContentRows()){
            String key = tableRow.getAsString(keyColName);
            // 不可以有重复键
            if (result.containsKey(key)){
                throw new IllegalArgumentException("keyCol hash duplicate key " + key);
            }
            result.put(key,tableRow.getAsString(valueColName));
        }
        return Collections.unmodifiableMap(result);
    }
}
