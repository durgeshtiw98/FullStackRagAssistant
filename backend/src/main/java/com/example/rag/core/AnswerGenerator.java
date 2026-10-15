package com.example.rag.core;

import java.io.IOException;

/** Writes an answer from a prompt. When {@link #enabled()} is false the service falls back to extraction. */
public interface AnswerGenerator {

    boolean enabled();

    String generate(Prompt prompt) throws IOException;
}
