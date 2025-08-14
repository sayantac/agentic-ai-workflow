package demo.ai.mcp.record;

public record WeatherResponse(String city, String description, int temperature,
                              int humidity, int windSpeed) {
}
