package com.testlinenergo.controller;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.dao.ReportDao;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;
import com.testlinenergo.service.ReportService;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * заппппооооооолнить!!!!!!!!!!!!
 */
@Controller
public class MainController {
    @Autowired
    private ReportService reportService;

    @Autowired
    private MeteoDataDao meteoDataDao;

    public static final String FILE_PATH
            = "C:\\Users\\kosty\\Desktop\\testlinenergo\\src\\main\\resources\\static\\";

    public static final String FILE_NAME = "monthly-meteo-report";

    public static final String FILE_TYPE = ".xls";

    private static final String CONTENT_TYPE = "application/vnd.ms-excel";

    private static final String HEADER_NAME = "Content-disposition";

    public static final String READ_TIME = "readTimestamp";

    public static final String TEMPERATURE = "temperature";

    public static final String PRESSURE = "pressure";

    public static final String WIND_DIR = "windDirection";

    public static final String WIND_SPEED = "windSpeed";

    private static final String FULL_REPORT = "full";

    public static final Long FULL_REPORT_INDEX = -1L;

    public static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @RequestMapping(value = "/add-meteo-data", method = RequestMethod.GET)
    public void addMeteoData(JSONObject data) {
        DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

        MeteoStationData newData = new MeteoStationData();
        try {
            newData.setReadTimestamp(formatter.parse(data.getString(READ_TIME)));
            newData.setTemperature((Double) data.get(TEMPERATURE));
            newData.setPressure((Integer) data.get(PRESSURE));
            newData.setWindDirection((Integer) data.get(WIND_DIR));
            newData.setWindSpeed((Integer) data.get(WIND_SPEED));
        } catch (ParseException e) {
            logger.debug(String.format("Parse read_temperature error! %s", e.getMessage()));
            e.printStackTrace();
        }

        this.meteoDataDao.save(newData);
    }

