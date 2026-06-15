package com.armae.weather.model.provider.weatherstack;

import lombok.Data;

@Data
public class WeatherStackResponse {

    private Boolean success;

    private Current current;

    private WeatherStackError error;
}
