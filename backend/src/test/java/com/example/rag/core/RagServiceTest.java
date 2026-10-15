package com.example.rag.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class RagServiceTest {

    /** Hashes words into a small vector, so texts that share words are close. No network needed. */
    static final class HashingEmbedder implements EmbeddingClient {
        int calls = 0;

        @Override
        public List<float[]> embed(List<String> texts) {
            calls++;
            List<float[]> out = new ArrayList<>();
            for (String text : texts) {
                float[] v = new float[64];
                for (String w : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
                    if (!w.isEmpty()) {
                        v[Math.floorMod(w.hashCode(), 64)] += 1f;
                    }
                }
                out.add(v);
            }
            return out;
        }
    }

    static final class FakeGenerator implements AnswerGenerator {
        final String reply;
        final boolean fail;
        Prompt lastPrompt;

        FakeGenerator(String reply, boolean fail) {
            this.reply = reply;
            this.fail = fail;
        }

        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public String generate(Prompt prompt) throws IOException {
            lastPrompt = prompt;
            if (fail) {
                throw new IOException("rate limited");
            }
            return reply;
        }
    }

    private static List<CorpusDocument> docs() {
        return List.of(
                new CorpusDocument("Apollo 11", "http://x/apollo11",
                        "Apollo 11 was the first crewed mission to land on the Moon. "
                                + "Neil Armstrong was the commander of the mission. "
                                + "Michael Collins stayed in orbit aboard the command module."),
                new CorpusDocument("Saturn V", "http://x/saturnv",
                        "The Saturn V was a super heavy lift launch vehicle. It had three stages. "
                                + "The first stage burned kerosene and liquid oxygen."));
    }

    private static RagService service(EmbeddingClient embedder, AnswerGenerator generator, Path indexPath,
                                      double minScore) {
        return new RagService(embedder, generator, new Chunker(40, 10), new IndexStore(), indexPath,
                new RagService.Settings(3, 2, minScore));
    }

    @Test
    void askBeforeIngestFails() {
        RagService s = service(new HashingEmbedder(), new DisabledAnswerGenerator(), null, 0.0);
        assertThrows(IllegalStateException.class, () -> s.ask("anything", null));
    }

    @Test
    void ingestThenAskWithModelReturnsCitedSources() throws IOException {
        FakeGenerator generator = new FakeGenerator("Neil Armstrong commanded it [1].", false);
        RagService s = service(new HashingEmbedder(), generator, null, 0.0);
        RagService.IngestResult ingested = s.ingest(docs());
        assertEquals(2, ingested.documents());
        assertTrue(ingested.chunks() >= 2);

        RagService.Answer answer = s.ask("Who was the commander of Apollo 11?", null);
        assertEquals("llm", answer.mode());
        assertEquals("Apollo 11", answer.sources().get(0).docTitle());
        assertTrue(answer.sources().get(0).cited());
        assertFalse(answer.sources().get(1).cited());
        assertTrue(generator.lastPrompt.user().contains("Question: Who was the commander of Apollo 11?"));
    }

    @Test
    void modelFailureFallsBackToExtractiveWithANote() throws IOException {
        RagService s = service(new HashingEmbedder(), new FakeGenerator(null, true), null, 0.0);
        s.ingest(docs());
        RagService.Answer answer = s.ask("Who was the commander of Apollo 11?", 3);
        assertEquals("extractive", answer.mode());
        assertNotNull(answer.note());
        assertTrue(answer.note().contains("rate limited"));
        assertTrue(answer.answer().contains("Armstrong"));
        assertTrue(answer.sources().stream().anyMatch(RagService.SourceView::cited));
    }

    @Test
    void withoutAModelTheAnswerIsExtractive() throws IOException {
        RagService s = service(new HashingEmbedder(), new DisabledAnswerGenerator(), null, 0.0);
        s.ingest(docs());
        RagService.Answer answer = s.ask("How many stages did the Saturn V have?", null);
        assertEquals("extractive", answer.mode());
        assertEquals(null, answer.note());
        assertTrue(answer.answer().contains("three stages"));
    }

    @Test
    void belowTheMinimumScoreNoModelCallIsMade() throws IOException {
        FakeGenerator generator = new FakeGenerator("should not be used", false);
        RagService s = service(new HashingEmbedder(), generator, null, 0.99);
        s.ingest(docs());
        RagService.Answer answer = s.ask("completely unrelated zebra question", null);
        assertEquals("none", answer.mode());
        assertEquals(PromptBuilder.NOT_FOUND, answer.answer());
        assertEquals(null, generator.lastPrompt);
    }

    @Test
    void ingestBatchesTheEmbeddingCalls() throws IOException {
        HashingEmbedder embedder = new HashingEmbedder();
        RagService s = service(embedder, new DisabledAnswerGenerator(), null, 0.0);
        RagService.IngestResult result = s.ingest(docs());
        assertEquals((result.chunks() + 1) / 2, embedder.calls);
    }

    @Test
    void persistedIndexIsReloaded() throws IOException {
        Path file = Files.createTempFile("rag-index", ".bin");
        try {
            RagService first = service(new HashingEmbedder(), new DisabledAnswerGenerator(), file, 0.0);
            first.ingest(docs());
            RagService second = service(new HashingEmbedder(), new DisabledAnswerGenerator(), file, 0.0);
            assertEquals(0, second.indexedChunks());
            assertTrue(second.loadPersisted());
            assertEquals(first.indexedChunks(), second.indexedChunks());
            assertEquals(first.chunksPerDocument(), second.chunksPerDocument());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void ingestingNothingFails() {
        RagService s = service(new HashingEmbedder(), new DisabledAnswerGenerator(), null, 0.0);
        assertThrows(IllegalArgumentException.class, () -> s.ingest(List.of(new CorpusDocument("Empty", null, " "))));
    }
}
