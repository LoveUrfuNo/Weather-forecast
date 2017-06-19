package com.testlinenergo.service;

import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * Created by kosty on 17.06.2017.
 */
public interface ReportService {
    boolean createReport(final String filename, NeedOfColumns columns, long index);

    List<MeteoStationData> getMonthlyMeteoDataList();

    void saveReport(Report report);

    List<String> getHeadRows(NeedOfColumns columns);

    void addFileInResponse(File reportFile, HttpServletResponse response);

    String saveEditingReportAndGetFileNAme(NeedOfColumns columns);

    NeedOfColumns parseStringWithLogicalColumnValues(String needfulColumns);

    void makeModelForStartPage(Model model);
}