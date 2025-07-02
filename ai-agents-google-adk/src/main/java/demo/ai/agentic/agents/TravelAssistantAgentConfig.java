package demo.ai.agentic.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.AgentTool;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.GoogleSearchTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Configuration
public class TravelAssistantAgentConfig {

    @Value("${mcp.server.url}")
    private String mcpServerUrl;
    private final ObjectMapper objectMapper;

    public TravelAssistantAgentConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Initialize the Travel Assistant Agent with tools and configuration
     */
    @Bean
    public BaseAgent travelAssistantAgent() {
        var toolList = getTools();
        return LlmAgent.builder()
                .name("travel-assistant")
                .description("A travel assistant that helps users plan activities " +
                        "using current weather and other available information.")
                .model("gemini-2.0-flash")
                .instruction("""
                    You are a helpful travel assistant.
                    Your role is to assist users with planning their day, exploring cities,
                    and making informed decisions based on current weather condition.
                    You should:
                    - Provide activity suggestions and recommendations for different cities
                    - Consider weather conditions when suggesting activities or travel plans
                    - Help users plan things to do based on conditions or preferences
                    - Be friendly, concise, and conversational in your responses
                    Always use the tools available to provide accurate and helpful recommendations.
                """)
                .tools(toolList)
                .build();
    }

    /**
     * Configure and return the list of tools available to the agent
     */
    private ArrayList<BaseTool> getTools() {
        SseServerParameters params = SseServerParameters.builder().url(mcpServerUrl).build();
        McpToolset.McpToolsAndToolsetResult toolsAndToolsetResult;
        try {
            toolsAndToolsetResult = McpToolset.fromServer(params, objectMapper).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var toolList = toolsAndToolsetResult.getTools().stream().map(mcpTool -> (BaseTool) mcpTool)
                .collect(Collectors.toCollection(ArrayList::new));

        LlmAgent googleSearchAgent = LlmAgent.builder()
                .model("gemini-2.0-flash")
                .name("google_search_agent")
                .description("Search Google for current information")
                .instruction("""
                    You are a specialist in Google Search.
                    Use the Google Search tool to find current, accurate information.
                    Always provide sources and ensure the information is up-to-date.
                    Summarize the key findings clearly and concisely.
                """)
                .tools(new GoogleSearchTool())
                .outputKey("google_search_result")
                .build();

        AgentTool searchTool = AgentTool.create(googleSearchAgent, false);
        toolList.add(searchTool);
        return toolList;
    }
}
