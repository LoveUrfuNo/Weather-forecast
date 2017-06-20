package com.testlinenergo.controller;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.model.EditingOptions;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss z yyyy";

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

    /**
     * Добавляет ежедневные данные в бд.
     *
     * @param data - данные в json
     */
    @RequestMapping(value = "/add-meteo-data", method = RequestMethod.GET)
    public void addMeteoData(JSONObject data) {
        DateFormat formatter
                = new SimpleDateFormat(DATE_FORMAT, Locale.US);

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

    /**
     * Обновляет все отчеты (по условию - каждый месяц).
     *
     * @param needfulColumns - строка, состоящая из пяти подстрок(true или false), разбитых '_',
     *                       хранящая значения полей объекта класса "NeedOfColumns":
     *                       timestampNeed, temperatureNeed, pressureNeed, windDirectionNeed, windSpeedNeed
     * @param optionsString  - строка, состоящая из 3 подстрок, разбитых '_', хранящая значения полей
     *                       объекта класса "EditingOptions": rowNumber, columnNumber, replacingData
     */
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

    /**
     * Направляет к начальной странице.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showReportGet(Model model) {
        this.reportService.makeModelForStartPage(model);

        NeedOfColumns columns = this.reportService.getColumnForFullReport();
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

        return "report";
    }

    /**
     * Направляет на начальную страницу. (см функцию downloadReportByIndex(...))
     */
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String showReportPost(Model model) {
        this.reportService.makeModelForStartPage(model);

        NeedOfColumns columns = this.reportService.getColumnForFullReport();
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

        return "report";
    }

    /**
     * Напрвляет на страницу с выбором колонок для отчета.
     */
    @RequestMapping(value = "/choice-columns", method = RequestMethod.GET)
    public String choiceColumns(Model model) {
        model.addAttribute("needfulColumns", new NeedOfColumns());

        return "choice-columns";
    }

    /**
     * Направляет на страницу с новым отчетом.
     */
    @RequestMapping(value = "/show-partial-report", method = RequestMethod.POST)
    public String showEditReport(@ModelAttribute(name = "needfulColumns") NeedOfColumns columns,
                                 Model model) {
        model.addAttribute("headRows", this.reportService.getHeadRows(columns));
        model.addAttribute("allData", this.reportService.getMonthlyMeteoDataList());
        model.addAttribute("needfulColumns", columns);
        model.addAttribute("options", new EditingOptions());

        return "report";
    }

    /**
     * Скачивает главный отчет "monthly-meteo-report.xls".
     */
    @RequestMapping(value = "/download-full-report", method = RequestMethod.GET)
    public void downloadFull(HttpServletResponse response) {
        File reportFile = new File(FILE_PATH, FILE_NAME + FILE_TYPE);
        if (!reportFile.isFile()) {
            List<MeteoStationData> allData = this.reportService.getMonthlyMeteoDataList();
            this.reportService.createReport(allData, FILE_PATH + FILE_NAME + FILE_TYPE,
                    this.reportService.getColumnForFullReport(), FULL_REPORT_INDEX);
        }

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME,
                String.format("attachment; filename=\"%s\"", FILE_NAME + FILE_TYPE));

        this.reportService.addFileInResponse(reportFile, response);
    }

    /**
     * Скачивает отчет с изменениями и сохраняет. Если изменений нет, то вызывает функцию downloadFull(...).
     */
    @RequestMapping(value = "/download-report/{needfulColumns}/{options}",
            method = RequestMethod.GET)
    public void downloadEditing(@PathVariable(name = "needfulColumns") String needfulColumns,
                                @PathVariable(name = "options") String optionsString,
                                HttpServletResponse response) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        EditingOptions options = null;
        // если изменений нет, то options остается null
        if (Arrays.stream(optionsString.split("_")).noneMatch(str -> str.equals("null"))) {
            options = this.reportService.parseStringWithOptions(optionsString, columns);
        }
        // если выбраны все колонки и нет изменений, то скачиваем полный отчет
        if (Arrays.stream(needfulColumns.split("_")).noneMatch(col -> col.equals("false"))
                && null == options) {
            downloadFull(response);

            return;
        }

        List<MeteoStationData> allData
                = this.reportService.getMonthlyMeteoDataList();
        try {     // корректировка данных для отчета с учетом новых параметров, введенных пользователем
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

    /**
     * Скачивает сохраненный отчет, предварительно найденный по его индексу, присвоенному при сохранении.
     *
     * @param report - report.getReportId() - номер отчета
     */
    @RequestMapping(value = "/download-report-by-index", method = RequestMethod.POST)
    public void downloadReportByIndex(@ModelAttribute(name = "report") Report report,
                                      HttpServletResponse response, HttpServletRequest request) {
        File reportFile = new File(FILE_PATH,
                FILE_NAME + "_number" + report.getReportId() + FILE_TYPE);
        if (!reportFile.isFile()) {
            try {     // перенаправляет на начальную страницу (в функцию showReportPost(...))
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

    /**
     * Сохраняет отчет.
     *
     * @param needfulColumns - строка, состоящая из пяти подстрок(true или false), разбитых '_',
     *                       хранящая значения полей объекта класса "NeedOfColumns":
     *                       timestampNeed, temperatureNeed, pressureNeed, windDirectionNeed, windSpeedNeed
     * @param optionsString  - строка, состоящая из 3 подстрок, разбитых '_', хранящая значения полей
     *                       объекта класса "EditingOptions": rowNumber, columnNumber, replacingData
     * @return название страницы, на которой показывается присвоенный отчету номер после сохранения.
     */
    @RequestMapping(value = "/save-partial-report/{needfulColumns}/{options}", method = RequestMethod.GET)
    public String saveReport(@PathVariable(name = "needfulColumns") String needfulColumns,
                             @PathVariable(name = "options") String optionsString,
                             Model model) {
        NeedOfColumns columns = this.reportService.
                parseStringWithLogicalColumnValues(needfulColumns);
        EditingOptions options = null;
        if (Arrays.stream(optionsString.split("_")).noneMatch(str -> str.equals("null"))) {
            options = this.reportService.parseStringWithOptions(optionsString, columns);
        }

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

    /**
     * Направляет на страницу, с заполнением новых данных для отчета.
     *
     * @param needfulColumns - строка, состоящая из пяти подстрок(true или false), разбитых '_',
     *                       хранящая значения полей объекта класса "NeedOfColumns":
     *                       timestampNeed, temperatureNeed, pressureNeed, windDirectionNeed, windSpeedNeed
     */
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
        // например:
        // если пользоватаель имеет в виду первую строку и первый столбец, то он вводит 1:1,
        // но в структурах данных этот элемент будет хранится под номером 0:0
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