package com.testlinenergo.service;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kosty on 17.06.2017.
 */
@Service
public class ExcelServiceImpl implements ExcelService {
    @Override
    public boolean createFile() {
        final String filename = "C:\\Users\\kosty\\Desktop\\testlinenergo\\src\\main\\resources\\static\\NewExcelFile.xls";
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("FirstSheet");

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue("No.");
        rowhead.createCell(1).setCellValue("Name");
        rowhead.createCell(2).setCellValue("Address");
        rowhead.createCell(3).setCellValue("Email");

        HSSFRow row = sheet.createRow((short) 1);
        row.createCell(0).setCellValue("1");
        row.createCell(1).setCellValue("Sankumarsingh");
        row.createCell(2).setCellValue("India");
        row.createCell(3).setCellValue("sankumarsingh@gmail.com");

        try(FileOutputStream fileOut = new FileOutputStream(filename)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}
