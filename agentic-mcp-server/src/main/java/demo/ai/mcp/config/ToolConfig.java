package demo.ai.mcp.config;

import demo.ai.mcp.service.DogAdoptionSchedulerService;
import demo.ai.mcp.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    ToolCallbackProvider toolCallbackProvider(DogAdoptionSchedulerService schedulerService,
                                              WeatherService weatherService) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(schedulerService, weatherService)
                .build();
    }
} 