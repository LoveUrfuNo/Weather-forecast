package com.testlinenergo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.util.Date;

/**
 * Класс для представления данных с метеостанции.
 */
@Entity
public class MeteoStationData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long meteoStationId;

    private Date readTimestamp;

    private Double temperature;

    private Integer pressure;

    private Integer windDirection;

    private Integer windSpeed;

    public Long getMeteoStationId() {
        return meteoStationId;
    }

    public void setMeteoStationId(Long meteoStationId) {
        this.meteoStationId = meteoStationId;
    }

    public Date getReadTimestamp() {
        return readTimestamp;
    }

    public void setReadTimestamp(Date readTimestamp) {
        this.readTimestamp = readTimestamp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    public Integer getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Integer windSpeed) {
        this.windSpeed = windSpeed;
    }

    @Override
    public String toString() {
        return "MeteoStationData{" +
                "meteoStationId=" + meteoStationId +
                ", readTimestamp=" + readTimestamp +
                ", temperature=" + temperature +
                ", pressure=" + pressure +
                ", windDirection=" + windDirection +
                ", windSpeed=" + windSpeed +
                '}';
    }
}
