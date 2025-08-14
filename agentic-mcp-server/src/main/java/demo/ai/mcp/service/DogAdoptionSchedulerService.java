package demo.ai.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DogAdoptionSchedulerService {

    private final ObjectMapper objectMapper;

    public DogAdoptionSchedulerService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Tool(description = "schedule an appointment to adopt a dog at the " +
            "Pooch Palace dog adoption agency")
    public String scheduleDogAdoptionAppointment(@ToolParam(description = "the id of the dog") int dogId,
                                         @ToolParam(description = "the name of the dog") String dogName) throws Exception {

        System.out.println("confirming appointment for [" + dogId + "] and [" + dogName + "]");
        var instant = Instant.now().plus(3, ChronoUnit.DAYS);
        return objectMapper.writeValueAsString(instant);
    }
} 