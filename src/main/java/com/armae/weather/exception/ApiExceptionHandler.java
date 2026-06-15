package com.armae.weather.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(WeatherProviderException.class)
    public ProblemDetail handleWeatherProviderException(
            WeatherProviderException exception) {

        LOGGER.warn("Returning provider error response: {}", exception.getMessage());

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_GATEWAY,
                        exception.getMessage());
        problemDetail.setTitle("Weather provider error");

        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(
            ConstraintViolationException exception) {

        LOGGER.debug("Returning validation error response: {}", exception.getMessage());

        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage());
        problemDetail.setTitle("Invalid request");

        return problemDetail;
    }
}
