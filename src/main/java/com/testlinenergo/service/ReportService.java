package com.testlinenergo.service;

import com.testlinenergo.model.EditingOptions;
import com.testlinenergo.model.MeteoStationData;
import com.testlinenergo.model.NeedOfColumns;
import com.testlinenergo.model.Report;

import org.springframework.ui.Model;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Сервис для всех операций, требующихся для взаимодействия с отчетом.
 */
public interface ReportService {
    /**
     * Создает и сохраняет файл с отчетом.
     *
     * @param allData  - данные, из которых будет составлен отчет
     * @param filePath - полное путь до файла
     * @param columns  - нужные колонки в отчете, выбранные пользователем
     * @param index    - номер отчета
     * @return выполнился ли отчет
     */
    boolean createReport(List<MeteoStationData> allData, final String filePath, NeedOfColumns columns, long index);

    /**
     * Возвращает все данные из бд, поступившие за последний месяц.
     */
    List<MeteoStationData> getMonthlyMeteoDataList();

    /**
     * Сохраняет данные об отчете в таблицу "report"
     */
    void saveReport(Report report);

    /**
     * Возвращает список с полями из columns.
     *
     * @param columns - нужные колонки в отчете, выбранные пользователем
     */
    List<String> getHeadRows(NeedOfColumns columns);

    /**
     * Заполняет response из файла для последующего скачивания этого файла.
     */
    void addFileInResponse(File reportFile, HttpServletResponse response);

    /**
     * Создает отчет и методом saveReport(report) сохраняет
     * данные о нем в бд и возвращает имя сохраненного файла.
     *
     * @param allData - данные, из которычх будет составлен отчет
     * @param columns - нужные колонки в отчете, выбранные пользователем
     */
    String saveEditingReportAndGetFileNAme(List<MeteoStationData> allData, NeedOfColumns columns);

    /**
     * Из строки заполняет объект NeedOfColumns.
     */
    NeedOfColumns parseStringWithLogicalColumnValues(String needfulColumns);

    /**
     * Заполняет model для двух идентичных сервлетов.
     */
    void makeModelForStartPage(Model model);

    /**
     * Редактирует все данные за последний месяц на основе параметров, заданных пользователем.
     *
     * @param allData - данные, которые будут редактироваться и фильтроваться
     */
    void editAllDataWithUserChanges(List<MeteoStationData> allData,
                                    NeedOfColumns columns, EditingOptions options) throws ParseException;

    /**
     * Из строки и объекта NeedOfColumns заполняет объект Options.
     */
    EditingOptions parseStringWithOptions(String optionsString, NeedOfColumns columns);

    /**
     * Обновляет все сохраненные отчеты, заполняя их новыми данными, но не трогая интерфейс.
     *
     * @param allData
     */
    void updateAllReports(List<MeteoStationData> allData) throws IOException;

    /**
     * @return объект NeedOfColumns со всеми полями true.
     */
    NeedOfColumns getColumnForFullReport();
}