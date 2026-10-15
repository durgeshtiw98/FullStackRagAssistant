package com.example.rag.core;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Fallback when no answer model is available: returns the sentence that best matches the question. */
public final class ExtractiveAnswerer {

    public record Result(String text, int sourceNumber) {
    }

    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "a", "an", "of", "in", "on", "at", "to", "for", "and", "or", "is", "are", "was", "were", "did",
            "do", "does", "what", "which", "who", "whom", "when", "where", "how", "many", "much", "that", "this",
            "with", "by", "from", "as", "it", "its", "be", "been", "has", "have", "had");
    private static final int CANDIDATE_SOURCES = 3;

    /** @param top sources in rank order; source numbers are their 1-based positions */
    public Result answer(String question, List<ScoredChunk> top) {
        if (top.isEmpty()) {
            return null;
        }
        Set<String> terms = contentWords(question);
        double bestScore = -1;
        int bestSource = 0;
        int bestSentence = 0;
        List<String> bestSentences = null;
        for (int i = 0; i < Math.min(CANDIDATE_SOURCES, top.size()); i++) {
            List<String> sentences = TextUtil.splitSentences(top.get(i).chunk().text());
            for (int j = 0; j < sentences.size(); j++) {
                Set<String> words = contentWords(sentences.get(j));
                int overlap = 0;
                for (String t : terms) {
                    if (words.contains(t)) {
                        overlap++;
                    }
                }
                double score = overlap + top.get(i).score();
                if (score > bestScore) {
                    bestScore = score;
                    bestSource = i;
                    bestSentence = j;
                    bestSentences = sentences;
                }
            }
        }
        if (bestSentences == null) {
            return new Result(top.get(0).chunk().text() + " [1]", 1);
        }
        String text = bestSentences.get(bestSentence);
        if (bestSentence + 1 < bestSentences.size()) {
            text = text + " " + bestSentences.get(bestSentence + 1);
        }
        int number = bestSource + 1;
        return new Result(text + " [" + number + "]", number);
    }

    private static Set<String> contentWords(String text) {
        Set<String> out = new HashSet<>();
        for (String w : NON_WORD.split(text.toLowerCase(Locale.ROOT))) {
            if (!w.isEmpty() && !STOP_WORDS.contains(w)) {
                out.add(w);
            }
        }
        return out;
    }
}
