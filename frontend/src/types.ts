export interface Source {
  n: number;
  chunkId: string;
  docTitle: string;
  url: string | null;
  text: string;
  score: number;
  cited: boolean;
}

export type AnswerMode = "llm" | "extractive" | "none";

export interface Answer {
  answer: string;
  mode: AnswerMode;
  sources: Source[];
  note: string | null;
}

export interface Health {
  status: string;
  indexedChunks: number;
  answerMode: "llm" | "extractive";
}

export interface IngestResult {
  documents: number;
  chunks: number;
  dimension: number;
  millis: number;
}
