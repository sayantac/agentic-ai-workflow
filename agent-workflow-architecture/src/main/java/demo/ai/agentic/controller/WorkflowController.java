package demo.ai.agentic.controller;

import demo.ai.agentic.constants.RouteConstant;
import demo.ai.agentic.record.FinalResponse;
import demo.ai.agentic.record.RefinedResponse;
import demo.ai.agentic.workflow.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WorkflowController {

    private final ChatClient chatClient;

    WorkflowController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/workflow/chain")
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
    String routingWorkflow(@PathVariable("incidentId") String incidentId) {

        var routerWorkflow = new RoutingWorkflow(this.chatClient);
        var ticket = RouteConstant.tickets.get(incidentId);

        return ticket == null ? "Ticket not found for id: " + incidentId :
                routerWorkflow.route(ticket, RouteConstant.supportRoutes);
    }

    @GetMapping("/workflow/parallel")
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
    FinalResponse orchestratorWorkflow() {

        return new OrchestratorWorkflow(this.chatClient)
                .process("Write a product description for " +
                        "a new eco-friendly water bottle");
    }

    @GetMapping("/workflow/evaluate/optimize")
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
