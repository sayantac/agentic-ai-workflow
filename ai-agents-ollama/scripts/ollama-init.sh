#!/bin/bash

set -e
set -o pipefail

# Function to log messages
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Start Ollama server in background
log "Starting Ollama server..."
ollama serve &

OLLAMA_PID=$!

# Trap to clean up on container shutdown
cleanup() {
    log "Stopping Ollama server..."
    kill "$OLLAMA_PID" 2>/dev/null || true
}
trap cleanup EXIT

# Wait for Ollama server to be responsive
log "Waiting for Ollama server to be ready..."
for _ in {1..20}; do
    if bash -c '</dev/tcp/localhost/11434' &>/dev/null; then
        log "Ollama server is ready."
        break
    fi
    sleep 1
done

# Final check
if ! bash -c '</dev/tcp/localhost/11434' &>/dev/null; then
    log "ERROR: Ollama server did not become ready in time."
    exit 1
fi

# Pull models if missing
pull_model_if_missing() {
    MODEL=$1
    if ollama list | grep -q "$MODEL"; then
        log "Model '$MODEL' already present. Skipping pull."
    else
        log "Pulling model '$MODEL'..."
        ollama pull "$MODEL"
    fi
}

pull_model_if_missing "mxbai-embed-large:335m"
pull_model_if_missing "llama3.2:3b"

# Pre-warm the model
log "Pre-warming model..."
ollama run llama3.2:3b "Hello" || true
log "Model pre-warming triggered."

# Keep container alive by waiting on Ollama process
log "Models ready. Container will now keep running."
wait "$OLLAMA_PID"
