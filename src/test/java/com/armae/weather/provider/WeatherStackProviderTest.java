package com.armae.weather.provider;

import com.armae.weather.config.WeatherStackProperties;
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

class WeatherStackProviderTest {

    private MockRestServiceServer server;

    private WeatherStackProvider provider;

    @BeforeEach
    void setUp() {

        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();

        WeatherStackProperties properties = new WeatherStackProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("http://api.weatherstack.com");

        provider = new WeatherStackProvider(
                restClientBuilder.build(),
                properties);
    }

    @Test
    void getWeatherMapsSuccessfulWeatherStackResponse() {

        server.expect(requestTo("http://api.weatherstack.com/current?access_key=test-key&query=New%20York"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "current": {
                            "temperature": 24,
                            "wind_speed": 11
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        WeatherResponse response = provider.getWeather("New York");

        assertThat(response.getTemperatureDegrees()).isEqualTo(24);
        assertThat(response.getWindSpeed()).isEqualTo(11);
        server.verify();
    }

    @Test
    void getWeatherThrowsProviderExceptionForWeatherStackError() {

        server.expect(requestTo("http://api.weatherstack.com/current?access_key=test-key&query=Atlantis"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "error": {
                            "code": 615,
                            "type": "request_failed",
                            "info": "Your API request failed."
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.getWeather("Atlantis"))
                .isInstanceOf(WeatherProviderException.class)
                .hasMessage("Your API request failed.");

        server.verify();
    }

    @Test
    void getWeatherThrowsProviderExceptionForIncompleteResponse() {

        server.expect(requestTo("http://api.weatherstack.com/current?access_key=test-key&query=Taipei"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "success": true
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.getWeather("Taipei"))
                .isInstanceOf(WeatherProviderException.class)
                .hasMessage("Weather provider returned an incomplete response");

        server.verify();
    }
}
