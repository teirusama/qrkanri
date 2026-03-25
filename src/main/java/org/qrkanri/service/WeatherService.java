package org.qrkanri.service;

import org.qrkanri.dto.WeatherDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherDto getTomorrowWeather() {
        String url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=37.5665" +
                "&longitude=126.9780" +
                "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
                "&timezone=Asia/Seoul";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("daily") == null) {
            return null;
        }
        Map<String, Object> daily = (Map<String, Object>) response.get("daily");
        List<String> time = (List<String>) daily.get("time");
        List<Number> maxTemp = (List<Number>) daily.get("temperature_2m_max");
        List<Number> minTemp = (List<Number>) daily.get("temperature_2m_min");
        List<Number> weatherCode = (List<Number>) daily.get("weathercode");
        if (time == null || maxTemp == null || minTemp == null || weatherCode == null) {
            return null;
        }
        if (time.size() < 2 || maxTemp.size() < 2 || minTemp.size() < 2 || weatherCode.size() < 2) {
            return null;
        }
        int index = 1;
        WeatherDto dto = new WeatherDto();
        dto.setDate(time.get(index));
        dto.setMaxTemp(maxTemp.get(index).doubleValue());
        dto.setMinTemp(minTemp.get(index).doubleValue());
        dto.setWeather(getWeatherText(weatherCode.get(index).intValue()));
        return dto;
    }

    private String getWeatherText(int code) {
        if (code == 0) return "晴れ";
        if (code == 1 || code == 2 || code == 3) return "曇り";
        if (code >= 45 && code <= 48) return "霧";
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return "雨";
        if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) return "雪";
        if (code >= 95 && code <= 99) return "強風";
        return "確認不可";
    }
}