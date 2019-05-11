package com.wjybxx.fastjgame.utils;

import com.wjybxx.fastjgame.utils.tablereader.CSVReader;
import com.wjybxx.fastjgame.utils.tablereader.ExcelReader;
import com.wjybxx.fastjgame.utils.tablereader.TableSheet;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Excel和Csv表格的辅助工具
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:18
 * @github - https://github.com/hl845740757
 */
public final class ExcelCsvUtils {

    private ExcelCsvUtils() {
    }

    // ---------------------------------- CSV ----------------------------

    /**
     * 读取CSV表格。
     * 默认编码格式 GBK
     * 默认属性名所在行索引 0 (也就是第一行)
     *
     * @param file csv文件
     */
    public static TableSheet readCsv(File file) throws Exception {
        return new CSVReader().readCfg(file,0,0);
    }

    /**
     * 读取CSV表格。
     * @param file csv文件
     * @param nameRowIndex 属性名所在行
     */
    public static TableSheet readCsv(File file,int nameRowIndex) throws Exception {
        return new CSVReader().readCfg(file,0,nameRowIndex);
    }

    /**
     * CSV 不支持分页，但是可以指定第几行为属性名
     * @param file csv文件
     * @param charset 指定csv文件编码格式
     * @param nameRowIndex 属性名所在行索引
     * @return
     */
    public static TableSheet readCsv(File file, Charset charset, int nameRowIndex) throws Exception {
        return new CSVReader(charset).readCfg(file,0,nameRowIndex);
    }

    // ---------------------------------- EXCEL ----------------------------

    /**
     * 读完excel的第一页，且第一行为属性名所在行
     * @param file excel文件 (.xlsx)
     */
    public static TableSheet readExcel(File file) throws Exception{
        return readExcel(file,0,0);
    }

    /**
     * Excel支持分页，可以指定 读取第几页 和 第几行为属性名
     * @param file excel文件 (.xlsx)
     * @param sheetIndex excel文件页索引
     * @param nameRowIndex 属性名所在行的索引
     */
    public static TableSheet readExcel(File file, int sheetIndex, int nameRowIndex) throws Exception{
        return new ExcelReader().readCfg(file,sheetIndex,nameRowIndex);
    }

}
