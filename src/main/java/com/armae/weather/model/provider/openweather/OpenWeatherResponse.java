package com.armae.weather.model.provider.openweather;

import lombok.Data;

@Data
public class OpenWeatherResponse {

    private OpenWeatherMain main;

    private OpenWeatherWind wind;
}
