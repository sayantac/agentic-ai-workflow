package demo.ai.agentic.controller;

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
public class AdoptionsAssistantController {

    private final ChatClient singularity;
    private final Map<String, PromptChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    AdoptionsAssistantController(ChatClient singularity,
                                VectorStore vectorStore) {
        this.singularity = singularity;
        this.questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
    }

    @GetMapping("/{user}/inquire")
    String inquire(@PathVariable("user") String user,
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