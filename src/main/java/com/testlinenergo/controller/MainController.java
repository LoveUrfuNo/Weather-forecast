package com.testlinenergo.controller;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.dao.ReportDao;
import com.testlinenergo.model.EditingOptions;
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

    private static final Integer NUMBER_OF_TIMESTAMP_COLUMN = 0;

    private static final Integer NUMBER_OF_TEMPERATURE_COLUMN = 1;

    private static final Integer NUMBER_OF_PRESSURE_COLUMN = 2;

    private static final Integer NUMBER_OF_WIND_DIR_COLUMN = 3;

    private static final Integer NUMBER_OF_WIND_SPEED_COLUMN = 4;


    public static final Logger logger = LoggerFactory.getLogger(MainController.class);

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

    @RequestMapping(value = "/add-meteo-data", method = RequestMethod.GET)
    public void addMeteoData(JSONObject data) {
        DateFormat formatter
                = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);

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

    @RequestMapping(value = "/update-reports/{needfulColumns}/{options}", method = RequestMethod.GET)
    public void updateReport(@PathVariable(name = "needfulColumns") String needfulColumns,
                             @PathVariable(name = "options") String optionsString) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        EditingOptions options
                = this.reportService.parseStringWithOptions(optionsString, columns);

        List<MeteoStationData> allData
                = this.reportService.getMonthlyMeteoDataList();
        try {
            this.reportService.editAllDataWithUserChanges(allData, columns, options);
            this.reportService.updateAllReports(allData);
        } catch (IOException | ParseException e) {
            logger.debug(String.format("Update reports error! %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showReportGet(Model model) {
        this.reportService.makeModelForStartPage(model);

        NeedOfColumns columns = this.reportService.getColumnForFullReport();
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

        return "report";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String showReportPost(Model model) {
        this.reportService.makeModelForStartPage(model);

        NeedOfColumns columns = this.reportService.getColumnForFullReport();
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

        return "report";
    }

    @RequestMapping(value = "/choice-columns", method = RequestMethod.GET)
    public String choiceColumns(Model model) {
        model.addAttribute("needfulColumns", new NeedOfColumns());

        return "choice-columns";
    }

    @RequestMapping(value = "/show-partial-report", method = RequestMethod.POST)
    public String showEditReport(@ModelAttribute(name = "needfulColumns") NeedOfColumns columns,
                                 Model model) {
        model.addAttribute("headRows", this.reportService.getHeadRows(columns));
        model.addAttribute("allData", this.reportService.getMonthlyMeteoDataList());
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

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

    @RequestMapping(value = "/download-editing-report/{needfulColumns}/{options}",
            method = RequestMethod.GET)
    public void downloadEditing(@PathVariable(name = "needfulColumns") String needfulColumns,
                                @PathVariable(name = "options") String optionsString,
                                HttpServletResponse response) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        EditingOptions options = null;
        if (Arrays.stream(optionsString.split("_")).noneMatch(str -> str.equals("null")))
            options = this.reportService.parseStringWithOptions(optionsString, columns);

        List<MeteoStationData> allData
                = this.reportService.getMonthlyMeteoDataList();
        try {
            this.reportService.editAllDataWithUserChanges(allData, columns, options);
        } catch (ParseException e) {
            logger.debug(String.format(
                    "Error, can't parse user string to date from \"/download-editing-report\", %s",
                    e.getMessage()));
            e.printStackTrace();
        }

        final String currentFileName
                = this.reportService.saveEditingReportAndGetFileNAme(allData, columns);
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

    @RequestMapping(value = "/save-partial-report/{needfulColumns}/{options}", method = RequestMethod.GET)
    public String saveReport(@PathVariable(name = "needfulColumns") String needfulColumns,
                             @PathVariable(name = "options") String optionsString,
                             Model model) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        EditingOptions options = null;
        if (!optionsString.split("_").equals("null"))
            options = this.reportService.parseStringWithOptions(optionsString, columns);

        List<MeteoStationData> allData
                = this.reportService.getMonthlyMeteoDataList();
        try {
            this.reportService.editAllDataWithUserChanges(allData, columns, options);
        } catch (ParseException e) {
            logger.debug(String.format(
                    "Error, can't parse user string to date from \"/save-partial-report\", %s",
                    e.getMessage()));
            e.printStackTrace();
        }

        String reportId = this.reportService.saveEditingReportAndGetFileNAme(allData, columns)
                .replaceAll("[\\D]", "");

        model.addAttribute("reportNumber", reportId);

        return "after-save";
    }

    @RequestMapping(value = "/editing-report/{needfulColumns}", method = RequestMethod.GET)
    public String editReport(@PathVariable String needfulColumns, Model model) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);

        EditingOptions options = new EditingOptions();
        options.setColumns(columns);
        model.addAttribute("options", options);

        return "editing";
    }

    @RequestMapping(value = "/editing-report/{needfulColumns}", method = RequestMethod.POST)
    public String showEditingReport(@PathVariable String needfulColumns,
                                    @ModelAttribute EditingOptions options, Model model) {
        NeedOfColumns columns
                = this.reportService.parseStringWithLogicalColumnValues(needfulColumns);
        List<MeteoStationData> allData
                = this.reportService.getMonthlyMeteoDataList();

        options.setColumnNumber(options.getColumnNumber() - 1);
        options.setRowNumber(options.getRowNumber() - 1);
        try {
            this.reportService.editAllDataWithUserChanges(allData, columns, options);
        } catch (ParseException e) {
            logger.debug(String.format(
                    "Error, can't parse user string to date from \"/editing-report\", %s",
                    e.getMessage()));
            e.printStackTrace();

            return "redirect:/";
        }

        model.addAttribute("allData", allData);
        model.addAttribute("headRows", this.reportService.getHeadRows(columns));
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", options);

        return "report";
    }
}