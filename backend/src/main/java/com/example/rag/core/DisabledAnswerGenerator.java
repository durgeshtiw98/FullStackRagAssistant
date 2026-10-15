package com.example.rag.core;

/** Used when no API key is configured. */
public final class DisabledAnswerGenerator implements AnswerGenerator {

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public String generate(Prompt prompt) {
        throw new UnsupportedOperationException("No answer model is configured.");
    }
}
