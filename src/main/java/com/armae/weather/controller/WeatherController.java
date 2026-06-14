package com.armae.weather.controller;

import com.armae.weather.model.WeatherResponse;
import com.armae.weather.service.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(
            WeatherService weatherService) {

        this.weatherService = weatherService;
    }

    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam String city) {

        return weatherService.getWeather(city);
    }
}