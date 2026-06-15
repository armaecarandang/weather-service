package com.armae.weather.service;

import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.provider.WeatherProvider;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherServiceTest {

    @Test
    void getWeatherFailsOverToSecondProviderWhenPrimaryFails() {

        WeatherResponse failoverResponse = new WeatherResponse(19, 8);
        WeatherService service =
                new WeatherService(
                        List.of(
                                new FailingProvider(),
                                new SuccessfulProvider(failoverResponse)),
                        Clock.systemUTC(),
                        Duration.ofSeconds(3));

        WeatherResponse response = service.getWeather("melbourne");

        assertThat(response).isSameAs(failoverResponse);
    }

    @Test
    void getWeatherReturnsFreshCachedResponseWithoutCallingProviderAgain() {

        MutableClock clock = new MutableClock(Instant.parse("2026-06-15T00:00:00Z"));
        CountingProvider provider = new CountingProvider(new WeatherResponse(20, 10));
        WeatherService service =
                new WeatherService(
                        List.of(provider),
                        clock,
                        Duration.ofSeconds(3));

        WeatherResponse firstResponse = service.getWeather("melbourne");
        WeatherResponse secondResponse = service.getWeather("Melbourne");

        assertThat(secondResponse).isSameAs(firstResponse);
        assertThat(provider.callCount()).isEqualTo(1);
    }

    @Test
    void getWeatherReturnsStaleCachedResponseWhenAllProvidersFail() {

        MutableClock clock = new MutableClock(Instant.parse("2026-06-15T00:00:00Z"));
        SwitchableProvider provider = new SwitchableProvider(new WeatherResponse(22, 9));
        WeatherService service =
                new WeatherService(
                        List.of(provider),
                        clock,
                        Duration.ofSeconds(3));

        WeatherResponse cachedResponse = service.getWeather("melbourne");
        provider.fail();
        clock.advance(Duration.ofSeconds(4));

        WeatherResponse staleResponse = service.getWeather("melbourne");

        assertThat(staleResponse).isSameAs(cachedResponse);
    }

    @Test
    void getWeatherThrowsWhenAllProvidersFailAndNoCacheExists() {

        WeatherService service =
                new WeatherService(
                        List.of(new FailingProvider()),
                        Clock.systemUTC(),
                        Duration.ofSeconds(3));

        assertThatThrownBy(() -> service.getWeather("melbourne"))
                .isInstanceOf(WeatherProviderException.class)
                .hasMessage("Provider failed");
    }

    private static class FailingProvider implements WeatherProvider {

        @Override
        public WeatherResponse getWeather(
                String city) {

            throw new WeatherProviderException("Provider failed");
        }
    }

    private static class SuccessfulProvider implements WeatherProvider {

        private final WeatherResponse response;

        private SuccessfulProvider(
                WeatherResponse response) {

            this.response = response;
        }

        @Override
        public WeatherResponse getWeather(
                String city) {

            return response;
        }
    }

    private static class CountingProvider implements WeatherProvider {

        private final WeatherResponse response;
        private int callCount;

        private CountingProvider(
                WeatherResponse response) {

            this.response = response;
        }

        @Override
        public WeatherResponse getWeather(
                String city) {

            callCount++;

            return response;
        }

        private int callCount() {
            return callCount;
        }
    }

    private static class SwitchableProvider implements WeatherProvider {

        private final WeatherResponse response;
        private boolean failing;

        private SwitchableProvider(
                WeatherResponse response) {

            this.response = response;
        }

        @Override
        public WeatherResponse getWeather(
                String city) {

            if (failing) {
                throw new WeatherProviderException("Provider failed");
            }

            return response;
        }

        private void fail() {
            failing = true;
        }
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(
                Instant instant) {

            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(
                ZoneId zone) {

            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(
                Duration duration) {

            instant = instant.plus(duration);
        }
    }
}
