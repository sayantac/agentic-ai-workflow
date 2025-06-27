package demo.ai.agentic.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
public class DocumentSearchController {

    private final ChatClient chatClient;
    private final Map<String, MessageChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    DocumentSearchController(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
    }

    @GetMapping("/{user}/doc/search")
    String inquire(@PathVariable("user") String user,
                   @RequestParam String question) {

        var advisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().build()).build());

        return this.chatClient
                .prompt()
                .user(question)
                .advisors(advisor, this.questionAnswerAdvisor)
                .call()
                .content();
    }
}
