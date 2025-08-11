package demo.ai.agentic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bedrockOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Agents Bedrock API")
                        .description("OpenAPI documentation for Bedrock-powered dog adoption assistant")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi bedrockGroup() {
        return GroupedOpenApi.builder()
                .group("bedrock")
                .packagesToScan("demo.ai.agentic.controller")
                .build();
    }
}


