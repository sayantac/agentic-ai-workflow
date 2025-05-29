#!/bin/bash

# Start Ollama server in the background
ollama serve &

# Wait for server to initialize
sleep 10

# Pull required models
ollama pull nomic-embed-text:latest
ollama pull llama3.2:1b

# Wait for all background processes
wait 