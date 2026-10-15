package com.example.rag.core;

import java.io.IOException;
import java.util.List;

/** Turns texts into unit-length embedding vectors. */
public interface EmbeddingClient {

    List<float[]> embed(List<String> texts) throws IOException;
}
