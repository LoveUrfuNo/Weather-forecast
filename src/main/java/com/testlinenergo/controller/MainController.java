package com.testlinenergo.controller;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.dao.ReportDao;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;
import com.testlinenergo.service.ReportService;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    private static final String FILE_PATH
            = "C:\\Users\\kosty\\Desktop\\testlinenergo\\src\\main\\resources\\static\\";

    private static final String FILE_NAME = "monthly-meteo-report";

    private static final String FILE_TYPE = ".xls";

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

    @RequestMapping(value = "/make-report", method = RequestMethod.POST)
    public void makeMonthlyReport(@ModelAttribute NeedOfColumns columns) {
        this.reportService.createReport(
                FILE_PATH + FILE_NAME + FILE_TYPE, columns, FULL_REPORT_INDEX);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showReport(Model model) {
        List<String> headRows = new LinkedList<>();
        headRows.add(READ_TIME);
        headRows.add(TEMPERATURE);
        headRows.add(PRESSURE);
        headRows.add(WIND_DIR);
        headRows.add(WIND_SPEED);

        List<MeteoStationData> allData = this.reportService.getMonthlyMeteoDataList();
        allData.sort(Comparator.comparing(MeteoStationData::getReadTimestamp));

        model.addAttribute("headRows", headRows);
        model.addAttribute("allData", allData);

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
//        allData.sort(Comparator.comparing(MeteoStationData::getReadTimestamp));

        model.addAttribute("headRows", this.reportService.getHeadRows(columns));
        model.addAttribute("allData", allData);
        model.addAttribute("needfulColumns", columns);

        return "report";
    }

    @RequestMapping(value = "/download-full-report", method = RequestMethod.GET)
    public void downloadFull(HttpServletResponse response) {
        File myFile = new File(FILE_PATH, FILE_NAME + FILE_TYPE);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", FILE_NAME + FILE_TYPE));

        this.reportService.addFileInResponse(myFile, response);
    }

    @RequestMapping(value = "/download-editing-report/{needfulColumns}", method = RequestMethod.GET)
    public void downloadEditing(@PathVariable String needfulColumns,
                                  HttpServletResponse response, Model model) {
        Report report = new Report();
        this.reportService.saveReport(report);

        String[] stringColumnsValues = needfulColumns.split("_");
        NeedOfColumns columns = new NeedOfColumns();
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

        final String currentFileName = FILE_NAME +
                "_number" + report.getReportId() + FILE_TYPE;
        this.reportService.createReport(FILE_PATH + currentFileName, columns, report.getReportId());

        File myFile = new File(FILE_PATH, currentFileName);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", currentFileName));

        this.reportService.addFileInResponse(myFile, response);
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