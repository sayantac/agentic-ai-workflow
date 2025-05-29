package demo.ai.agentic.config;

import demo.ai.agentic.repository.DogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ClientConfig.class);
    private final DogRepository dogRepository;
    private final VectorStore vectorStore;

    @Value("${app.vectorstore.initialize}")
    private boolean vectorStoreInitialize;

    public ClientConfig(DogRepository dogRepository, VectorStore vectorStore) {
        this.dogRepository = dogRepository;
        this.vectorStore = vectorStore;
        log.info("ClientConfig initialized with vectorStoreInitialize={}", vectorStoreInitialize);
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder,
                          SyncMcpToolCallbackProvider toolProvider) {
        var system = """
                You are an AI powered assistant to help people adopt a dog from the
                agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo,
                Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco and London.
                Information about the dogs available will be presented below.
                If there is no information, then return a polite response suggesting
                we don't have any dogs available.
                """;

        return builder
                .defaultSystem(system)
                .defaultToolCallbacks(toolProvider.getToolCallbacks())
                .build();
    }

    @PostConstruct
    void initializeVectorStore() {
        log.info("Starting vector store initialization. vectorStoreInitialize={}", vectorStoreInitialize);
        if(vectorStoreInitialize) {
            try {
                log.info("Fetching dogs from repository...");
                var dogs = dogRepository.findAll();
                log.info("Found {} dogs in repository", dogs.size());
                
                var count = new AtomicInteger();
                dogs.forEach(dog -> {
                    try {
                        var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                                dog.id(), dog.name(), dog.description()));
                        vectorStore.add(List.of(dogument));
                        count.incrementAndGet();
                        log.debug("Added dog to vector store: {}", dog.name());
                    } catch (Exception e) {
                        log.error("Error adding dog {} to vector store: {}", dog.name(), e.getMessage());
                    }
                });
                log.info("Vector store initialized with {} documents", count.get());
            } catch (Exception e) {
                log.error("Error during vector store initialization: {}", e.getMessage(), e);
            }
        } else {
            log.info("Vector store initialization skipped as vectorStoreInitialize is false");
        }
    }
}