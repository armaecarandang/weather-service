package com.armae.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openweather")
public class OpenWeatherProperties {

    private String apiKey;

    private String baseUrl;
}
