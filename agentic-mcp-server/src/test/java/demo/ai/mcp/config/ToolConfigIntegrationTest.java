package demo.ai.mcp.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ToolConfigIntegrationTest {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    void toolCallbackProvider_ShouldBeConfigured() {
        assertThat(toolCallbackProvider).isNotNull();
    }
} 