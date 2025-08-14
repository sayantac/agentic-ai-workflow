package demo.ai.mcp.service;

import demo.ai.mcp.record.WeatherResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherService {

    @Tool(description = "Get the current weather for a given city.")
    public WeatherResponse getCurrentWeather(@ToolParam(description = "The name of the city") String city) {
        return new WeatherResponse(city, "Clear sky", 25, 40, 5);
    }
} 