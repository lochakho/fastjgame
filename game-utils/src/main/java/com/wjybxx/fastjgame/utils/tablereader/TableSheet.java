package com.wjybxx.fastjgame.utils.tablereader;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Excel或CSV等配置表的一页
 *
 * like:
 *                ---------------------------------
 * ColNameRow     | name | age |  sex | profession|
 *                |--------------------------------
 * ColNameRow+1   | zhang| 25  |  man | coder     |
 *                ---------------------------------
 *
 * 没有实现迭代器是因为，你一般需要跳过属性名及前面的行，建议使用{@link #getContentRows()}进行遍历
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:09
 * @github - https://github.com/hl845740757
 */
public class TableSheet {

    /**
     * 文件名字
     */
    private final String fileName;
    /**
     * 文件的第几页，默认应该为0
     * 索引0开始
     */
    private final int sheetIndex;
    /**
     * 属性名所在的行
     */
    private final ColNameRow colNameRow;
    /**
     * 所有内容行
     */
    private final List<TableRow> tableRows;

    /**
     * new instance
     * @param fileName 文件名
     * @param sheetIndex 第几页
     * @param colNameRow 属性名称行
     * @param tableRows LinkedHashMap要求保持读入序
     */
    public TableSheet(String fileName, int sheetIndex, ColNameRow colNameRow, List<TableRow> tableRows) {
        this.fileName = fileName;
        this.sheetIndex = sheetIndex;
        this.colNameRow = colNameRow;
        this.tableRows = Collections.unmodifiableList(tableRows);
    }

    public String getFileName() {
        return fileName;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public ColNameRow getColNameRow() {
        return colNameRow;
    }

    /**
     * 获取有效内容行，方便迭代(遍历)。
     * 强烈建议使用它进行遍历。
     * {@coe
     *      for(TableRow row:getContentRows()){
     *          int age=row.getAsInt("age");
     *      }
     * }
     * @return 跳过了属性名称行及前面的行
     */
    public List<TableRow> getContentRows(){
        int contentStartRowIndex = colNameRow.getRowIndex() + 1;
        if (contentStartRowIndex < tableRows.size()){
            return tableRows.subList(contentStartRowIndex,tableRows.size());
        }else {
            return Collections.emptyList();
        }
    }

    /**
     * 通过行索引获取该行配置。
     * 强烈建议使用{@link #getContentRows()}遍历。
     * @param rowIndex [0,getTotalRowNum()-1]
     * @return
     */
    @Nonnull
    public TableRow getRow(int rowIndex){
        if (rowIndex <0 || rowIndex >=tableRows.size()){
            throw new IllegalArgumentException("rowIndex range: [0,totalRowNum-1]");
        }
        return tableRows.get(rowIndex);
    }

    /**
     * 获取表格的所有行内容。
     * 强烈建议使用{@link #getContentRows()}遍历。
     * @return 所有内容行
     */
    public List<TableRow> getAllRows(){
        return tableRows;
    }

    /**
     * 获取表总行数
     */
    public int getTotalRowNum(){
        return tableRows.size();
    }

    /**
     * 获取所有列名(属性名)
     */
    public Set<String> getColNameSet(){
        return colNameRow.getColeNameSet();
    }

    @Override
    public String toString() {
        return "TableSheet{" +
                "fileName='" + fileName + '\'' +
                ", sheetIndex=" + sheetIndex +
                ", colNameRow=" + colNameRow +
                ", totalRowNum=" + tableRows.size() +
                '}';
    }

}
