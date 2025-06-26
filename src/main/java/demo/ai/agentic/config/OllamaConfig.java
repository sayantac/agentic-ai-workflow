package demo.ai.agentic.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;

@Configuration
@Profile("ollama")
public class OllamaConfig {

    private static final Logger log = LoggerFactory.getLogger(OllamaConfig.class);
    private final VectorStore vectorStore;

    public OllamaConfig(VectorStore vectorStore) {
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
    void ingestPDF() throws MalformedURLException {
        var fileURL = "https://www.kyndryl.com/content/dam/kyndrylprogram/doc/en/2024/technology-impact-sustainability.pdf";
        Resource pdfResource = new FileUrlResource(URI.create(fileURL).toURL());

        // Spring AI utility class to read a PDF file page by page
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        // Transform
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        log.info("Parsing document, splitting, creating embeddings, and storing in vector store...");

        // Tag as external knowledge in the vector store's metadata
        List<Document> splitDocuments = tokenTextSplitter.split(pdfReader.read());
        for (Document splitDocument: splitDocuments) { // footnotes
            splitDocument.getMetadata().put("filename", pdfResource.getFilename());
            splitDocument.getMetadata().put("version", 1);
        }

        // Sending batch of documents to vector store
        vectorStore.write(splitDocuments);

        log.info("Done parsing document, splitting, creating embeddings and storing in vector store.");
    }
}
