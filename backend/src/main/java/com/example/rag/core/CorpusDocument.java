package com.example.rag.core;

/** A source document before chunking. */
public record CorpusDocument(String title, String url, String text) {
}
