package demo.ai.agentic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI googleAdkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Agents Google ADK API")
                        .description("OpenAPI documentation for Google ADK-powered travel assistant")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi googleAdkGroup() {
        return GroupedOpenApi.builder()
                .group("google-adk")
                .packagesToScan("demo.ai.agentic.controller")
                .build();
    }
}


