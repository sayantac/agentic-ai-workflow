package demo.ai.agentic.workflow;

import demo.ai.agentic.record.FinalResponse;
import demo.ai.agentic.record.OrchestratorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.List;

public class OrchestratorWorkflow {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorWorkflow.class);

    private final ChatClient chatClient;
    private final String orchestratorPrompt;
    private final String workerPrompt;

    public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
            Analyze this task and break it down into 2-3 distinct approaches:
            
            Task: {task}
            
            Return your response in this JSON format:
            \\{
            "analysis": "Explain your understanding of the task and which variations would be valuable.
                         Focus on how each approach serves different aspects of the task.",
            "tasks": [
            	\\{
            	"type": "formal",
            	"description": "Write a precise, technical version that emphasizes specifications"
            	\\},
            	\\{
            	"type": "conversational",
            	"description": "Write an engaging, friendly version that connects with readers"
            	\\}
            ]
            \\}
            """;

    public static final String DEFAULT_WORKER_PROMPT = """
            Generate content based on:
            Task: {original_task}
            Style: {task_type}
            Guidelines: {task_description}
            """;

    public OrchestratorWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_ORCHESTRATOR_PROMPT, DEFAULT_WORKER_PROMPT);
    }

    public OrchestratorWorkflow(ChatClient chatClient, String orchestratorPrompt, String workerPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(orchestratorPrompt, "Orchestrator prompt must not be empty");
        Assert.hasText(workerPrompt, "Worker prompt must not be empty");

        this.chatClient = chatClient;
        this.orchestratorPrompt = orchestratorPrompt;
        this.workerPrompt = workerPrompt;
    }

    public FinalResponse process(String taskDescription) {
        Assert.hasText(taskDescription, "Task description must not be empty");

        // Step 1: Get orchestrator response
        OrchestratorResponse orchestratorResponse = this.chatClient.prompt()
                .user(u -> u.text(this.orchestratorPrompt)
                        .param("task", taskDescription))
                .call()
                .entity(OrchestratorResponse.class);

        assert orchestratorResponse != null;
        log.info("=== ORCHESTRATOR OUTPUT ===");
        log.info("ANALYSIS: {}", orchestratorResponse.analysis());
        log.info("TASKS: {}", orchestratorResponse.tasks());

        // Step 2: Process each task
        List<String> workerResponses = orchestratorResponse.tasks().stream().map(task -> this.chatClient.prompt()
                .user(u -> u.text(this.workerPrompt)
                        .param("original_task", taskDescription)
                        .param("task_type", task.type())
                        .param("task_description", task.description()))
                .call()
                .content()).toList();

        log.info("=== WORKER OUTPUT ===");
        log.info("{}", workerResponses);

        return new FinalResponse(orchestratorResponse.analysis(), workerResponses);
    }
}
