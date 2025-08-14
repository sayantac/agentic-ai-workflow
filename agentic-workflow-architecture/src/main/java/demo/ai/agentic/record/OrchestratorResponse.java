package demo.ai.agentic.record;

import java.util.List;

public record OrchestratorResponse(String analysis, List<Task> tasks) {
}
