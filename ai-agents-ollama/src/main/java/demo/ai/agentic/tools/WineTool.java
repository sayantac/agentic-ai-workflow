package demo.ai.agentic.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class WineTool {

    private final VectorStore vectorStore;

    public WineTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(name = "WineQuery",
            description = "Get the wine related details.Takes query string as input.")
    public List<Document> wineQuery(
            @ToolParam(description = "wine related query string") String query) {
        return this.vectorStore
                .similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(3)
                                .build()
                );
    }
}
