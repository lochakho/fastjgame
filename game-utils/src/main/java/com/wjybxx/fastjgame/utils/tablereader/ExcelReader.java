package com.wjybxx.fastjgame.utils.tablereader;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Excel文件的Reader
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/11 16:18
 * @github - https://github.com/hl845740757
 */
public class ExcelReader extends TableReader<Row> {

    private static final Logger logger= LoggerFactory.getLogger(ExcelReader.class);
    /**
     * 读取表格时产生的缓存
     */
    private Workbook workbook=null;

    @Override
    protected Iterator<Row> toIterator(File file, int sheetIndex) throws IOException {
        // 缓存100行
        // 看源码发现open时使用file更好
        workbook = StreamingReader.builder()
                .rowCacheSize(200)
                .bufferSize(1024*1024)
                .open(file);
        return workbook.getSheetAt(sheetIndex).rowIterator();
    }

    @Override
    protected int getTotalColNum(Row row) {
        return row.getLastCellNum();
    }

    @Override
    protected String getNullableCell(Row row, int colIndex) {
        Cell cell=row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.getStringCellValue();
    }

    @Override
    public void close() throws Exception {
        if (null!=workbook){
            try {
                workbook.close();
            }catch (Exception e){
                logger.info("workbook.close",e);
            }
        }
    }
}
