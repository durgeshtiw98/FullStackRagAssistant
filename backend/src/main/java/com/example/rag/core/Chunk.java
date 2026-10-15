package com.example.rag.core;

/** One retrievable passage of a document. */
public record Chunk(String id, String docTitle, String url, int index, String text) {

    /** The title is prepended so a passage that never repeats the subject name still matches queries about it. */
    public String embeddingText() {
        return docTitle + ". " + text;
    }
}
