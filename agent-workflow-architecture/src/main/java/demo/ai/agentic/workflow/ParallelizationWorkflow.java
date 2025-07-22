package demo.ai.agentic.workflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParallelizationWorkflow {

    private final ChatClient chatClient;

    public ParallelizationWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Processes multiple inputs concurrently using a fixed thread pool and the same prompt template.
     * This method maintains the order of results corresponding to the input order.
     *
     * @param prompt   The prompt template to use for each input. The input will be appended to this prompt.
     *                 Must not be null. Example: "Translate the following text to French":
     * @param inputs   List of input strings to process. Each input will be processed independently
     *                 in parallel. Must not be null or empty. Example: ["Hello", "World", "Good morning"]
     * @param nWorkers The number of concurrent worker threads to use. This controls the maximum
     *                 number of simultaneous LLM API calls. Must be greater than 0. Consider API
     *                 rate limits when setting this value.
     * @return List of processed results in the same order as the inputs. Each result contains
     * the LLM's response for the corresponding input.
     * @throws IllegalArgumentException if prompt is null, inputs is null/empty, or nWorkers <= 0
     * @throws RuntimeException         if processing fails for any input, with the cause containing
     *                                  the specific error details
     */
    public List<String> parallel(String prompt, List<String> inputs, int nWorkers) {
        Assert.notNull(prompt, "Prompt cannot be null");
        Assert.notEmpty(inputs, "Inputs list cannot be empty");
        Assert.isTrue(nWorkers > 0, "Number of workers must be greater than 0");

        try (ExecutorService executor = Executors.newFixedThreadPool(nWorkers)) {

            List<CompletableFuture<String>> futures = inputs.stream()
                    .map(input -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return chatClient.prompt(prompt + "\nInput: " + input).call().content();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to process input: " + input, e);
                        }
                    }, executor))
                    .toList();

            // Wait for all tasks to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new));
            allFutures.join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
    }
}
