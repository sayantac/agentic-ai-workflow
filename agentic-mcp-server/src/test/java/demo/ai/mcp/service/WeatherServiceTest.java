package demo.ai.mcp.service;

import demo.ai.mcp.record.WeatherResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherServiceTest {

    private final WeatherService weatherService = new WeatherService();

    @Test
    void getCurrentWeather_ShouldReturnMockedWeatherResponse() {
        String city = "Berlin";
        WeatherResponse response = weatherService.getCurrentWeather(city);

        assertThat(response.city()).isEqualTo(city);
        assertThat(response.description()).isEqualTo("Clear sky");
        assertThat(response.temperature()).isEqualTo(25);
        assertThat(response.humidity()).isEqualTo(40);
        assertThat(response.windSpeed()).isEqualTo(5);
    }
} 