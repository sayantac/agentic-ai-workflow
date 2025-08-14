### agentic-mcp-server

A lightweight Model Context Protocol (MCP) server built with Spring Boot and Spring AI. It exposes two MCP tools that agents can call over SSE:

- **Dog Adoption Scheduling**: schedule an appointment to adopt a dog at the Pooch Palace dog adoption agency.
- **Weather**: get the current weather for a given city.

These tools are auto-registered via Spring AI's method-based tool discovery and made available to MCP clients.

### What's inside

- **Tool registration**: `demo.ai.mcp.config.ToolConfig` wires up Spring AI's `MethodToolCallbackProvider` to expose tool methods from beans.
- **Dog adoption tool**: `demo.ai.mcp.service.DogAdoptionSchedulerService#scheduleDogAdoptionAppointment(int dogId, String dogName)` returns an ISO-8601 timestamp ~3 days in the future as the scheduled appointment time.
- **Weather tool**: `demo.ai.mcp.service.WeatherService#getCurrentWeather(String city)` returns a `demo.ai.mcp.record.WeatherResponse` with `city`, `description`, `temperature`, `humidity`, and `windSpeed`.
- **Server**: Standard Spring Boot app (`McpServerApplication`) with Actuator enabled.
- **Port**: Defaults to `8081` (`src/main/resources/application.yml`).

### Requirements

- **JDK**: 21+
- **Maven**: 3.9+
- **Docker** (optional): for containerized runs

### Run locally (Maven)

- From this module directory:

```bash
mvn spring-boot:run
```

- Or build and run the jar:

```bash
mvn clean package -DskipTests
java -jar target/agentic-mcp-server-0.0.1-SNAPSHOT.jar
```

- Override port if needed:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"
```

### Run with Docker

- Build image and run:

```bash
docker build -t mcp-server:latest .
docker run --name weather-mcp-server -p 8081:8081 mcp-server:latest
```

- Health check (Actuator):

```bash
curl http://localhost:8081/actuator/health
```

### Using from an MCP client

Agents can discover and call these tools via SSE using Spring AI's MCP client utilities. Example (Java):

```java
import io.modelcontextprotocol.client.transport.impl.server.SseServerParameters;
import org.springframework.ai.tool.McpToolset;
import com.fasterxml.jackson.databind.ObjectMapper;

ObjectMapper objectMapper = new ObjectMapper();
SseServerParameters params = SseServerParameters.builder()
        .url("http://localhost:8081")
        .build();

var toolsAndToolset = McpToolset.fromServer(params, objectMapper).get();
var tools = toolsAndToolset.getTools(); // Contains the dog adoption scheduler and weather tools
```

This module is already integrated in other modules of the workspace. For example:

- **ai-agents-google-adk**: connects to the MCP server and augments a travel assistant with weather + search.
- **ai-agents-bedrock**: demonstrates MCP-backed dog adoption appointment scheduling.

If using Docker Compose in those modules, ensure the MCP server service is healthy before dependent services start (Compose files already define an Actuator health check on port 8081).

### Endpoints and observability

- **Actuator health**: `GET /actuator/health`
- **Logs**: Standard Spring Boot logs. Enable more verbose logging via properties if needed.

### Source map

- `src/main/java/demo/ai/mcp/config/ToolConfig.java`: registers tool beans
- `src/main/java/demo/ai/mcp/service/DogAdoptionSchedulerService.java`: dog adoption scheduler tool
- `src/main/java/demo/ai/mcp/service/WeatherService.java`: weather tool
- `src/main/java/demo/ai/mcp/record/WeatherResponse.java`: response record for weather tool
- `src/main/resources/application.yml`: base config (port 8081)
- `Dockerfile`: multi-stage build with health check

### Notes

- Tools are exported based on Spring AI `@Tool`-annotated methods on Spring beans. Any additional tools can be added by creating new `@Component` classes and adding `@Tool` methods; they will be auto-discovered by the configured `MethodToolCallbackProvider`.
- Return types should be serializable; prefer simple POJOs/records for structured outputs.
