package com.testlinenergo.service;

import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;

import java.util.List;

/**
 * Created by kosty on 17.06.2017.
 */
public interface ReportService {
    boolean createReport(final String fileName, NeedOfColumns columns);

    List<MeteoStationData> getMonthlyMeteoDataList();
}
