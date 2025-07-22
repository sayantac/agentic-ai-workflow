package demo.ai.agentic.record;

import java.util.List;

public record RefinedResponse(String solution, List<Generation> chainOfThought) {
}
