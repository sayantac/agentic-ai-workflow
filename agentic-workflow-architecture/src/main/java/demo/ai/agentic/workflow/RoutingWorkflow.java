package demo.ai.agentic.workflow;

import demo.ai.agentic.record.RoutingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.Map;

public class RoutingWorkflow {

    private static final Logger log = LoggerFactory.getLogger(RoutingWorkflow.class);

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Routes input to a specialized prompt based on content classification. This
     * method first analyzes the input to determine the most appropriate route, then
     * processes the input using the specialized prompt for that route.
     *
     * <p>
     * The routing process involves:
     * <ol>
     * <li>Content analysis to determine the appropriate category</li>
     * <li>Selection of a specialized prompt optimized for that category</li>
     * <li>Processing the input with the selected prompt</li>
     * </ol>
     *
     * <p>
     * This approach allows for:
     * <ul>
     * <li>Better handling of diverse input types</li>
     * <li>Optimization of prompts for specific categories</li>
     * <li>Improved accuracy through specialized processing</li>
     * </ul>
     *
     * @param input  The input text to be routed and processed
     * @param routes Map of route names to their corresponding specialized prompts
     * @return Processed response from the selected specialized route
     */
    public String route(String input, Map<String, String> routes) {
        Assert.notNull(input, "Input text cannot be null");
        Assert.notEmpty(routes, "Routes map cannot be null or empty");

        // Determine the appropriate route for the input
        String routeKey = determineRoute(input, routes.keySet());

        // Get the selected prompt from the route map
        String selectedPrompt = routes.get(routeKey);

        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }

        // Process the input with the selected prompt
        return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
    }

    /**
     * Analyzes the input content and determines the most appropriate route based on
     * content classification. The classification process considers key terms,
     * context, and patterns in the input to select the optimal route.
     *
     * <p>
     * The method uses an LLM to:
     * <ul>
     * <li>Analyze the input content and context</li>
     * <li>Consider the available routing options</li>
     * <li>Provide reasoning for the routing decision</li>
     * <li>Select the most appropriate route</li>
     * </ul>
     *
     * @param input The input text to analyze for routing
     * @param availableRoutes The set of available routing options
     * @return The selected route key based on content analysis
     */
    private String determineRoute(String input, Iterable<String> availableRoutes) {
        System.out.println("\nAvailable routes: " + availableRoutes);

        String selectorPrompt = String.format("""
                Analyze the input and select the most appropriate support team from these options: %s
                First explain your reasoning, then provide your selection in this JSON format:
                
                \\{
                    "reasoning": "Brief explanation of why this ticket should be routed to a specific team.
                                Consider key terms, user intent, and urgency level.",
                    "selection": "The chosen team name"
                \\}
                
                Input: %s""", availableRoutes, input);

        RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

        assert routingResponse != null;

        log.info("Routing Analysis: {}\nSelected route: {}",
                routingResponse.reasoning(),
                routingResponse.selection());

        return routingResponse.selection();
    }
}
