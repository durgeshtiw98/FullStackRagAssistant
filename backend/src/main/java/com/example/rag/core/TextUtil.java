package com.example.rag.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Small text helpers shared by the chunker and the extractive answerer. */
public final class TextUtil {

    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?])\\s+(?=[\"'(\\[]?[A-Z0-9])");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private TextUtil() {
    }

    public static List<String> splitSentences(String text) {
        List<String> out = new ArrayList<>();
        for (String sentence : SENTENCE_BOUNDARY.split(text.trim())) {
            String s = sentence.trim();
            if (!s.isEmpty()) {
                out.add(s);
            }
        }
        return out;
    }

    public static String collapseWhitespace(String text) {
        return WHITESPACE.matcher(text).replaceAll(" ").trim();
    }

    public static int wordCount(String text) {
        String t = text.trim();
        return t.isEmpty() ? 0 : WHITESPACE.split(t).length;
    }

    public static String slug(String title) {
        String s = NON_ALNUM.matcher(title.toLowerCase(Locale.ROOT)).replaceAll("-");
        s = s.replaceAll("^-+|-+$", "");
        return s.isEmpty() ? "doc" : s;
    }
}
