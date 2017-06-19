package com.testlinenergo.model;

/**
 * Класс для хранения выбранных столбцов при редактировании отчета.
 */
public class NeedOfColumns {
    private Boolean timestampNeed;

    private Boolean temperatureNeed;

    private Boolean pressureNeed;

    private Boolean windDirectionNeed;

    private Boolean windSpeedNeed;

    public Boolean getTimestampNeed() {
        return timestampNeed;
    }

    public void setTimestampNeed(Boolean timestampNeed) {
        this.timestampNeed = timestampNeed;
    }

    public Boolean getTemperatureNeed() {
        return temperatureNeed;
    }

    public void setTemperatureNeed(Boolean temperatureNeed) {
        this.temperatureNeed = temperatureNeed;
    }

    public Boolean getPressureNeed() {
        return pressureNeed;
    }

    public void setPressureNeed(Boolean pressureNeed) {
        this.pressureNeed = pressureNeed;
    }

    public Boolean getWindDirectionNeed() {
        return windDirectionNeed;
    }

    public void setWindDirectionNeed(Boolean windDirectionNeed) {
        this.windDirectionNeed = windDirectionNeed;
    }

    public Boolean getWindSpeedNeed() {
        return windSpeedNeed;
    }

    public void setWindSpeedNeed(Boolean windSpeedNeed) {
        this.windSpeedNeed = windSpeedNeed;
    }

    @Override
    public String toString() {
        return "NeedOfColumns{" +
                "timestampNeed=" + timestampNeed +
                ", temperatureNeed=" + temperatureNeed +
                ", pressureNeed=" + pressureNeed +
                ", windDirectionNeed=" + windDirectionNeed +
                ", windSpeedNeed=" + windSpeedNeed +
                '}';
    }
}