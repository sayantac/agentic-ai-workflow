package demo.ai.agentic.controller;

import com.google.adk.agents.BaseAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class TravelAssistantController {

    private final InMemoryRunner runner;

    // In-memory store for user sessions
    private final Map<String, Session> userSessions = new ConcurrentHashMap<>();

    public TravelAssistantController(BaseAgent travelAssistantAgent) {
        this.runner = new InMemoryRunner(travelAssistantAgent);
    }

    @GetMapping("/{user}/travel/plan")
    String inquire(@PathVariable("user") String user,
                   @RequestParam String question) {

        // Get or create a session for the user
        Session session = userSessions.computeIfAbsent(user, x ->
                runner.sessionService()
                        .createSession(runner.appName(), x)
                        .blockingGet()
        );

        Content userMsg = Content.fromParts(Part.fromText(question));
        Flowable<Event> events = runner.runAsync(user, session.id(), userMsg);

        return events.blockingLast().stringifyContent();
    }
}
