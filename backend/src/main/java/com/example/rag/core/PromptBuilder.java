package com.example.rag.core;

import java.util.List;

/** Builds the grounded question answering prompt. */
public final class PromptBuilder {

    public static final String NOT_FOUND = "I could not find the answer in the provided documents.";

    private static final String SYSTEM = "You answer questions using only the numbered sources provided. "
            + "Cite the source number in square brackets after every claim, for example [1] or [2]. "
            + "If the sources do not contain the answer, reply exactly: " + NOT_FOUND + " "
            + "Answer in one to three sentences and do not add facts that are not in the sources.";

    public Prompt build(String question, List<ScoredChunk> sources) {
        StringBuilder user = new StringBuilder("Sources:\n");
        for (int i = 0; i < sources.size(); i++) {
            Chunk c = sources.get(i).chunk();
            user.append('[').append(i + 1).append("] (").append(c.docTitle()).append(") ")
                    .append(c.text()).append("\n\n");
        }
        user.append("Question: ").append(question.trim());
        return new Prompt(SYSTEM, user.toString());
    }
}
