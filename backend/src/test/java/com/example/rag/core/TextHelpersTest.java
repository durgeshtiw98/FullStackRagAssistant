package com.example.rag.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TextHelpersTest {

    @Test
    void citationParserFindsAllForms() {
        assertEquals(Set.of(1), CitationParser.citedNumbers("It was Armstrong [1].", 5));
        assertEquals(Set.of(1, 3), CitationParser.citedNumbers("A [1] and B [3].", 5));
        assertEquals(Set.of(1, 2), CitationParser.citedNumbers("Both agree [1, 2].", 5));
        assertEquals(Set.of(2, 4), CitationParser.citedNumbers("Joined [2][4]", 5));
    }

    @Test
    void citationParserIgnoresOutOfRangeAndNonCitations() {
        assertEquals(Set.of(), CitationParser.citedNumbers("Out of range [9] and [0].", 5));
        assertEquals(Set.of(), CitationParser.citedNumbers("No brackets, or [note] and (1).", 5));
        assertEquals(Set.of(), CitationParser.citedNumbers(null, 5));
        assertEquals(Set.of(), CitationParser.citedNumbers("Huge [123456789]", 5));
    }

    @Test
    void notFoundAnswerIsRecognized() {
        assertTrue(CitationParser.isNotFound(PromptBuilder.NOT_FOUND));
        assertFalse(CitationParser.isNotFound("Armstrong commanded it [1]."));
    }

    @Test
    void promptNumbersSourcesAndRepeatsTheQuestion() {
        List<ScoredChunk> sources = List.of(
                new ScoredChunk(new Chunk("a#0", "Apollo 11", null, 0, "Armstrong commanded the mission."), 0.9f),
                new ScoredChunk(new Chunk("b#0", "Saturn V", null, 0, "It had three stages."), 0.5f));
        Prompt prompt = new PromptBuilder().build("  Who commanded Apollo 11? ", sources);
        assertTrue(prompt.user().contains("[1] (Apollo 11) Armstrong commanded the mission."));
        assertTrue(prompt.user().contains("[2] (Saturn V) It had three stages."));
        assertTrue(prompt.user().endsWith("Question: Who commanded Apollo 11?"));
        assertTrue(prompt.system().contains(PromptBuilder.NOT_FOUND));
    }

    @Test
    void extractiveAnswerPicksTheMatchingSentenceAndCitesItsSource() {
        List<ScoredChunk> top = List.of(
                new ScoredChunk(new Chunk("a#0", "Saturn V", null, 0,
                        "The rocket was very tall. It had three stages. Each stage used different engines."), 0.60f),
                new ScoredChunk(new Chunk("b#0", "Apollo 11", null, 0,
                        "The crew trained for months. Neil Armstrong was the commander of the mission. "
                                + "Buzz Aldrin piloted the lunar module."), 0.55f));
        ExtractiveAnswerer.Result result = new ExtractiveAnswerer().answer("Who was the commander of Apollo 11?", top);
        assertNotNull(result);
        assertEquals(2, result.sourceNumber());
        assertTrue(result.text().startsWith("Neil Armstrong was the commander of the mission."));
        assertTrue(result.text().endsWith("[2]"));
    }

    @Test
    void corpusLoaderParsesHeaderAndDropsHeadings() {
        String raw = "TITLE: Apollo 11\nURL: https://en.wikipedia.org/wiki/Apollo_11\nREVISION: 123\n\n"
                + "Apollo 11 landed on the Moon.\n== Crew ==\nArmstrong was commander.\n";
        CorpusDocument doc = new CorpusLoader().parse("apollo-11.txt", raw);
        assertEquals("Apollo 11", doc.title());
        assertEquals("https://en.wikipedia.org/wiki/Apollo_11", doc.url());
        assertTrue(doc.text().contains("Apollo 11 landed on the Moon."));
        assertTrue(doc.text().contains("Armstrong was commander."));
        assertFalse(doc.text().contains("=="));
        assertFalse(doc.text().contains("TITLE"));
    }

    @Test
    void corpusLoaderFallsBackToFileNameWithoutHeader() {
        CorpusDocument doc = new CorpusLoader().parse("notes.txt", "Just a body line.\n");
        assertEquals("notes", doc.title());
        assertTrue(doc.text().contains("Just a body line."));
    }
}
