package com.armae.weather.provider;

import com.armae.weather.config.WeatherStackProperties;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.model.provider.weatherstack.WeatherStackResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherStackProvider implements WeatherProvider {

    private final RestClient restClient;
    private final WeatherStackProperties properties;

    public WeatherStackProvider(
            RestClient restClient,
            WeatherStackProperties properties) {

        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public WeatherResponse getWeather(String city) {

        String url =
                properties.getBaseUrl()
                        + "/current?access_key="
                        + properties.getApiKey()
                        + "&query="
                        + city;

        WeatherStackResponse response =
                restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(WeatherStackResponse.class);

        return new WeatherResponse(
                response.getCurrent().getTemperature(),
                response.getCurrent().getWindSpeed()
        );
    }
}