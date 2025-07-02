package demo.ai.agentic.controller;

import demo.ai.agentic.tools.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class DateTimeController {

    private final ChatClient chatClient;
    private final Map<String, MessageChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();

    DateTimeController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/{user}/clock")
    String inquire(@PathVariable("user") String user,
                   @RequestParam String question) {

        var advisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder().build()).build());

        return this.chatClient
                .prompt()
                .user(question)
                .advisors(advisor)
                .tools(new DateTimeTools())
                .call()
                .content();
    }
}
