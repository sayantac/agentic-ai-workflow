# Agentic AI Workflow

A Spring Boot application that demonstrates an AI-powered dog adoption assistant using Spring AI and vector store capabilities. The application helps users find and adopt dogs from Pooch Palace, with locations across multiple cities worldwide.

## Features

- AI-powered conversational interface for dog adoption assistance
- Vector store integration for efficient dog information retrieval
- PostgreSQL with pgvector extension for vector similarity search
- Docker containerization for easy deployment
- Spring AI integration with Model Context Protocol (MCP) client
- AWS Bedrock integration for AI capabilities

## Tech Stack

- Java 21
- Spring Boot 3.4.5
- Spring AI 1.0.0-M8
- PostgreSQL with pgvector extension
- Docker & Docker Compose
- Maven for build management
- AWS Bedrock for AI services

## Prerequisites

Before running the application, ensure you have the following installed:

- Docker Desktop (latest version)
- Docker Compose (included with Docker Desktop)
- Git (for cloning the repository)
- AWS credentials with Bedrock access

## Running the Application

### Using Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd agentic-ai-workflow
   ```

2. Configure AWS credentials:
   The application requires AWS credentials for Bedrock services. These are configured in the `docker-compose.yml` file:
   ```yaml
   environment:
     - AWS_REGION=us-east-1
     - AWS_ACCESS_KEY=your-access-key
     - AWS_SECRET_KEY=your-secret-key
     - AWS_SESSION_TOKEN=your-session-token
   ```

3. Build and start the application:
   ```bash
   docker compose up --build
   ```
   This command will:
   - Build the Spring Boot application with no cache
   - Start PostgreSQL with pgvector extension
   - Initialize the vector store with dog data
   - Start the MCP server
   - Start the application server

4. The application will be available at:
   - Application: http://localhost:8080
   - PostgreSQL: localhost:5432
   - MCP Server: http://localhost:8081

5. To stop the application:
   ```bash
   docker compose down
   ```

6. To remove all data and start fresh:
   ```bash
   docker compose down -v
   ```

### Important Note on Docker Compose and Profile Switching

When switching profiles (e.g., from `bedrock` to `ollama`) or making code changes, Docker Compose does not automatically rebuild the project. You must explicitly rebuild the image to ensure the correct version is used. For example:

```sh
docker compose up --build
```

If you want a clean build (removing old containers and images), run:

```sh
docker compose down -v
docker compose up --build
```

This ensures that the Maven build runs with the correct profile and the resulting JAR is up to date.

### Service Details

The application consists of three main services:

1. **PostgreSQL (postgres)**
   - Uses pgvector extension for vector storage
   - Port: 5432
   - Database: adoptions
   - Credentials: postgres/postgres

2. **MCP Server (mcp-server)**
   - Handles AI model interactions
   - Port: 8081
   - Health check endpoint: /actuator/health

3. **Agentic AI Workflow (agentic-ai-workflow)**
   - Main application service
   - Port: 8080
   - Depends on postgres and mcp-server
   - Initializes vector store with dog data

### Configuration

The application can be configured using environment variables in docker-compose.yml:

- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `VECTORSTORE_INITIALIZE`: Whether to initialize vector store on startup
- `AWS_REGION`: AWS region for Bedrock services
- `AWS_ACCESS_KEY`: AWS access key
- `AWS_SECRET_KEY`: AWS secret key
- `AWS_SESSION_TOKEN`: AWS session token

### Vector Store Initialization

The vector store is initialized with dog data from the database. This process:
1. Creates necessary tables and indexes
2. Loads initial dog data from schema.sql
3. Converts dog information into vector embeddings
4. Stores vectors for similarity search

### Testing the Application

Once the application is running, you can test it by:

1. Making a GET request to the inquiry endpoint:
   ```
   GET http://localhost:8080/{user}/inquire?question=your_question
   ```
   Replace `{user}` with any user identifier and `your_question` with your query about dogs.

2. Example questions:
   - "What dogs are good with children?"
   - "Do you have any small dogs available?"
   - "Tell me about dogs that are good for apartments"

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/demo/ai/agentic/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── repository/      # Data repositories
│   │   │   └── record/          # Data models
│   │   └── resources/           # Application properties
├── postgres/
│   └── schema.sql              # Database schema and initial data
├── Dockerfile                  # Docker build file
├── docker-compose.yml         # Docker compose configuration
└── pom.xml                    # Maven dependencies
```

## Troubleshooting

1. **Vector Store Initialization Issues**
   - Check if `VECTORSTORE_INITIALIZE` is set to true
   - Verify database connection
   - Check application logs for initialization errors

2. **AWS Bedrock Connection Issues**
   - Verify AWS credentials
   - Check AWS region configuration
   - Ensure Bedrock service is available in your region
   - When using Bedrock profile, ensure the embedding model is properly configured
   - If you see "EmbeddingModel bean not found" error, check that BedrockConfig is properly set up

3. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure schema.sql is properly loaded

## Contributing

Feel free to submit issues and enhancement requests.

## License

[Add your license information here] 