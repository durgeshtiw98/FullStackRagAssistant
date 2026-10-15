package com.example.rag.core;

/** System and user messages for the answer model. */
public record Prompt(String system, String user) {
}
