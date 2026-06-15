package com.armae.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    @JsonProperty("temperature_degrees")
    private double temperatureDegrees;

    @JsonProperty("wind_speed")
    private double windSpeed;
}
