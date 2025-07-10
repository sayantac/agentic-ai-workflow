package demo.ai.agentic.controller;

import demo.ai.agentic.record.WineDetails;
import demo.ai.agentic.tools.WineTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class WineController {

    private final ChatClient chatClient;
    private final Map<String, MessageChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();
    private final VectorStore vectorStore;

    WineController(ChatClient chatClient, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    @GetMapping("/{user}/ai/chat")
    String inquire(@PathVariable("user") String user,
                   @RequestParam(value = "message",
                   defaultValue = "Hello LLM") String userInput) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt("provide succinct answers")
                .user(userInput)
                .advisors(chatMemoryAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/chroma")
    public List<Document> query(@RequestParam(value = "message",
            defaultValue = "tasty wine") String message) {
        return this.vectorStore
                .similaritySearch(
                        SearchRequest.builder()
                                .query(message)
                                .topK(3)
                                .build()
                );
    }

    @GetMapping("/ai/chroma/meta")
    public List<Document> queryWithMeta(
            @RequestParam(value = "search_query", defaultValue = "tasty wine") String message,
            @RequestParam(value = "meta_query", defaultValue = "Grizzly Peak") String meta_query
    ) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(message)
                        .topK(3)
                        .filterExpression("title == '"+ meta_query+"'")
                        .build()
        );
    }

    @GetMapping("/{user}/ai/tool/call")
    String generation(@PathVariable("user") String user,
                      @RequestParam(value = "message",
                              defaultValue = "What wine do you suggest me?") String userInput) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt()
                .user(userInput)
                .advisors(chatMemoryAdvisor)
                .tools(new WineTool(this.vectorStore)) // Access to vector db
                .call()
                .content();
    }

    @GetMapping("/{user}/ai/structure")
    WineDetails structure(@PathVariable("user") String user,
                      @RequestParam(value = "message",
                              defaultValue = "What wine do you suggest me?") String userInput) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt("Return only the title and description")
                .user(userInput)
                .advisors(chatMemoryAdvisor)
                .tools(new WineTool(this.vectorStore))
                .call()
                .entity(WineDetails.class); // specify the structure expected
    }

    @GetMapping("/{user}/ai/rag")
    String rag(@PathVariable("user") String user,
                      @RequestParam(value = "message",
                              defaultValue = "What is wine?") String userInput) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        var questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(this.vectorStore).build();

        return this.chatClient
                .prompt("Please use advisors for answering wine related queries")
                .user(userInput)
                .advisors(chatMemoryAdvisor, questionAnswerAdvisor) // RAG goes here
                .call()
                .content();
    }

    @GetMapping("/{user}/ai/guardrail")
    String guardrail(@PathVariable("user") String user,
               @RequestParam(value = "message",
                       defaultValue = "What is wine?") String userInput) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        var questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(this.vectorStore).build();

        var safeguardAdvisor = SafeGuardAdvisor.builder()
                .sensitiveWords(List.of("wine")).build();

        return this.chatClient
                .prompt("Please use advisors for answering wine related queries")
                .user(userInput)
                .advisors(safeguardAdvisor) // guard the content
                .advisors(chatMemoryAdvisor, questionAnswerAdvisor)
                .call()
                .content();
    }
}
