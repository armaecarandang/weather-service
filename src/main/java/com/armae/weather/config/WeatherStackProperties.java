package com.armae.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "weatherstack")
public class WeatherStackProperties {

    private String apiKey;

    private String baseUrl;

}