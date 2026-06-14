package com.armae.weather.model.provider.weatherstack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Current {

    private Double temperature;

    @JsonProperty("wind_speed")
    private Double windSpeed;

}