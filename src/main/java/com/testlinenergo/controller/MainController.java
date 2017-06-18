package com.testlinenergo.controller;

import com.testlinenergo.dao.MeteoDataDao;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.service.ReportService;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
        final String currentFileName = FILE_NAME + FILE_TYPE;
        this.reportService.createReport(FILE_PATH + currentFileName, columns);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showReport(Model model, @ModelAttribute NeedOfColumns columns) {
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
        List<String> headRows = new LinkedList<>();
        if(columns.getTemperatureNeed()) headRows.add(READ_TIME);
        if(columns.getTemperatureNeed()) headRows.add(TEMPERATURE);
        if(columns.getPressureNeed()) headRows.add(PRESSURE);
        if(columns.getWindDirectionNeed()) headRows.add(WIND_DIR);
        if(columns.getWindSpeedNeed()) headRows.add(WIND_SPEED);

        if (headRows.isEmpty()) headRows.add(READ_TIME);

        List<MeteoStationData> allData = this.reportService.getMonthlyMeteoDataList();
        allData.sort(Comparator.comparing(MeteoStationData::getReadTimestamp));

        model.addAttribute("headRows", headRows);
        model.addAttribute("allData", allData);

        return "report";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void download(HttpServletResponse response) {
        File myFile = new File(FILE_PATH, FILE_NAME + FILE_TYPE);

        response.setContentType(CONTENT_TYPE);
        response.setHeader(HEADER_NAME, String.format("attachment; filename=\"%s\"", FILE_NAME + FILE_TYPE));

        try (OutputStream out = response.getOutputStream();
             FileInputStream in = new FileInputStream(myFile)) {
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

    @ResponseBody
    @RequestMapping(value = "/get-json", method = RequestMethod.GET)
    public JSONObject getJson(List<MeteoStationData> data) {              //TODO: PERDELATb
        return new JSONObject(data);
    }
}
