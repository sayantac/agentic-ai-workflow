package demo.ai.agentic.controller;

import demo.ai.agentic.constants.RouteConstant;
import demo.ai.agentic.record.FinalResponse;
import demo.ai.agentic.record.RefinedResponse;
import demo.ai.agentic.workflow.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Workflow", description = "Agentic workflow example endpoints")
public class WorkflowController {

    private final ChatClient chatClient;

    WorkflowController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/workflow/chain")
    @Operation(summary = "Run chain workflow", description = "Executes a simple chain workflow on a sample report and returns the result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow executed successfully")
    })
    String chainWorkflow() {

        var report = """
            Q3 Performance Summary:
            Our customer satisfaction score rose to 92 points this quarter.
            Revenue grew by 45% compared to last year.
            Market share is now at 23% in our primary market.
            Customer churn decreased to 5% from 8%.
            New user acquisition cost is $43 per user.
            Product adoption rate increased to 78%.
            Employee satisfaction is at 87 points.
            Operating margin improved to 34%.
            """;

        return new ChainWorkflow(this.chatClient).chain(report);
    }

    @GetMapping("/workflow/route/{incidentId}")
    @Operation(summary = "Run routing workflow", description = "Routes an incoming incident to the appropriate handler based on its content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Routing completed")
    })
    String routingWorkflow(
            @Parameter(description = "Incident ID to route")
            @PathVariable("incidentId") String incidentId) {

        var routerWorkflow = new RoutingWorkflow(this.chatClient);
        var ticket = RouteConstant.tickets.get(incidentId);

        return ticket == null ? "Ticket not found for id: " + incidentId :
                routerWorkflow.route(ticket, RouteConstant.supportRoutes);
    }

    @GetMapping("/workflow/parallel")
    @Operation(summary = "Run parallelization workflow", description = "Runs analysis for multiple stakeholder groups in parallel and returns results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Parallel workflow executed successfully")
    })
    List<String> parallelWorkflow() {

        return new ParallelizationWorkflow(this.chatClient)
                .parallel("""
                                    Analyze how market changes will impact this stakeholder group.
                                    Provide specific impacts and recommended actions.
                                    Format with clear sections and priorities.
                                    """,
                        List.of(
                                """
                                        Customers:
                                        - Price sensitive
                                        - Want better tech
                                        - Environmental concerns
                                        """,

                                """
                                        Employees:
                                        - Job security worries
                                        - Need new skills
                                        - Want clear direction
                                        """,

                                """
                                        Investors:
                                        - Expect growth
                                        - Want cost control
                                        - Risk concerns
                                        """,

                                """
                                        Suppliers:
                                        - Capacity constraints
                                        - Price pressures
                                        - Tech transitions
                                        """),
                        4);
    }

    @GetMapping("/workflow/orchestrate")
    @Operation(summary = "Run orchestrator workflow", description = "Orchestrates multiple steps to generate a product description.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orchestration completed")
    })
    FinalResponse orchestratorWorkflow() {

        return new OrchestratorWorkflow(this.chatClient)
                .process("Write a product description for " +
                        "a new eco-friendly water bottle");
    }

    @GetMapping("/workflow/evaluate/optimize")
    @Operation(summary = "Run evaluator/optimizer workflow", description = "Evaluates and optimizes a coding task and returns the refined result.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evaluation and optimization completed")
    })
    RefinedResponse evaluateOptimizeWorkflow() {

        return new EvaluatorOptimizerWorkflow(this.chatClient)
                .loop("""
					<user input>
					Implement a Stack in Java with:
					1. push(x)
					2. pop()
					3. getMin()
					All operations should be O(1).
					All inner fields should be private and when used should be prefixed with 'this.'.
					</user input>
					""");
    }
}
