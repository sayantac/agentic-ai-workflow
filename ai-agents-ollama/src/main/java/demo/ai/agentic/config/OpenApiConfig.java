package demo.ai.agentic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ollamaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Agents Ollama API")
                        .description("OpenAPI documentation for Ollama-powered wine assistant")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi ollamaGroup() {
        return GroupedOpenApi.builder()
                .group("ollama")
                .packagesToScan("demo.ai.agentic.controller")
                .build();
    }
}


