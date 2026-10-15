package com.example.rag.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Splits a document into sentence-aligned chunks of at most {@code targetWords} words.
 *
 * <p>Consecutive chunks share trailing sentences (up to {@code overlapWords} words) so a fact that sits at a
 * boundary is still whole in one chunk. The default of 80 words keeps a chunk plus its title under the
 * 128-token limit of the embedding service.
 */
public final class Chunker {

    private final int targetWords;
    private final int overlapWords;

    public Chunker(int targetWords, int overlapWords) {
        if (targetWords < 10) {
            throw new IllegalArgumentException("targetWords must be at least 10");
        }
        if (overlapWords < 0 || overlapWords >= targetWords) {
            throw new IllegalArgumentException("overlapWords must be between 0 and targetWords - 1");
        }
        this.targetWords = targetWords;
        this.overlapWords = overlapWords;
    }

    public List<Chunk> chunk(CorpusDocument doc) {
        List<String> pieces = new ArrayList<>();
        for (String paragraph : doc.text().split("\\R+")) {
            String p = TextUtil.collapseWhitespace(paragraph);
            if (p.isEmpty()) {
                continue;
            }
            for (String sentence : TextUtil.splitSentences(p)) {
                pieces.addAll(capLength(sentence));
            }
        }

        String slug = TextUtil.slug(doc.title());
        List<Chunk> chunks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int currentWords = 0;
        int freshWords = 0;
        for (String piece : pieces) {
            int w = TextUtil.wordCount(piece);
            if (currentWords + w > targetWords && !current.isEmpty()) {
                chunks.add(makeChunk(doc, slug, chunks.size(), current));
                current = tail(current, w);
                currentWords = countWords(current);
                freshWords = 0;
            }
            current.add(piece);
            currentWords += w;
            freshWords += w;
        }
        if (freshWords > 0) {
            chunks.add(makeChunk(doc, slug, chunks.size(), current));
        }
        return chunks;
    }

    /** Trailing sentences worth at most overlapWords words, or none if they would not leave room for the next piece. */
    private List<String> tail(List<String> pieces, int nextPieceWords) {
        List<String> kept = new ArrayList<>();
        int words = 0;
        for (int i = pieces.size() - 1; i >= 0; i--) {
            int w = TextUtil.wordCount(pieces.get(i));
            if (words + w > overlapWords) {
                break;
            }
            kept.add(0, pieces.get(i));
            words += w;
        }
        if (words + nextPieceWords > targetWords) {
            return new ArrayList<>();
        }
        return kept;
    }

    private List<String> capLength(String sentence) {
        String[] words = sentence.trim().split("\\s+");
        List<String> out = new ArrayList<>();
        if (words.length <= targetWords) {
            if (!sentence.isBlank()) {
                out.add(sentence.trim());
            }
            return out;
        }
        for (int start = 0; start < words.length; start += targetWords) {
            int end = Math.min(words.length, start + targetWords);
            out.add(String.join(" ", Arrays.copyOfRange(words, start, end)));
        }
        return out;
    }

    private int countWords(List<String> pieces) {
        int total = 0;
        for (String p : pieces) {
            total += TextUtil.wordCount(p);
        }
        return total;
    }

    private Chunk makeChunk(CorpusDocument doc, String slug, int index, List<String> pieces) {
        return new Chunk(slug + "#" + index, doc.title(), doc.url(), index, String.join(" ", pieces));
    }
}
