package demo.ai.agentic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agentWorkflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agent Workflow Architecture API")
                        .description("OpenAPI documentation for agentic workflow endpoints")
                        .version("v1"));
    }

    @Bean
    public GroupedOpenApi workflowGroup() {
        return GroupedOpenApi.builder()
                .group("workflow")
                .packagesToScan("demo.ai.agentic.controller")
                .build();
    }
}


