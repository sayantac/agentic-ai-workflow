package demo.ai.agentic.workflow;

import demo.ai.agentic.record.EvaluationResponse;
import demo.ai.agentic.record.Generation;
import demo.ai.agentic.record.RefinedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

public class EvaluatorOptimizerWorkflow {

    private static final Logger log = LoggerFactory.getLogger(EvaluatorOptimizerWorkflow.class);

    public static final String DEFAULT_GENERATOR_PROMPT = """
			Your goal is to complete the task based on the input. If there are feedback
			from your previous generations, you should reflect on them to improve your solution.

			CRITICAL: Your response must be a SINGLE LINE of valid JSON with NO LINE BREAKS except those explicitly escaped with \\n.
			Here is the exact format to follow, including all quotes and braces:

			{"thoughts":"Brief description here","response":"public class Example {\\n    // Code here\\n}"}

			Rules for the response field:
			1. ALL line breaks must use \\n
			2. ALL quotes must use \\"
			3. ALL backslashes must be doubled: \\
			4. NO actual line breaks or formatting - everything on one line
			5. NO tabs or special characters
			6. Java code must be complete and properly escaped

			Example of properly formatted response:
			{"thoughts":"Implementing counter","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

			Follow this format EXACTLY - your response must be valid JSON on a single line.
			""";

    public static final String DEFAULT_EVALUATOR_PROMPT = """
			Evaluate this code implementation for correctness, time complexity, and best practices.
			Ensure the code have proper javadoc documentation.
			Respond with EXACTLY this JSON format on a single line:

			{"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"Your feedback here"}

			The evaluation field must be one of: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
			Use "PASS" only if all criteria are met with no improvements needed.
			""";

    private final ChatClient chatClient;

    private final String generatorPrompt;

    private final String evaluatorPrompt;

    public EvaluatorOptimizerWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_GENERATOR_PROMPT, DEFAULT_EVALUATOR_PROMPT);
    }

    public EvaluatorOptimizerWorkflow(ChatClient chatClient, String generatorPrompt, String evaluatorPrompt) {
        Assert.notNull(chatClient, "ChatClient must not be null");
        Assert.hasText(generatorPrompt, "Generator prompt must not be empty");
        Assert.hasText(evaluatorPrompt, "Evaluator prompt must not be empty");

        this.chatClient = chatClient;
        this.generatorPrompt = generatorPrompt;
        this.evaluatorPrompt = evaluatorPrompt;
    }

    /**
     * Initiates the evaluator-optimizer workflow for a given task. This method
     * orchestrates the iterative process of generation and evaluation until a
     * satisfactory solution is reached.
     *
     * <p>
     * The workflow follows these steps:
     * </p>
     * <ol>
     * <li>Generate an initial solution</li>
     * <li>Evaluate the solution against quality criteria</li>
     * <li>If evaluation passes, return the solution</li>
     * <li>If evaluation indicates a need for improvement, incorporate feedback and
     * generate a new solution</li>
     * <li>Repeat steps 2-4 until a satisfactory solution is achieved</li>
     * </ol>
     *
     * @param task The task or problem to be solved through iterative refinement
     * @return A RefinedResponse containing the final solution and the chain of
     *         thought showing the evolution of the solution
     */
    public RefinedResponse loop(String task) {
        List<String> memory = new ArrayList<>();
        List<Generation> chainOfThought = new ArrayList<>();

        return loop(task, "", memory, chainOfThought);
    }

    /**
     * Internal recursive implementation of the evaluator-optimizer loop. This
     * method maintains the state of previous attempts and feedback while recursively
     * refining the solution until it meets the evaluation criteria.
     *
     * @param task           The original task to be solved
     * @param context        Accumulated context including previous attempts and feedback
     * @param memory         List of previous solution attempts for reference
     * @param chainOfThought List tracking the evolution of solutions and reasoning
     * @return A RefinedResponse containing the final solution and complete solution history
     */
    private RefinedResponse loop(String task, String context, List<String> memory,
                                 List<Generation> chainOfThought) {

        Generation generation = generate(task, context);
        memory.add(generation.response());
        chainOfThought.add(generation);

        EvaluationResponse evaluationResponse = evaluate(generation.response(), task);

        if (evaluationResponse.evaluation().equals(EvaluationResponse.Evaluation.PASS)) {
            // Solution is accepted!
            return new RefinedResponse(generation.response(), chainOfThought);
        }

        // Accumulated new context including the last and the previous attempts and
        // feedbacks.
        StringBuilder newContext = new StringBuilder();
        newContext.append("Previous attempts:");
        for (String m : memory) {
            newContext.append("\n- ").append(m);
        }
        newContext.append("\nFeedback: ").append(evaluationResponse.feedback());

        return loop(task, newContext.toString(), memory, chainOfThought);
    }

    /**
     * Generates or refines a solution based on the given task and feedback context.
     * This method represents the generator component of the workflow, producing
     * responses that can be iteratively improved through evaluation feedback.
     *
     * @param task    The primary task or problem to be solved
     * @param context Previous attempts and feedback for iterative improvement
     * @return A Generation containing the model's thoughts and proposed solution
     */
    private Generation generate(String task, String context) {
        Generation generationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\n{context}\nTask: {task}")
                        .param("prompt", this.generatorPrompt)
                        .param("context", context)
                        .param("task", task))
                .call()
                .entity(Generation.class);

        assert generationResponse != null;
        log.info("=== GENERATOR OUTPUT ===");
        log.info("THOUGHTS: {}", generationResponse.thoughts());
        log.info("RESPONSE:\n{}", generationResponse.response());

        return generationResponse;
    }

    /**
     * Evaluates if a solution meets the specified requirements and quality
     * criteria. This method represents the evaluator component of the workflow, analyzing
     * solutions and providing detailed feedback for further refinement until the desired
     * quality level is reached.
     *
     * @param content The solution content to be evaluated
     * @param task    The original task against which to evaluate the solution
     * @return An EvaluationResponse containing the evaluation result
     *         (PASS/NEEDS_IMPROVEMENT/FAIL) and detailed feedback for improvement
     */
    private EvaluationResponse evaluate(String content, String task) {

        EvaluationResponse evaluationResponse = chatClient.prompt()
                .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                        .param("prompt", this.evaluatorPrompt)
                        .param("task", task)
                        .param("content", content))
                .call()
                .entity(EvaluationResponse.class);

        assert evaluationResponse != null;
        log.info("=== EVALUATOR OUTPUT ===");
        log.info("EVALUATION: {}", evaluationResponse.evaluation());
        log.info("FEEDBACK: {}", evaluationResponse.feedback());

        return evaluationResponse;
    }
}
