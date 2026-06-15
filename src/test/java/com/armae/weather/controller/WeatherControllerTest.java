package com.armae.weather.controller;

import com.armae.weather.exception.WeatherProviderException;
import com.armae.weather.model.WeatherResponse;
import com.armae.weather.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void getWeatherReturnsWeatherResponse() throws Exception {

        when(weatherService.getWeather("Taipei"))
                .thenReturn(new WeatherResponse(30, 12));

        mockMvc.perform(get("/v1/weather")
                .param("city", "Taipei"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature_degrees").value(30))
                .andExpect(jsonPath("$.wind_speed").value(12));

        verify(weatherService).getWeather("Taipei");
    }

    @Test
    void getWeatherRejectsBlankCity() throws Exception {

        mockMvc.perform(get("/v1/weather")
                        .param("city", " "))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(weatherService);
    }

    @Test
    void getWeatherRejectsMissingCity() throws Exception {

        mockMvc.perform(get("/v1/weather"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(weatherService);
    }

    @Test
    void getWeatherRejectsTooLongCity() throws Exception {

        mockMvc.perform(get("/v1/weather")
                        .param("city", "a".repeat(101)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(weatherService);
    }

    @Test
    void getWeatherReturnsBadGatewayForProviderError() throws Exception {

        when(weatherService.getWeather("Atlantis"))
                .thenThrow(new WeatherProviderException("Your API request failed."));

        mockMvc.perform(get("/v1/weather")
                        .param("city", "Atlantis"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Weather provider error"))
                .andExpect(jsonPath("$.detail").value("Your API request failed."));

        verify(weatherService).getWeather("Atlantis");
    }
}
