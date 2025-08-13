package demo.ai.agentic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Tag(name = "Pooch Palace", description = "Dog adoption assistant endpoints powered by AWS Bedrock")
public class AdoptionsAssistantController {

    private final ChatClient singularity;
    private final Map<String, PromptChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    AdoptionsAssistantController(ChatClient singularity, VectorStore vectorStore) {
        this.singularity = singularity;
        this.questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
    }

    @GetMapping("/{user}/adoption/enquiry")
    @Operation(summary = "Ask adoption assistant", description = "Conversational query endpoint with memory and RAG over adoption knowledge base.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response generated successfully")
    })
    String inquire(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User question", example = "Do you have any neurotic dogs for adoption?")
            @RequestParam String question) {

        var advisor = this.advisorMap.computeIfAbsent(user,
                x -> PromptChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().build()).build());

        return this.singularity
                .prompt()
                .user(question)
                .advisors(advisor, this.questionAnswerAdvisor)
                .call()
                .content();
    }
} 