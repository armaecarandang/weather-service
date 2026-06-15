package com.armae.weather.provider;

import com.armae.weather.config.WeatherStackProperties;
import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.model.provider.weatherstack.Current;
import com.armae.weather.model.provider.weatherstack.WeatherStackError;
import com.armae.weather.model.provider.weatherstack.WeatherStackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@Order(1)
public class WeatherStackProvider implements WeatherProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherStackProvider.class);

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

        URI uri =
                UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                        .path("/current")
                        .queryParam("access_key", properties.getApiKey())
                        .queryParam("query", city)
                        .build()
                        .encode()
                        .toUri();

        WeatherStackResponse response;

        try {
            LOGGER.debug("Calling Weatherstack for city={}", city);
            response =
                    restClient.get()
                            .uri(uri)
                            .retrieve()
                            .body(WeatherStackResponse.class);
        } catch (RestClientException exception) {
            LOGGER.warn("Weatherstack request failed for city={}", city, exception);
            throw new WeatherProviderException(
                    "Weather provider request failed",
                    exception);
        }

        Current current = extractCurrent(response);

        return new WeatherResponse(
                current.getTemperature(),
                current.getWindSpeed()
        );
    }

    private Current extractCurrent(
            WeatherStackResponse response) {

        if (response == null) {
            LOGGER.warn("Weatherstack returned an empty response");
            throw new WeatherProviderException("Weather provider returned an empty response");
        }

        if (Boolean.FALSE.equals(response.getSuccess()) || response.getError() != null) {
            LOGGER.warn("Weatherstack returned an error response type={}", getErrorType(response.getError()));
            throw new WeatherProviderException(formatProviderError(response.getError()));
        }

        Current current = response.getCurrent();

        if (current == null
                || current.getTemperature() == null
                || current.getWindSpeed() == null) {

            LOGGER.warn("Weatherstack returned an incomplete response");
            throw new WeatherProviderException("Weather provider returned an incomplete response");
        }

        return current;
    }

    private String formatProviderError(
            WeatherStackError error) {

        if (error == null) {
            return "Weather provider rejected the request";
        }

        if (error.getInfo() != null && !error.getInfo().isBlank()) {
            return error.getInfo();
        }

        if (error.getType() != null && !error.getType().isBlank()) {
            return "Weather provider error: " + error.getType();
        }

        return "Weather provider rejected the request";
    }

    private String getErrorType(
            WeatherStackError error) {

        if (error == null || error.getType() == null || error.getType().isBlank()) {
            return "unknown";
        }

        return error.getType();
    }
}
