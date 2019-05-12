package com.wjybxx.fastjgame.utils.tablereader;

import com.wjybxx.fastjgame.utils.configwrapper.TableSheetConfigWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Excel或CSV等配置表的一页，默认colNameRow之后的为有效内容行，前面的行都是注释行。
 *
 * like:
 *                |-------------------------------|
 * AnnotationRow  | 名字 | 年龄 | 性别  |  职业      |
 *                ---------------------------------
 * ColNameRow     | name | age |  sex | profession|
 *                |--------------------------------
 * ColNameRow+1   | zhang| 25  |  man | coder     |
 *                ---------------------------------
 *
 * 建议使用{@link #getContentRows()}进行遍历。
 * {@code
 *      for(TableRow row:getContentRows()){
 *          String name=row.getAsString("name");
 *          int age=row.getAsInt("age");
 *      }
 * }
 *
 * 如果你的表格只有两列有效列，且是属于key-value性质的(key不可以重复)，like:
 *                |---------------------------|
 * AnnotationRow  |   参数列    |  值列         |
 *                |---------------------------|
 * ColNameRow     |   param    |   value      |
 *                |----------------------------
 * ColNameRow+1   |   a        |       1      |
 *                -----------------------------
 * ColNameRow+2   |   b        |       2      |
 *                -----------------------------
 * 你可以使用{@link #creatMap(String, String)}创建这两列的包装对象，以使用key获取value，like:
 * {@code
 *       ConfigWrapper wrapper=creatMap(param,value);
 *       // wrapper.getAsInt("a") 将会返回 1
 *       // wrapper.getAsInt("b") 将会返回 2
 * }
 *
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
     * 为指定两列属性建立映射，如果你的表格只有两列是有效的，且是key-value性质的，推荐使用该方法。
     * @param keyColName key所在列名，key所在列不可以重复。
     * @param valueColName value所在列名
     * @return
     */
    public TableSheetConfigWrapper creatMap(String keyColName, String valueColName){
        return TableSheetConfigWrapper.create(this,keyColName,valueColName);
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
     * 在内容行查询某列为某值的行
     * @param colName 列名
     * @param colValue 该列对应的值
     * @return
     */
    @Nullable
    public TableRow getRow(String colName,String colValue){
        for (TableRow tableRow:getContentRows()){
            if (colValue.equals(tableRow.getAsString(colName))){
                return tableRow;
            }
        }
        return null;
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
