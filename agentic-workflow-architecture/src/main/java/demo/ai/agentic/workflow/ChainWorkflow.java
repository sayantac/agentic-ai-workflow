package demo.ai.agentic.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

public class ChainWorkflow {

    private static final Logger log = LoggerFactory.getLogger(ChainWorkflow.class);

    /**
     * Array of system prompts that define the transformation steps in the chain.
     * Each prompt acts as a gate that validates and transforms the output before
     * proceeding to the next step.
     */
    private static final String[] DEFAULT_SYSTEM_PROMPTS = {
            // Step 1
            """
			Extract only the numerical values and their associated metrics from the text.
			Format each as 'value: metric' on a new line.
			Example format:
			92: customer satisfaction
			45%: revenue growth""",
            // Step 2
            """
			Convert all numerical values to percentages where possible.
			If not a percentage or points, convert to decimal (e.g., 92 points -> 92%).
			Keep one number per line.
			Example format:
			92%: customer satisfaction
			45%: revenue growth""",
            // Step 3
            """
			Sort all lines in descending order by numerical value.
			Keep the format 'value: metric' on each line.
			Example:
			92%: customer satisfaction
			87%: employee satisfaction""",
            // Step 4
            """
			Format the sorted data as a markdown table with columns:
			| Metric | Value |
			|:--|--:|
			| Customer Satisfaction | 92% |
			"""};

    private final ChatClient chatClient;
    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    private ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    /**
     * Executes the prompt chaining workflow by processing the input text through
     * a series of LLM calls, where each call's output becomes the input for the
     * next step.
     *
     * <p>
     * The method prints the intermediate results after each step to show the
     * progression of transformations through the chain.
     *
     * @param userInput the input text containing numerical data to be processed
     * @return the final output after all steps have been executed
     */
    public String chain(String userInput) {

        int step = 0;
        String response = userInput;
        log.info("\nSTEP {}:\n {}", step++, response);

        for (String prompt : systemPrompts) {

            // 1. Compose the input using the response from the previous step.
            String input = String.format("{%s}\n {%s}", prompt, response);

            // 2. Call the chat client with the new input and get the new response.
            response = chatClient.prompt(input).call().content();

            log.info("\nSTEP {}:\n {}", step++, response);
        }

        return response;
    }
}
