package com.armae.weather.service;

import com.armae.weather.model.WeatherResponse;
import com.armae.weather.provider.WeatherProvider;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final WeatherProvider weatherProvider;

    public WeatherService(
            WeatherProvider weatherProvider) {

        this.weatherProvider = weatherProvider;
    }

    public WeatherResponse getWeather(
            String city) {

        return weatherProvider.getWeather(city);
    }
}