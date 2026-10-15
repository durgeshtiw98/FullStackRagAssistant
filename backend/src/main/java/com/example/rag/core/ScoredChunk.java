package com.example.rag.core;

/** A chunk with its cosine similarity to the query. */
public record ScoredChunk(Chunk chunk, float score) {
}
