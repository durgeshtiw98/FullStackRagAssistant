package com.example.rag.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Reads {@code .txt} files that start with {@code TITLE:} and {@code URL:} header lines, a blank line, then the body.
 * Wikipedia style heading lines such as {@code == History ==} are dropped.
 */
public final class CorpusLoader {

    public List<CorpusDocument> load(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            throw new IOException("Corpus directory not found: " + dir.toAbsolutePath());
        }
        List<Path> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream.filter(p -> p.getFileName().toString().endsWith(".txt")).sorted().toList();
        }
        List<CorpusDocument> docs = new ArrayList<>();
        for (Path file : files) {
            docs.add(parse(file.getFileName().toString(), Files.readString(file, StandardCharsets.UTF_8)));
        }
        return docs;
    }

    CorpusDocument parse(String fileName, String raw) {
        String title = fileName.replaceFirst("\\.txt$", "");
        String url = null;
        StringBuilder body = new StringBuilder();
        boolean inHeader = true;
        for (String line : raw.split("\\R", -1)) {
            if (inHeader) {
                if (line.startsWith("TITLE:")) {
                    title = line.substring("TITLE:".length()).trim();
                    continue;
                }
                if (line.startsWith("URL:")) {
                    url = line.substring("URL:".length()).trim();
                    continue;
                }
                if (line.startsWith("REVISION:")) {
                    continue;
                }
                if (line.isBlank()) {
                    inHeader = false;
                    continue;
                }
                inHeader = false;
            }
            if (line.matches("^\\s*=+.*?=+\\s*$")) {
                continue;
            }
            body.append(line).append('\n');
        }
        return new CorpusDocument(title, url, body.toString());
    }
}
