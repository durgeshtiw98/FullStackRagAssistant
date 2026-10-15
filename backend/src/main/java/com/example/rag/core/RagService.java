package com.example.rag.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/** Ingestion and question answering. Plain Java so it can be tested without Spring. */
public final class RagService {

    private static final System.Logger LOG = System.getLogger(RagService.class.getName());

    public record Settings(int topK, int embedBatchSize, double minScore) {
    }

    public record SourceView(int n, String chunkId, String docTitle, String url, String text, double score,
                             boolean cited) {
    }

    public record Answer(String answer, String mode, List<SourceView> sources, String note) {
    }

    public record IngestResult(int documents, int chunks, int dimension, long millis) {
    }

    private final EmbeddingClient embeddings;
    private final AnswerGenerator generator;
    private final Chunker chunker;
    private final IndexStore store;
    private final Path indexPath;
    private final Settings settings;
    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final ExtractiveAnswerer extractive = new ExtractiveAnswerer();
    private final AtomicReference<VectorIndex> index = new AtomicReference<>(new VectorIndex());

    public RagService(EmbeddingClient embeddings, AnswerGenerator generator, Chunker chunker, IndexStore store,
                      Path indexPath, Settings settings) {
        this.embeddings = embeddings;
        this.generator = generator;
        this.chunker = chunker;
        this.store = store;
        this.indexPath = indexPath;
        this.settings = settings;
    }

    /** Loads a previously saved index if one exists. */
    public boolean loadPersisted() throws IOException {
        if (indexPath == null || !Files.exists(indexPath)) {
            return false;
        }
        index.set(store.load(indexPath));
        return true;
    }

    public IngestResult ingest(List<CorpusDocument> docs) throws IOException {
        long start = System.nanoTime();
        List<Chunk> chunks = new java.util.ArrayList<>();
        for (CorpusDocument doc : docs) {
            chunks.addAll(chunker.chunk(doc));
        }
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No text found to index.");
        }
        VectorIndex fresh = new VectorIndex();
        int batchSize = Math.max(1, settings.embedBatchSize());
        for (int from = 0; from < chunks.size(); from += batchSize) {
            List<Chunk> batch = chunks.subList(from, Math.min(chunks.size(), from + batchSize));
            List<String> texts = batch.stream().map(Chunk::embeddingText).toList();
            List<float[]> vectors = embeddings.embed(texts);
            if (vectors.size() != batch.size()) {
                throw new IllegalStateException("Embedding service returned " + vectors.size()
                        + " vectors for " + batch.size() + " texts.");
            }
            for (int i = 0; i < batch.size(); i++) {
                fresh.add(batch.get(i), vectors.get(i));
            }
            LOG.log(System.Logger.Level.INFO, "Embedded {0} of {1} chunks", Math.min(chunks.size(), from + batchSize),
                    chunks.size());
        }
        if (indexPath != null) {
            store.save(indexPath, fresh);
        }
        index.set(fresh);
        long millis = (System.nanoTime() - start) / 1_000_000;
        return new IngestResult(docs.size(), chunks.size(), fresh.dimension(), millis);
    }

    public Answer ask(String question, Integer requestedTopK) throws IOException {
        VectorIndex current = index.get();
        if (current.size() == 0) {
            throw new IllegalStateException("The index is empty. Ingest the corpus first.");
        }
        int k = Math.max(1, Math.min(20, requestedTopK == null ? settings.topK() : requestedTopK));
        List<float[]> queryVectors = embeddings.embed(List.of(question));
        List<ScoredChunk> top = current.search(queryVectors.get(0), k);

        if (top.isEmpty() || top.get(0).score() < settings.minScore()) {
            return new Answer(PromptBuilder.NOT_FOUND, "none", views(top, Set.of()),
                    "The best matching passage scored below the minimum similarity of " + settings.minScore() + ".");
        }

        String note = null;
        if (generator.enabled()) {
            try {
                String text = generator.generate(promptBuilder.build(question, top)).trim();
                if (text.isEmpty()) {
                    throw new IOException("The answer model returned an empty answer.");
                }
                return new Answer(text, "llm", views(top, CitationParser.citedNumbers(text, top.size())), null);
            } catch (IOException | RuntimeException e) {
                LOG.log(System.Logger.Level.WARNING, "Answer model failed: {0}", e.getMessage());
                note = "The answer model failed, so the best matching passage is shown instead: " + e.getMessage();
            }
        }
        ExtractiveAnswerer.Result result = extractive.answer(question, top);
        return new Answer(result.text(), "extractive", views(top, Set.of(result.sourceNumber())), note);
    }

    public int indexedChunks() {
        return index.get().size();
    }

    public Map<String, Integer> chunksPerDocument() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Chunk c : index.get().chunks()) {
            counts.merge(c.docTitle(), 1, Integer::sum);
        }
        return counts;
    }

    public boolean answerModelEnabled() {
        return generator.enabled();
    }

    private static List<SourceView> views(List<ScoredChunk> top, Set<Integer> cited) {
        List<SourceView> out = new java.util.ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            Chunk c = top.get(i).chunk();
            out.add(new SourceView(i + 1, c.id(), c.docTitle(), c.url(), c.text(), top.get(i).score(),
                    cited.contains(i + 1)));
        }
        return out;
    }
}
