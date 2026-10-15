import { splitCitations } from "../citations";
import type { Answer, AnswerMode } from "../types";

interface Props {
  answer: Answer;
  onCitationClick: (n: number) => void;
}

const MODE_LABEL: Record<AnswerMode, string> = {
  llm: "Written by the answer model from the sources below",
  extractive: "Best matching sentences, no answer model used",
  none: "No relevant passage found",
};

export function AnswerCard({ answer, onCitationClick }: Props) {
  const segments = splitCitations(answer.answer, answer.sources.length);
  return (
    <section className="answer-card" aria-labelledby="answer-heading">
      <div className="answer-head">
        <h2 id="answer-heading">Answer</h2>
        <span className={`mode mode-${answer.mode}`}>{MODE_LABEL[answer.mode]}</span>
      </div>
      <p className="answer-text">
        {segments.map((segment, index) =>
          segment.type === "text" ? (
            <span key={index}>{segment.value}</span>
          ) : (
            <span key={index} className="cite-group">
              {segment.numbers.map((n) => (
                <button
                  key={n}
                  type="button"
                  className="cite"
                  onClick={() => onCitationClick(n)}
                  aria-label={`Go to source ${n}`}
                >
                  {n}
                </button>
              ))}
            </span>
          ),
        )}
      </p>
      {answer.note && (
        <p className="note" role="note">
          {answer.note}
        </p>
      )}
    </section>
  );
}
