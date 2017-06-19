package com.testlinenergo.service;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.dao.ReportDao;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private ReportDao reportDao;

    private static final Long NUMBER_OF_MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000L;

    private static final Integer NUMBER_OF_DAYS_IN_MONTH = 31;

    @Override
    public boolean createReport(final String filename, NeedOfColumns columns, long index) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("FirstSheet");

        List<String> validColumn = new LinkedList<>();
        if (null == columns.getTimestampNeed()) columns.setTimestampNeed(false);
        else if (columns.getTimestampNeed()) validColumn.add(READ_TIME);

        if (null == columns.getTemperatureNeed()) columns.setTemperatureNeed(false);
        else if (columns.getTemperatureNeed()) validColumn.add(TEMPERATURE);

        if (null == columns.getPressureNeed()) columns.setPressureNeed(false);
        else if (columns.getPressureNeed()) validColumn.add(PRESSURE);

        if (null == columns.getWindDirectionNeed()) columns.setWindDirectionNeed(false);
        else if (columns.getWindDirectionNeed()) validColumn.add(WIND_DIR);

        if (null == columns.getWindSpeedNeed()) columns.setWindSpeedNeed(false);
        else if (columns.getWindSpeedNeed()) validColumn.add(WIND_SPEED);

        final int[] headColumnCounter = new int[]{0};
        HSSFRow rowhead = sheet.createRow((short) 0);
        validColumn.forEach(col ->
                rowhead.createCell(headColumnCounter[0]++).setCellValue(col));

        if (headColumnCounter[0] == 0)     //если не пользователь не отметил ни одной колонки, то добавляем одну колонку
            rowhead.createCell(0).setCellValue(READ_TIME);

        List<MeteoStationData> dataList = getMonthlyMeteoDataList();
        for (int i = 0; i < dataList.size(); ++i) {
            HSSFRow row = sheet.createRow((short) i + 1);
            int rowColumnCounter = 0;
            if (columns.getTimestampNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getReadTimestamp().toString());
            if (columns.getTemperatureNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getTemperature());
            if (columns.getPressureNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getPressure());
            if (columns.getWindDirectionNeed())
                row.createCell(rowColumnCounter++).setCellValue(dataList.get(i).getWindDirection());
            if (columns.getWindSpeedNeed())
                row.createCell(rowColumnCounter).setCellValue(dataList.get(i).getWindSpeed());
        }

        if (index != FULL_REPORT_INDEX) {
            HSSFRow lastRowForPrintReportIndex = sheet.createRow((short) dataList.size());
            lastRowForPrintReportIndex.createCell(0).setCellValue("Номер отчета " + index);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
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

    @Override
    public void saveReport(Report report) {
        this.reportDao.save(report);
    }

    @Override
    public List<String> getHeadRows(NeedOfColumns columns) {
        List<String> headRows = new LinkedList<>();
        if (columns.getTimestampNeed()) headRows.add(READ_TIME);
        if (columns.getTemperatureNeed()) headRows.add(TEMPERATURE);
        if (columns.getPressureNeed()) headRows.add(PRESSURE);
        if (columns.getWindDirectionNeed()) headRows.add(WIND_DIR);
        if (columns.getWindSpeedNeed()) headRows.add(WIND_SPEED);

        if (headRows.isEmpty()) headRows.add(READ_TIME);

        return headRows;
    }

    @Override
    public void addFileInResponse(File reportFile, HttpServletResponse response) {
        try (OutputStream out = response.getOutputStream();
             FileInputStream in = new FileInputStream(reportFile)) {
            int length;
            byte[] buffer = new byte[4096];
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            logger.debug(String.format("Error, \"%s\".", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public String saveEditingReportAndGetFileNAme(NeedOfColumns columns) {
        Report report = new Report();
        saveReport(report);

        final String currentFileName = FILE_NAME
                + "_number" + report.getReportId() + FILE_TYPE;
        createReport(FILE_PATH + currentFileName, columns, report.getReportId());

        return currentFileName;
    }

    @Override
    public NeedOfColumns parseStringWithLogicalColumnValues(String needfulColumns) {
        NeedOfColumns columns = new NeedOfColumns();
        if (null == needfulColumns || needfulColumns.isEmpty()) {
            columns.setTimestampNeed(true);
            columns.setTemperatureNeed(true);
            columns.setPressureNeed(true);
            columns.setWindDirectionNeed(true);
            columns.setWindSpeedNeed(true);
        } else {
            String[] stringColumnsValues = needfulColumns.split("_");
            columns.setTimestampNeed(
                    Boolean.valueOf(stringColumnsValues[0]));
            columns.setTemperatureNeed(
                    Boolean.valueOf(stringColumnsValues[1]));
            columns.setPressureNeed(
                    Boolean.valueOf(stringColumnsValues[2]));
            columns.setWindDirectionNeed(
                    Boolean.valueOf(stringColumnsValues[3]));
            columns.setWindSpeedNeed(
                    Boolean.valueOf(stringColumnsValues[4]));
        }

        return columns;
    }

    @Override
    public void makeModelForStartPage(Model model) {
        List<String> headRows = new LinkedList<>();
        headRows.add(READ_TIME);
        headRows.add(TEMPERATURE);
        headRows.add(PRESSURE);
        headRows.add(WIND_DIR);
        headRows.add(WIND_SPEED);

        List<MeteoStationData> allData = getMonthlyMeteoDataList();
        allData.sort(Comparator.comparing(MeteoStationData::getReadTimestamp));

        model.addAttribute("headRows", headRows);
        model.addAttribute("allData", allData);
        model.addAttribute("report", new Report());
    }
}
