#!/bin/bash

# Start Ollama server in the background
ollama serve &

# Wait for server to initialize
sleep 10

# Pull required models
ollama pull mxbai-embed-large:335m
ollama pull llama3.2:3b

# Wait for all background processes
wait