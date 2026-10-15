import { FormEvent, useCallback, useEffect, useState } from "react";
import { ApiError, askQuestion, getHealth, ingestCorpus } from "./api";
import { AnswerCard } from "./components/AnswerCard";
import { SourceList } from "./components/SourceList";
import type { Answer, Health } from "./types";

const EXAMPLES = [
  "Who was the commander of Apollo 11?",
  "What caused the Apollo 13 emergency?",
  "When was the Hubble Space Telescope launched?",
];

export default function App() {
  const [health, setHealth] = useState<Health | null>(null);
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState<Answer | null>(null);
  const [asked, setAsked] = useState("");
  const [loading, setLoading] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeSource, setActiveSource] = useState<number | null>(null);

  const refreshHealth = useCallback(async () => {
    try {
      setHealth(await getHealth());
    } catch (e) {
      setHealth(null);
      setError(e instanceof ApiError ? e.message : "Could not load the API status.");
    }
  }, []);

  useEffect(() => {
    void refreshHealth();
  }, [refreshHealth]);

  async function ask(text: string) {
    const trimmed = text.trim();
    if (!trimmed || loading) {
      return;
    }
    setLoading(true);
    setError(null);
    setActiveSource(null);
    try {
      const result = await askQuestion(trimmed);
      setAnswer(result);
      setAsked(trimmed);
    } catch (e) {
      setAnswer(null);
      setError(e instanceof ApiError ? e.message : "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    void ask(question);
  }

  async function onIngest() {
    setIngesting(true);
    setError(null);
    try {
      await ingestCorpus();
      await refreshHealth();
    } catch (e) {
      setError(e instanceof ApiError ? e.message : "Ingestion failed.");
    } finally {
      setIngesting(false);
    }
  }

  function onCitationClick(n: number) {
    setActiveSource(n);
    document.getElementById(`source-${n}`)?.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  const emptyIndex = health !== null && health.indexedChunks === 0;

  return (
    <main className="app">
      <header className="app-header">
        <h1>Document Q&amp;A</h1>
        <p className="status" aria-live="polite">
          {health
            ? `${health.indexedChunks.toLocaleString()} passages indexed · ${
                health.answerMode === "llm" ? "answer model on" : "extractive answers"
              }`
            : "API not connected"}
        </p>
      </header>

      {emptyIndex && (
        <div className="banner">
          <p>The index is empty. Start the embedding service, then build the index from the corpus folder.</p>
          <button type="button" onClick={() => void onIngest()} disabled={ingesting}>
            {ingesting ? "Indexing, this can take a minute" : "Build index"}
          </button>
        </div>
      )}

      <form onSubmit={onSubmit} className="ask-form">
        <label htmlFor="question">Ask a question about the documents</label>
        <div className="ask-row">
          <input
            id="question"
            type="text"
            value={question}
            onChange={(event) => setQuestion(event.target.value)}
            placeholder="For example: Who was the commander of Apollo 11?"
            maxLength={1000}
            autoComplete="off"
          />
          <button type="submit" disabled={loading || question.trim() === "" || emptyIndex}>
            {loading ? "Searching" : "Ask"}
          </button>
        </div>
        <div className="examples">
          {EXAMPLES.map((example) => (
            <button
              key={example}
              type="button"
              className="chip"
              onClick={() => {
                setQuestion(example);
                void ask(example);
              }}
              disabled={loading || emptyIndex}
            >
              {example}
            </button>
          ))}
        </div>
      </form>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      {loading && <p className="loading">Searching the documents</p>}

      {answer && !loading && (
        <div aria-live="polite">
          <p className="asked">{asked}</p>
          <AnswerCard answer={answer} onCitationClick={onCitationClick} />
          <SourceList sources={answer.sources} activeSource={activeSource} />
        </div>
      )}
    </main>
  );
}
