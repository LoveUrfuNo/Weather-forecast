package com.testlinenergo.model;

/**
 * Класс для хранения параметров редакторования отчета.
 */
public class EditingOptions {
    private Integer rowNumber;

    private Integer columnNumber;

    private String replacingData;

    private NeedOfColumns columns;

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getReplacingData() {
        return replacingData;
    }

    public void setReplacingData(String replacingData) {
        this.replacingData = replacingData;
    }

    public NeedOfColumns getColumns() {
        return columns;
    }

    public void setColumns(NeedOfColumns columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "EditingOptions{" +
                "rowNumber=" + rowNumber +
                ", columnNumber=" + columnNumber +
                ", replacingData='" + replacingData + '\'' +
                ", columns=" + columns +
                '}';
    }
}
