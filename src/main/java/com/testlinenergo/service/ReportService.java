package com.testlinenergo.service;

import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;

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

    void addFileInResponse(File filename, HttpServletResponse response);
}