package com.armae.weather.service;

import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.provider.WeatherProvider;
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
            return cachedWeather.response();
        }

        WeatherProviderException lastException = null;

        for (WeatherProvider weatherProvider : weatherProviders) {
            try {
                WeatherResponse response = weatherProvider.getWeather(city);
                cache.put(cacheKey, new CachedWeather(response, now));

                return response;
            } catch (WeatherProviderException exception) {
                lastException = exception;
            }
        }

        if (cachedWeather != null) {
            return cachedWeather.response();
        }

        if (lastException != null) {
            throw lastException;
        }

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
