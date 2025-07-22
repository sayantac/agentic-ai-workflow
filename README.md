# Agentic AI Workflow – Monorepo Guide

This repository contains multiple independent AI-powered Spring Boot applications, each demonstrating a unique agentic workflow or integration. Each module is self-contained and can be run individually using Docker Compose. This guide provides a detailed overview of each module, their functionality, environment configuration, and API documentation.

---

## Table of Contents

1. [Repository Structure](#repository-structure)
2. [Common Setup Requirements](#common-setup-requirements)
3. [Module Overviews & API Docs](#module-overviews--api-docs)
    - [agent-workflow-architecture](#agent-workflow-architecture)
    - [ai-agents-bedrock](#ai-agents-bedrock)
    - [ai-agents-google-adk](#ai-agents-google-adk)
    - [ai-agents-ollama](#ai-agents-ollama)
4. [Environment Variables (.env)](#environment-variables-env)
5. [Troubleshooting](#troubleshooting)
6. [Contributing](#contributing)
7. [License](#license)

---

## Repository Structure

```
agentic-ai-workflow/
│
├── agent-workflow-architecture/   # Agentic workflow design patterns
├── ai-agents-bedrock/             # AWS Bedrock-powered dog adoption assistant
├── ai-agents-google-adk/          # Google ADK-powered travel assistant
├── ai-agents-ollama/              # Ollama LLM-powered wine assistant
├── Dockerfile
├── README.md
└── ...
```

---

## Common Setup Requirements

- **Docker Desktop** (latest)
- **Docker Compose**
- **Java 21** (for local builds)
- **Maven** (for local builds)
- **Git**
- **.env file**: Each module requires a `.env` file in its root directory for environment-specific configuration.

---

## Module Overviews & API Docs

### 1. agent-workflow-architecture

**Purpose:**  
Demonstrates various agentic workflow patterns (chain, routing, parallelization, orchestration, evaluation/optimization) using Spring Boot. This is a reference implementation for building complex AI agent workflows.

**Key Features:**
- Multiple workflow patterns implemented as Java classes
- REST API for workflow orchestration
- Configurable via environment variables

**API Documentation:**

| Endpoint                                 | Method | Description                                                                                  |
|-------------------------------------------|--------|----------------------------------------------------------------------------------------------|
| `/workflow/chain`                        | GET    | Runs the chain workflow on a sample report and returns the result.                           |
| `/workflow/route/{incidentId}`           | GET    | Runs the routing workflow for a given incident ID.                                           |
| `/workflow/parallel`                     | GET    | Runs the parallelization workflow for multiple stakeholder groups and returns the results.    |
| `/workflow/orchestrate`                  | GET    | Runs the orchestrator workflow to generate a product description.                            |
| `/workflow/evaluate/optimize`            | GET    | Runs the evaluator/optimizer workflow on a sample coding task and returns the refined result.|

---

### 2. ai-agents-bedrock

**Purpose:**  
An AI-powered dog adoption assistant leveraging AWS Bedrock for LLM and PGVector for vector search. Helps users find adoptable dogs using natural language queries.

**Key Features:**
- Conversational AI for dog adoption
- Vector store (PostgreSQL + pgvector) for semantic search
- Integration with AWS Bedrock for embeddings and LLM
- REST API for user queries
- **Appointment Scheduling:** Demonstrates scheduling appointments for dog adoption through integration with an MCP server, enabling end-to-end adoption workflows.

**API Documentation:**

| Endpoint                                 | Method | Description                                                                                  |
|-------------------------------------------|--------|----------------------------------------------------------------------------------------------|
| `/{user}/adoption/enquiry?question=...`  | GET    | Asks a question about dog adoption for a specific user. Uses LLM and vector search.          |

---

### 3. ai-agents-google-adk

**Purpose:**  
A travel assistant agent using Google ADK (Agent Development Kit). Provides travel recommendations and planning via conversational interface.

**Key Features:**
- Google ADK integration for LLM
- REST API for travel queries
- Modular agent configuration
- **Weather and Search Integration:** Integrates a weather MCP server and a Google Search agent to help plan a day in a city, combining real-time weather data and search results for comprehensive travel planning.

**API Documentation:**

| Endpoint                                 | Method | Description                                                                                  |
|-------------------------------------------|--------|----------------------------------------------------------------------------------------------|
| `/{user}/travel/plan?question=...`       | GET    | Asks a travel planning question for a specific user. Integrates weather and search agents.   |

---

### 4. ai-agents-ollama

**Purpose:**  
A wine recommendation and information assistant powered by Ollama LLM and ChromaDB for vector search. Demonstrates advanced agent builder features for domain-specific queries.

**Key Features:**
- Ollama LLM integration
- Wine data loaded from CSV
- REST API for wine queries and date/time tools
- **Agent Builder Demonstration:** Showcases key features such as:
  - **Tools:** Custom tools for enhanced agent capabilities
  - **Chat:** Conversational interface
  - **RAG (Retrieval-Augmented Generation):** Combines LLM with vector search for context-aware answers
  - **Vector Search:** Semantic search over wine data
  - **Guardrails:** Implements safety and control mechanisms for agent responses

**API Documentation:**

| Endpoint                                 | Method | Description                                                                                  |
|-------------------------------------------|--------|----------------------------------------------------------------------------------------------|
| `/{user}/ai/chat?message=...`            | GET    | Conversational chat with the LLM for a specific user.                                        |
| `/ai/chroma?message=...`                 | GET    | Vector search for wines based on a query.                                                    |
| `/ai/chroma/meta?search_query=...&meta_query=...` | GET | Vector search for wines with metadata filtering.                                             |
| `/{user}/ai/tool/call?message=...`       | GET    | Calls a custom wine tool for recommendations.                                                |
| `/{user}/ai/structure?message=...`       | GET    | Returns structured wine details for a query.                                                 |
| `/{user}/ai/rag?message=...`             | GET    | Retrieval-Augmented Generation: combines LLM and vector search for answers.                  |
| `/{user}/ai/guardrail?message=...`       | GET    | Chat with guardrails (safety filters) enabled.                                               |

---

## Environment Variables (.env)

Each module expects a `.env` file in its root directory. This file is used by Docker Compose to inject environment variables into the containers.

**Example `.env` for ai-agents-bedrock:**
```
AWS_REGION=us-east-1
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_SESSION_TOKEN=your-session-token
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/adoptions
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
VECTORSTORE_INITIALIZE=true
```

**Example `.env` for ai-agents-google-adk:**
```
GOOGLE_API_KEY=your-google-api-key
SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

**Example `.env` for ai-agents-ollama:**
```
OLLAMA_API_URL=http://ollama:11434
SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

**How to create:**
1. Copy the example above to a file named `.env` in the module directory.
2. Fill in your credentials and configuration.

---

## Troubleshooting

- **Build Issues:**  
  Use `docker compose down -v` to remove old containers/volumes, then rebuild.

- **Environment Variables Not Loaded:**  
  Ensure `.env` exists and is in the correct directory.

- **Database Connection Errors:**  
  Check DB credentials and that the DB service is running.

- **AWS/Google/Ollama API Errors:**  
  Verify your API keys and network access.

---