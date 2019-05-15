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

import com.wjybxx.fastjgame.configwrapper.ConfigHelper;

import java.util.Map;

/**
 * Excel或CSV表格中的一行(内容行)
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:11
 * @github - https://github.com/hl845740757
 */
public class TableRow extends ConfigHelper {

    /**
     * 所属的行号，0开始
     */
    private final int rowIndex;
    /**
     * 本行内容
     * colName -> content 可读性更好
     * colIndex -> content 的可读性有点差
     */
    private final Map<String,String> colName2Value;

    /**
     * create instance
     * @param rowIndex 行索引
     * @param colName2Value 属性名到属性值的映射
     */
    public TableRow(int rowIndex, Map<String,String> colName2Value) {
        this.rowIndex = rowIndex;
        this.colName2Value = colName2Value;
    }

    @Override
    public String getAsString(String key) {
        return colName2Value.get(key);
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public String toString() {
        return "TableRow{" +
                "rowIndex=" + rowIndex +
                ", colName2Value=" + colName2Value +
                '}';
    }
}
