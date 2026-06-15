package com.armae.weather.provider;

import com.armae.weather.config.OpenWeatherProperties;
import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenWeatherProviderTest {

    private MockRestServiceServer server;

    private OpenWeatherProvider provider;

    @BeforeEach
    void setUp() {

        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();

        OpenWeatherProperties properties = new OpenWeatherProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("http://api.openweathermap.org");

        provider = new OpenWeatherProvider(
                restClientBuilder.build(),
                properties);
    }

    @Test
    void getWeatherMapsSuccessfulOpenWeatherResponse() {

        server.expect(requestTo("http://api.openweathermap.org/data/2.5/weather?q=Melbourne,AU&appid=test-key&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "main": {
                            "temp": 21.5
                          },
                          "wind": {
                            "speed": 7.2
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherResponse response = provider.getWeather("Melbourne");

        assertThat(response.getTemperatureDegrees()).isEqualTo(21.5);
        assertThat(response.getWindSpeed()).isEqualTo(7.2);
        server.verify();
    }

    @Test
    void getWeatherThrowsProviderExceptionForIncompleteResponse() {

        server.expect(requestTo("http://api.openweathermap.org/data/2.5/weather?q=Melbourne,AU&appid=test-key&units=metric"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "main": {
                            "temp": 21.5
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.getWeather("Melbourne"))
                .isInstanceOf(WeatherProviderException.class)
                .hasMessage("OpenWeatherMap returned an incomplete response");

        server.verify();
    }
}
