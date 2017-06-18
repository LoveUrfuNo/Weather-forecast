package com.testlinenergo.service;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.testlinenergo.controller.MainController.*;

/**
 * Created by kosty on 17.06.2017.
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private MeteoDataDao meteoDataDao;

    private static final Long NUMBER_OF_MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000L;

    private static final Integer NUMBER_OF_DAYS_IN_MONTH = 31;

    @Override
    public boolean createReport(final String fileName, NeedOfColumns columns) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("FirstSheet");

        int headColumnCounter = 0;
        HSSFRow rowhead = sheet.createRow((short) 0);
        if (columns.getTimestampNeed())
            rowhead.createCell(headColumnCounter++).setCellValue(READ_TIME);

        if (columns.getTemperatureNeed())
            rowhead.createCell(headColumnCounter++).setCellValue(TEMPERATURE);

        if (columns.getPressureNeed())
            rowhead.createCell(headColumnCounter++).setCellValue(PRESSURE);

        if (columns.getWindDirectionNeed())
            rowhead.createCell(headColumnCounter++).setCellValue(WIND_DIR);

        if (columns.getWindSpeedNeed())
            rowhead.createCell(headColumnCounter++).setCellValue(WIND_SPEED);

        if (headColumnCounter == 0)
            rowhead.createCell(headColumnCounter).setCellValue(READ_TIME);;

        List<MeteoStationData> dataList = getMonthlyMeteoDataList();
        for (int i = 0; i < dataList.size(); ++i) {
            HSSFRow row = sheet.createRow((short) i + 1);
            int rowColumnCounter = 0;

            if (columns.getTimestampNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getReadTimestamp());
            if (columns.getTemperatureNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getTemperature());
            if (columns.getPressureNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getPressure());
            if (columns.getWindDirectionNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getWindDirection());
            if (columns.getWindSpeedNeed())
                row.createCell(rowColumnCounter).setCellValue(dataList.get(i).getWindSpeed());
        }

        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(String.format("Error while creating report, \"%s\".", e.getMessage()));

            return false;
        }

        return true;
    }

    @Override
    public List<MeteoStationData> getMonthlyMeteoDataList() {
        return this.meteoDataDao.findAll().stream()
                .filter(data ->
                        (System.currentTimeMillis() - data.getReadTimestamp().getTime())
                                / NUMBER_OF_MILLISECONDS_IN_DAY <= NUMBER_OF_DAYS_IN_MONTH)
                .collect(Collectors.toList());
    }
}
