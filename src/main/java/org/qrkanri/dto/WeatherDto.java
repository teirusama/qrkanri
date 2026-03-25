package org.qrkanri.dto;

import lombok.Data;

@Data
public class WeatherDto {
    private String date;
    private double maxTemp;
    private double minTemp;
    private String weather;
}
