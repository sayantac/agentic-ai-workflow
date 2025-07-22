package demo.ai.agentic.config;

import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ChatClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatClientConfig.class);
    private final VectorStore vectorStore;

    @Value("${app.vectorstore.initialize}")
    private boolean vectorStoreInitialize;

    public ChatClientConfig(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultOptions(ToolCallingChatOptions.builder()
                        .temperature(0.7)
                        .internalToolExecutionEnabled(Boolean.TRUE)
                        .build())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @PostConstruct
    void ingestWineReviewsCSV() {
        log.info("Starting wine reviews ingestion. vectorStoreInitialize={}", vectorStoreInitialize);
        if (vectorStoreInitialize) {
            Path csvPath = Path.of("data/wine_reviews.csv");
            int batchSize = 100;
            List<Document> batch = new ArrayList<>();
            AtomicInteger totalCount = new AtomicInteger();
            long start = System.currentTimeMillis();

            try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
                CSVFormat format = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get();

                try (CSVParser parser = format.parse(reader)) {
                    for (CSVRecord record : parser) {
                        String description = record.get("description");
                        String title = record.get("title");

                        Document doc = new Document(description);
                        doc.getMetadata().put("title", title);
                        doc.getMetadata().put("schema_initialized", true);
                        batch.add(doc);
                        totalCount.incrementAndGet();

                        if (batch.size() >= batchSize) {
                            vectorStore.write(batch);
                            batch.clear();
                        }
                    }

                    if (!batch.isEmpty()) {
                        vectorStore.write(batch);
                    }

                    log.info("Ingested {} wine reviews in {} ms.", totalCount.get(), System.currentTimeMillis() - start);
                }

            } catch (IOException e) {
                log.error("Error reading {}", csvPath.toAbsolutePath(), e);
            }
        } else {
            log.info("Wine reviews ingestion skipped as vectorStoreInitialize={}", vectorStoreInitialize);
        }
    }
}
