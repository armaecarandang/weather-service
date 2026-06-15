# Weather Service

Spring Boot service that returns current weather data using Weatherstack as the primary provider and OpenWeatherMap as failover.

The service returns a unified response with temperature in degrees Celsius and wind speed.

## Technologies

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Validation
- Spring RestClient
- Maven
- JUnit 5
- Mockito
- Lombok

## Local Config

Create `devops/local/application-local.yml` and add your API keys:

```yaml
weatherstack:
  api-key: YOUR_WEATHERSTACK_KEY
  base-url: http://api.weatherstack.com

openweather:
  api-key: YOUR_OPENWEATHER_KEY
  base-url: https://api.openweathermap.org
```

`devops/local/` is ignored by Git.

## Run From IntelliJ

Open the project in IntelliJ, then run this from the IntelliJ Terminal:

```shell
.\mvnw.cmd spring-boot:run
```

The app runs on:

```text
http://localhost:8080
```

## Test With Postman

Use this request:

```http
GET http://localhost:8080/v1/weather?city=melbourne
```

Example response:

```json
{
  "temperature_degrees": 21.5,
  "wind_speed": 7.2
}
```

## Run Automated Tests

From the IntelliJ Terminal:

```shell
.\mvnw.cmd test
```
