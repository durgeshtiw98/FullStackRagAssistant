import { useState } from "react";
import type { Source } from "../types";

interface Props {
  sources: Source[];
  activeSource: number | null;
}

export function SourceList({ sources, activeSource }: Props) {
  const [expanded, setExpanded] = useState<Set<number>>(new Set());

  function toggle(n: number) {
    setExpanded((previous) => {
      const next = new Set(previous);
      if (next.has(n)) {
        next.delete(n);
      } else {
        next.add(n);
      }
      return next;
    });
  }

  if (sources.length === 0) {
    return null;
  }

  return (
    <section aria-labelledby="sources-heading" className="sources">
      <h2 id="sources-heading">Sources</h2>
      <ol className="source-list">
        {sources.map((source) => {
          const isOpen = expanded.has(source.n) || activeSource === source.n;
          return (
            <li
              key={source.chunkId}
              id={`source-${source.n}`}
              className={`source${source.cited ? " cited" : ""}${activeSource === source.n ? " active" : ""}`}
            >
              <div className="source-head">
                <span className="source-number">{source.n}</span>
                {source.url ? (
                  <a href={source.url} target="_blank" rel="noreferrer" className="source-title">
                    {source.docTitle}
                  </a>
                ) : (
                  <span className="source-title">{source.docTitle}</span>
                )}
                {source.cited && <span className="badge">Cited</span>}
                <span className="score" title="Cosine similarity to the question">
                  {source.score.toFixed(2)}
                </span>
              </div>
              <p className={isOpen ? "passage" : "passage clamped"}>{source.text}</p>
              <button type="button" className="link-button" onClick={() => toggle(source.n)} aria-expanded={isOpen}>
                {isOpen ? "Show less" : "Show full passage"}
              </button>
            </li>
          );
        })}
      </ol>
    </section>
  );
}
