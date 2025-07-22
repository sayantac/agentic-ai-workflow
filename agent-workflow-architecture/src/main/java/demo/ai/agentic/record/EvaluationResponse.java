package demo.ai.agentic.record;

public record EvaluationResponse(Evaluation evaluation, String feedback) {

    public enum Evaluation {
        PASS, NEEDS_IMPROVEMENT, FAIL
    }
}
