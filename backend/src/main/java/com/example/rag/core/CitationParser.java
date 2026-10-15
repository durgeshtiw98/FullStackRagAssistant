package com.example.rag.core;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds source numbers such as [1], [2][3] or [1, 2] in an answer. */
public final class CitationParser {

    private static final Pattern GROUP = Pattern.compile("\\[(\\d{1,4}(?:\\s*[,;]\\s*\\d{1,4})*)\\]");

    private CitationParser() {
    }

    /** Source numbers cited in the text, limited to 1..maxSource, in ascending order. */
    public static Set<Integer> citedNumbers(String text, int maxSource) {
        Set<Integer> found = new TreeSet<>();
        if (text == null) {
            return found;
        }
        Matcher m = GROUP.matcher(text);
        while (m.find()) {
            for (String part : m.group(1).split("\\s*[,;]\\s*")) {
                int n = Integer.parseInt(part.trim());
                if (n >= 1 && n <= maxSource) {
                    found.add(n);
                }
            }
        }
        return found;
    }

    public static boolean isNotFound(String text) {
        return text != null && text.toLowerCase().contains("could not find the answer");
    }
}
