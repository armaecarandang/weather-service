package com.armae.weather.provider;

import com.armae.weather.config.OpenWeatherProperties;
import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.model.provider.openweather.OpenWeatherResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@Order(2)
public class OpenWeatherProvider implements WeatherProvider {

    private final RestClient restClient;
    private final OpenWeatherProperties properties;

    public OpenWeatherProvider(
            RestClient restClient,
            OpenWeatherProperties properties) {

        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public WeatherResponse getWeather(
            String city) {

        URI uri =
                UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                        .path("/data/2.5/weather")
                        .queryParam("q", formatCityQuery(city))
                        .queryParam("appid", properties.getApiKey())
                        .queryParam("units", "metric")
                        .build()
                        .encode()
                        .toUri();

        OpenWeatherResponse response;

        try {
            response =
                    restClient.get()
                            .uri(uri)
                            .retrieve()
                            .body(OpenWeatherResponse.class);
        } catch (RestClientException exception) {
            throw new WeatherProviderException(
                    "OpenWeatherMap request failed",
                    exception);
        }

        if (response == null
                || response.getMain() == null
                || response.getMain().getTemp() == null
                || response.getWind() == null
                || response.getWind().getSpeed() == null) {

            throw new WeatherProviderException("OpenWeatherMap returned an incomplete response");
        }

        return new WeatherResponse(
                response.getMain().getTemp(),
                response.getWind().getSpeed());
    }

    private String formatCityQuery(
            String city) {

        if (city.contains(",")) {
            return city;
        }

        return city + ",AU";
    }
}
