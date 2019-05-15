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
package com.wjybxx.fastjgame.tablereader;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

import java.util.Set;

/**
 * Excel或CSV表格的属性名所在行。
 * 请查看{@link TableSheet}注释中的表格示例。
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:14
 * @github - https://github.com/hl845740757
 */
public class ColNameRow {

    /**
     * 列名所在的行索引
     */
    private final int rowIndex;
    /**
     * 列名到列索引的映射
     */
    private final Object2IntMap<String> colName2Index;

    /**
     * create instance
     * @param rowIndex 所在的行索引
     * @param colName2Index LinkedHashMap保持读入序
     */
    public ColNameRow(int rowIndex, Object2IntMap<String> colName2Index) {
        this.rowIndex = rowIndex;
        this.colName2Index = Object2IntMaps.unmodifiable(colName2Index);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex(String colName){
        return colName2Index.getInt(colName);
    }

    /**
     * 获取有效列名集合
     * @return
     */
    public Set<String> getColeNameSet(){
        return colName2Index.keySet();
    }

    public Object2IntMap<String> getColName2Index() {
        return colName2Index;
    }

    @Override
    public String toString() {
        return "ColNameRow{" +
                "rowIndex=" + rowIndex +
                ", colName2Index=" + colName2Index +
                '}';
    }
}
