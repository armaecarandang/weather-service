package com.armae.weather.provider;

import com.armae.weather.model.WeatherResponse;

public interface WeatherProvider {
    WeatherResponse getWeather(String city);
}
