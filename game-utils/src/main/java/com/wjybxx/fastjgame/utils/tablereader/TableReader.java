package com.wjybxx.fastjgame.utils.tablereader;

import com.wjybxx.fastjgame.utils.ExcelCsvUtils;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Excel和Csv读表超类，模板实现。
 * 推荐使用{@link ExcelCsvUtils}中的静态方法读取表格。
 *
 * @apiNote
 * 如果你自己有自己的实现，请注意在{@link #close()}中关闭流等资源。
 *
 * @param <T> the type of row
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:16
 * @github - https://github.com/hl845740757
 */
public abstract class TableReader<T> implements AutoCloseable{

    /**
     * 读取表格的模板方法
     * @param file 要读取的表格文件
     * @param sheetIndex 表格页索引(0开始)
     * @param nameRowIndex 属性名索引(0开始)
     * @return {@link TableSheet}
     * @throws Exception
     */
    public final TableSheet readCfg(File file, int sheetIndex, int nameRowIndex) throws Exception {
        String fileName = file.getName();
        Iterator<T> rowItr = toIterator(file,sheetIndex);
        try {
            List<TableRow> tableRows=new ArrayList<>();
            ColNameRow colNameRow =null;

            // 缓存前面的行
            List<T> cacheRows=new ArrayList<>(nameRowIndex);

            for (int rowIndex = 0; rowItr.hasNext(); rowIndex++){
                T row=rowItr.next();
                // 前面的行
                if (rowIndex<nameRowIndex){
                    cacheRows.add(row);
                    continue;
                }
                if (rowIndex==nameRowIndex){
                    colNameRow = readColNameRow(fileName,rowIndex,row);
                    // 把前面行也读取进来
                    for (int cacheRowIndex=0;cacheRowIndex<rowIndex;cacheRowIndex++){
                        TableRow tableRow = readContentRow(colNameRow, cacheRowIndex, cacheRows.get(cacheRowIndex));
                        tableRows.add(tableRow);
                    }
                }
                assert null!= colNameRow;
                TableRow tableRow = readContentRow(colNameRow, rowIndex, row);
                tableRows.add(tableRow);
            }
            if (null== colNameRow){
                throw new IllegalArgumentException("file " + fileName + " missing colNameRow");
            }
            return new TableSheet(fileName, sheetIndex, colNameRow,tableRows);
        }finally {
            close();
        }
    }

    /**
     * 将文件转换为可迭代的行
     */
    protected abstract Iterator<T> toIterator(File file, int sheetIndex) throws IOException;

    /**
     * 获取指定行有多少列
     */
    protected abstract int getTotalColNum(T row);

    /**
     * 获取指定行指定列索引的元素
     * @param row 原始行类型
     * @param colIndex [0,getTotalColNum(row))
     * @return nullable
     */
    protected abstract String getNullableCell(T row, int colIndex);

    /**
     * 读取属性名行
     * @param fileName 文件名，用于打印更详细的错误原因
     * @param rowIndex 行索引
     * @param row 行内容
     * @return
     */
    private ColNameRow readColNameRow(String fileName, int rowIndex, T row){
        // 使用LinkedHashMap以保持读入顺序
        int totalColNum = getTotalColNum(row);
        Object2IntMap<String> colName2Index=new Object2IntLinkedOpenHashMap<>(totalColNum+1,1);
        for (int colIndex = 0; colIndex<totalColNum; colIndex++){
            String originalColName=getNullableCell(row,colIndex);
            // 属性名称行，空白属性跳过
            if (null == originalColName){
                continue;
            }
            // 去掉空白填充
            String realColName = originalColName.trim();
            if (realColName.length()==0){
                continue;
            }
            // 属性名不可以有重复
            if (colName2Index.containsKey(realColName)){
                throw new IllegalArgumentException("file " + fileName
                        + " propertyNameRow has duplicate column " + realColName);
            }
            colName2Index.put(realColName,colIndex);
        }
        return new ColNameRow(rowIndex,colName2Index);
    }

    /**
     * 读取内容行
     * @param colNameRow 属性列信息
     * @param rowIndex 行索引
     * @param row 行内容
     * @return
     */
    private TableRow readContentRow(ColNameRow colNameRow, int rowIndex, T row){
        // 使用LinkedHashMap以保持读入顺序
        Map<String,String> colName2Value=new LinkedHashMap<>();
        // 读取所有属性
        for (Object2IntMap.Entry<String> entry:colNameRow.getColName2Index().object2IntEntrySet()){
            String colName=entry.getKey();
            int colIndex=entry.getIntValue();
            String value=getNullableCell(row,colIndex);
            colName2Value.put(colName,null==value?"":value);
        }
        return new TableRow(rowIndex,colName2Value);
    }
}
