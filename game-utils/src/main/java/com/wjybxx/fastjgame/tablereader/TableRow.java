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
