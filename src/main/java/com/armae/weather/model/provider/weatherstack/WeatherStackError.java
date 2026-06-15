package com.armae.weather.model.provider.weatherstack;

import lombok.Data;

@Data
public class WeatherStackError {

    private Integer code;

    private String type;

    private String info;
}
