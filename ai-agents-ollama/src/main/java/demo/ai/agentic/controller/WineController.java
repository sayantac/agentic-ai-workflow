package demo.ai.agentic.controller;

import demo.ai.agentic.record.WineDetails;
import demo.ai.agentic.tools.WineTool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Wine Connoisseur", description = "Wine assistant endpoints powered by Ollama")
public class WineController {

    private final ChatClient chatClient;
    private final Map<String, MessageChatMemoryAdvisor> advisorMap = new ConcurrentHashMap<>();
    private final VectorStore vectorStore;

    WineController(ChatClient chatClient, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    @GetMapping("/{user}/ai/chat")
    @Operation(summary = "Chat with LLM", description = "Conversational chat with memory for a given user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response generated successfully")
    })
    String chatWithLLM(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User message to the assistant", example = "What is AI agents?")
            @RequestParam String question) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt("provide succinct answers")
                .user(question)
                .advisors(chatMemoryAdvisor)
                .call()
                .content();
    }

    @GetMapping("/ai/chroma")
    @Operation(summary = "Query vector store", description = "Semantic search over wine documents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed",
                    content = @Content(schema = @Schema(implementation = Document.class)))
    })
    public List<Document> query(
            @Parameter(description = "Query text for similarity search", example = "What are some tasty wines?")
            @RequestParam String question) {
        return this.vectorStore
                .similaritySearch(
                        SearchRequest.builder()
                                .query(question)
                                .topK(3)
                                .build()
                );
    }

    @GetMapping("/ai/chroma/meta")
    @Operation(summary = "Query vector store with metadata filter", description = "Semantic search over wine documents with metadata filtering.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed",
                    content = @Content(schema = @Schema(implementation = Document.class)))
    })
    public List<Document> queryWithMeta(
            @Parameter(description = "Query text for similarity search", example = "What are some tasty wines?")
            @RequestParam String search_query,
            @Parameter(description = "Metadata filter value for title field", example = "Castoro Cellars 2006 Roussanne (Paso Robles)")
            @RequestParam String meta_filter
    ) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(search_query)
                        .topK(3)
                        .filterExpression("title == '"+ meta_filter+"'")
                        .build()
        );
    }

    @GetMapping("/{user}/ai/tool/call")
    @Operation(summary = "Call wine recommendation tool", description = "Invokes a custom tool backed by the vector store to recommend wines.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tool executed successfully")
    })
    String toolCall(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User message for the tool", example = "Suggest some sparkling wines?")
            @RequestParam String question) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt()
                .user(question)
                .advisors(chatMemoryAdvisor)
                .tools(new WineTool(this.vectorStore)) // Access to vector db
                .call()
                .content();
    }

    @GetMapping("/{user}/ai/structure")
    @Operation(summary = "Structured output", description = "Returns structured wine details for a given user query.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Structured entity returned",
                    content = @Content(schema = @Schema(implementation = WineDetails.class)))
    })
    WineDetails structure(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User message for structured output", example = "Can you suggest me three tropical wines?")
            @RequestParam String question) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        return this.chatClient
                .prompt("Return only the title and description")
                .user(question)
                .advisors(chatMemoryAdvisor)
                .tools(new WineTool(this.vectorStore))
                .call()
                .entity(WineDetails.class); // specify the structure expected
    }

    @GetMapping("/{user}/ai/rag")
    @Operation(summary = "RAG-based chat", description = "Combines chat with semantic retrieval over wine documents.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response generated successfully")
    })
    String rag(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User message to the assistant", example = "Can you suggest me three aromatic wines?")
            @RequestParam String question) {

        var chatMemoryAdvisor = this.advisorMap.computeIfAbsent(user,
                x -> MessageChatMemoryAdvisor
                        .builder(MessageWindowChatMemory.builder()
                                .maxMessages(5)
                                .build()).build());

        var questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(this.vectorStore).build();

        return this.chatClient
                .prompt("Please use advisors for answering wine related queries")
                .user(question)
                .advisors(chatMemoryAdvisor, questionAnswerAdvisor) // RAG goes here
                .call()
                .content();
    }

    @GetMapping("/{user}/ai/guardrail")
    @Operation(summary = "Guardrailed chat", description = "Chat endpoint with safety filters applied.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Response generated successfully")
    })
    String guardrail(
            @Parameter(description = "Username or session identifier")
            @PathVariable("user") String user,
            @Parameter(description = "User message to the assistant", example = "Can you suggest me three aromatic wines?")
            @RequestParam String question) {

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
                .user(question)
                .advisors(safeguardAdvisor) // guard the content
                .advisors(chatMemoryAdvisor, questionAnswerAdvisor)
                .call()
                .content();
    }
}
