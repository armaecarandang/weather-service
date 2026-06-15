package com.armae.weather.service;

import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.provider.WeatherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherService.class);

    private final List<WeatherProvider> weatherProviders;
    private final Clock clock;
    private final Duration cacheTtl;
    private final Map<String, CachedWeather> cache = new ConcurrentHashMap<>();

    @Autowired
    public WeatherService(
            List<WeatherProvider> weatherProviders) {

        this(weatherProviders, Clock.systemUTC(), Duration.ofSeconds(3));
    }

    WeatherService(
            List<WeatherProvider> weatherProviders,
            Clock clock,
            Duration cacheTtl) {

        this.weatherProviders = weatherProviders;
        this.clock = clock;
        this.cacheTtl = cacheTtl;
    }

    public WeatherResponse getWeather(
            String city) {

        String cacheKey = city.trim().toLowerCase(Locale.ROOT);
        Instant now = Instant.now(clock);
        CachedWeather cachedWeather = cache.get(cacheKey);

        if (cachedWeather != null && cachedWeather.isFresh(now, cacheTtl)) {
            LOGGER.debug("Returning fresh cached weather for city={}", cacheKey);

            return cachedWeather.response();
        }

        WeatherProviderException lastException = null;

        for (WeatherProvider weatherProvider : weatherProviders) {
            try {
                LOGGER.debug(
                        "Requesting weather for city={} using provider={}",
                        cacheKey,
                        weatherProvider.getClass().getSimpleName());

                WeatherResponse response = weatherProvider.getWeather(city);
                cache.put(cacheKey, new CachedWeather(response, now));
                LOGGER.info(
                        "Weather lookup succeeded for city={} provider={}",
                        cacheKey,
                        weatherProvider.getClass().getSimpleName());

                return response;
            } catch (WeatherProviderException exception) {
                LOGGER.warn(
                        "Weather provider failed for city={} provider={} reason={}",
                        cacheKey,
                        weatherProvider.getClass().getSimpleName(),
                        exception.getMessage());
                lastException = exception;
            }
        }

        if (cachedWeather != null) {
            LOGGER.warn("Returning stale cached weather for city={} because all providers failed", cacheKey);

            return cachedWeather.response();
        }

        if (lastException != null) {
            LOGGER.error("Weather lookup failed for city={} and no cached value is available", cacheKey);

            throw lastException;
        }

        LOGGER.error("Weather lookup failed for city={} because no providers are configured", cacheKey);

        throw new WeatherProviderException("No weather providers are configured");
    }

    private record CachedWeather(
            WeatherResponse response,
            Instant cachedAt) {

        private boolean isFresh(
                Instant now,
                Duration cacheTtl) {

            return cachedAt.plus(cacheTtl).isAfter(now);
        }
    }
}
