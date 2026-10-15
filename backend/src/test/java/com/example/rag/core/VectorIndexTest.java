package com.example.rag.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class VectorIndexTest {

    private static Chunk chunk(String id) {
        return new Chunk(id, "Doc " + id, "http://example.com/" + id, 0, "text " + id);
    }

    private static VectorIndex sample() {
        VectorIndex index = new VectorIndex();
        index.add(chunk("a"), new float[] {1f, 0f, 0f});
        index.add(chunk("b"), new float[] {0f, 2f, 0f});
        index.add(chunk("c"), new float[] {1f, 1f, 0f});
        return index;
    }

    @Test
    void returnsBestMatchesFirstWithCosineScores() {
        List<ScoredChunk> result = sample().search(new float[] {3f, 0.1f, 0f}, 2);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0).chunk().id());
        assertEquals("c", result.get(1).chunk().id());
        assertTrue(result.get(0).score() > 0.99f);
        assertTrue(result.get(0).score() > result.get(1).score());
    }

    @Test
    void kLargerThanSizeReturnsEverything() {
        assertEquals(3, sample().search(new float[] {1f, 1f, 1f}, 10).size());
    }

    @Test
    void rejectsWrongDimensionAndZeroVectors() {
        VectorIndex index = sample();
        assertThrows(IllegalArgumentException.class, () -> index.add(chunk("d"), new float[] {1f, 2f}));
        assertThrows(IllegalArgumentException.class, () -> index.search(new float[] {1f, 2f}, 1));
        assertThrows(IllegalArgumentException.class, () -> new VectorIndex().add(chunk("z"), new float[] {0f, 0f}));
    }

    @Test
    void emptyIndexReturnsNothing() {
        assertEquals(0, new VectorIndex().search(new float[] {1f}, 3).size());
    }

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        VectorIndex original = sample();
        Path file = Files.createTempFile("index", ".bin");
        try {
            IndexStore store = new IndexStore();
            store.save(file, original);
            VectorIndex loaded = store.load(file);
            assertEquals(original.size(), loaded.size());
            assertEquals(original.dimension(), loaded.dimension());
            assertEquals(original.chunks(), loaded.chunks());
            List<ScoredChunk> before = original.search(new float[] {0.2f, 1f, 0f}, 3);
            List<ScoredChunk> after = loaded.search(new float[] {0.2f, 1f, 0f}, 3);
            for (int i = 0; i < before.size(); i++) {
                assertEquals(before.get(i).chunk(), after.get(i).chunk());
                assertEquals(before.get(i).score(), after.get(i).score(), 1e-6);
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadRejectsOtherFiles() throws IOException {
        Path file = Files.createTempFile("notindex", ".bin");
        try {
            Files.writeString(file, "this is not an index file");
            assertThrows(IOException.class, () -> new IndexStore().load(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