    @RequestMapping(value = "/update-reports", method = RequestMethod.GET)
    public void updateReport() {
        File allReportsDir = new File(FILE_PATH);
        if (allReportsDir.listFiles() != null) {
            for (File file : allReportsDir.listFiles()) {
                HSSFWorkbook workBook;
                HSSFSheet firstSheet = null;
                HSSFRow firstRow = null;
                try {
                    workBook = new HSSFWorkbook(new POIFSFileSystem(file));
                    firstSheet = workBook.getSheetAt(0);
                    if (null == firstSheet || firstSheet.getLastRowNum() == 0)
                        throw new NullPointerException();

                    firstRow = firstSheet.getRow(0);
                    if (null == firstRow || firstRow.getPhysicalNumberOfCells() == 0)
                        throw new NullPointerException();
                } catch (IOException e) {
                    logger.debug(String.format("Update reports error! %s", e.getMessage()));
                    e.printStackTrace();
                }

                NeedOfColumns columns = new NeedOfColumns();
                for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); ++i) {    //columns number in the current file
                    if (firstRow.getCell(i).toString().equals(READ_TIME))
                        columns.setTimestampNeed(true);
                }
                for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); ++i) {
                    if (firstRow.getCell(i).toString().equals(TEMPERATURE))
                        columns.setTemperatureNeed(true);
                }
                for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); ++i) {
                    if (firstRow.getCell(i).toString().equals(PRESSURE))
                        columns.setPressureNeed(true);
                }
                for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); ++i) {
                    if (firstRow.getCell(i).toString().equals(WIND_DIR))
                        columns.setWindDirectionNeed(true);
                }
                for (int i = 0; i < firstRow.getPhysicalNumberOfCells(); ++i) {
                    if (firstRow.getCell(i).toString().equals(WIND_SPEED))
                        columns.setWindSpeedNeed(true);
                }

                String lastRowFromFile = firstSheet.getRow(firstSheet.getLastRowNum()).getCell(0).toString();
                long reportIndex = lastRowFromFile.contains("Номер отчета")
                        ? Long.valueOf(lastRowFromFile.replaceAll("\\D", ""))
                        : FULL_REPORT_INDEX;
                this.reportService.createReport(FILE_PATH + file.getName(),
                        columns, reportIndex);
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showReportGet(Model model) {
        this.reportService.makeModelForStartPage(model);

        return "report";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String showReportPost(Model model) {
        this.reportService.makeModelForStartPage(model);

        return "report";
    }

    @RequestMapping(value = "/editing", method = RequestMethod.GET)
    public String editReport(Model model) {
        model.addAttribute("checkObject", new NeedOfColumns());

        return "editing";
    }

    @RequestMapping(value = "/show-editing-report", method = RequestMethod.POST)
    public String showEditReport(@ModelAttribute NeedOfColumns columns, Model model) {
        List<MeteoStationData> allData = this.reportService.getMonthlyMeteoDataList();

        model.addAttribute("headRows", this.reportService.getHeadRows(columns));
        model.addAttribute("allData", allData);
        model.addAttribute("needfulColumns", columns);

        return "report";
    }

    @RequestMapping(value = "/download-full-report", method = RequestMethod.GET)
    public void downloadFull(HttpServletResponse response) {
        File reportFile = new File(FILE_PATH, FILE_NAME + FILE_TYPE);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", FILE_NAME + FILE_TYPE));

        this.reportService.addFileInResponse(reportFile, response);
    }

    @RequestMapping(value = "/download-editing-report/{needfulColumns}", method = RequestMethod.GET)
    public void downloadEditing(@PathVariable String needfulColumns,
                                HttpServletResponse response, Model model) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        final String currentFileName
                = this.reportService.saveEditingReportAndGetFileNAme(columns);
        File reportFile = new File(FILE_PATH, currentFileName);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", currentFileName));

        this.reportService.addFileInResponse(reportFile, response);
    }

    @RequestMapping(value = "/download-report-by-index", method = RequestMethod.POST)
    public void downloadReportByIndex(@ModelAttribute(name = "report") Report report,
                                      HttpServletResponse response, HttpServletRequest request) {
        File reportFile = new File(FILE_PATH,
                FILE_NAME + "_number" + report.getReportId() + FILE_TYPE);
        if (!reportFile.isFile()) {
            try {
                request.getRequestDispatcher("/").forward(request, response);
            } catch (ServletException | IOException e) {
                logger.debug(String.format(
                        "Error, can't redirect from \"/download-report-by-index\" to \"/\", %s",
                        e.getMessage()));
                e.printStackTrace();
            }

            return;
        }

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", reportFile.getName()));

        this.reportService.addFileInResponse(reportFile, response);
    }

    @RequestMapping(value = "/save-editing-report/{needfulColumns}", method = RequestMethod.GET)
    public String saveReport(@PathVariable String needfulColumns, Model model) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        String reportIdStr = this.reportService.saveEditingReportAndGetFileNAme(columns)
                .replaceAll("[\\D]", "");

        model.addAttribute("reportNumber", reportIdStr);

        return "after-save";
    }

    /**
     * Возвращает json массив с данными либо полного, либо редактированного отчета.
     *
     * @param columns - "all" или например "readTimestamp_temperature_windSpeed"
     * @return JsonArray with data from report
     */
    @ResponseBody
    @RequestMapping(value = "/get-json/{columns}", method = RequestMethod.GET)
    public JSONArray getJson(@PathVariable(name = "columns") String columns) {
        if (columns.equals(FULL_REPORT)) {
            return new JSONArray(this.reportService.getMonthlyMeteoDataList());
        } else {
            List<MeteoStationData> listWithoutExcessFields
                    = this.reportService.getMonthlyMeteoDataList();
            listWithoutExcessFields.forEach(data -> {
                if (Arrays.stream(columns.split("_")).noneMatch(READ_TIME::equals))
                    data.setReadTimestamp(null);
                if (Arrays.stream(columns.split("_")).noneMatch(TEMPERATURE::equals))
                    data.setTemperature(null);
                if (Arrays.stream(columns.split("_")).noneMatch(PRESSURE::equals))
                    data.setPressure(null);
                if (Arrays.stream(columns.split("_")).noneMatch(WIND_DIR::equals))
                    data.setWindDirection(null);
                if (Arrays.stream(columns.split("_")).noneMatch(WIND_SPEED::equals))
                    data.setWindSpeed(null);
            });

            return new JSONArray(listWithoutExcessFields);
        }
    }
}