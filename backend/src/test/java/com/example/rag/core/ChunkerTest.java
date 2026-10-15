package com.example.rag.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChunkerTest {

    private static String sentences(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            sb.append("Sentence number ").append(i).append(" is here for chunking tests. ");
        }
        return sb.toString().trim();
    }

    @Test
    void everyChunkStaysWithinTheWordLimit() {
        Chunker chunker = new Chunker(40, 10);
        List<Chunk> chunks = chunker.chunk(new CorpusDocument("Test Doc", null, sentences(60)));
        assertTrue(chunks.size() > 5);
        for (Chunk c : chunks) {
            assertTrue(TextUtil.wordCount(c.text()) <= 40, "chunk too long: " + c.text());
        }
    }

    @Test
    void consecutiveChunksOverlapAndNothingIsLost() {
        Chunker chunker = new Chunker(40, 10);
        List<Chunk> chunks = chunker.chunk(new CorpusDocument("Test Doc", null, sentences(60)));
        for (int i = 0; i + 1 < chunks.size(); i++) {
            List<String> current = TextUtil.splitSentences(chunks.get(i).text());
            String lastSentence = current.get(current.size() - 1);
            assertTrue(chunks.get(i + 1).text().startsWith(lastSentence),
                    "chunk " + (i + 1) + " should start with the last sentence of chunk " + i);
        }
        Set<Integer> seen = new HashSet<>();
        for (Chunk c : chunks) {
            for (String s : TextUtil.splitSentences(c.text())) {
                seen.add(Integer.parseInt(s.split(" ")[2]));
            }
        }
        for (int i = 1; i <= 60; i++) {
            assertTrue(seen.contains(i), "sentence " + i + " is missing from every chunk");
        }
    }

    @Test
    void aSentenceLongerThanTheLimitIsSplit() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("word").append(i).append(' ');
        }
        List<Chunk> chunks = new Chunker(30, 5).chunk(new CorpusDocument("Long", null, sb.toString().trim() + "."));
        assertEquals(4, chunks.size());
        for (Chunk c : chunks) {
            assertTrue(TextUtil.wordCount(c.text()) <= 30);
        }
    }

    @Test
    void shortAndEmptyDocuments() {
        Chunker chunker = new Chunker(40, 10);
        assertEquals(1, chunker.chunk(new CorpusDocument("Short", null, "Only one sentence here.")).size());
        assertEquals(0, chunker.chunk(new CorpusDocument("Empty", null, "  \n\n ")).size());
    }

    @Test
    void idsAreUniqueAndUseTheTitleSlug() {
        List<Chunk> chunks = new Chunker(40, 10).chunk(new CorpusDocument("Apollo 11", "http://x", sentences(30)));
        Set<String> ids = new HashSet<>();
        for (Chunk c : chunks) {
            assertTrue(ids.add(c.id()));
            assertTrue(c.id().startsWith("apollo-11#"));
            assertEquals("Apollo 11", c.docTitle());
        }
    }

    @Test
    void rejectsBadSettings() {
        assertThrows(IllegalArgumentException.class, () -> new Chunker(5, 0));
        assertThrows(IllegalArgumentException.class, () -> new Chunker(40, 40));
        assertThrows(IllegalArgumentException.class, () -> new Chunker(40, -1));
    }
}
