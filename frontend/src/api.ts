import type { Answer, Health, IngestResult } from "./types";

export class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, init);
  } catch {
    throw new ApiError("Cannot reach the API. Is the Spring Boot backend running on port 8080?", 0);
  }
  if (!response.ok) {
    let message = `Request failed with status ${response.status}.`;
    try {
      const body = (await response.json()) as { error?: string };
      if (body.error) {
        message = body.error;
      }
    } catch {
      // The body was not JSON. Keep the generic message.
    }
    throw new ApiError(message, response.status);
  }
  return (await response.json()) as T;
}

export function getHealth(): Promise<Health> {
  return request<Health>("/api/health");
}

export function askQuestion(question: string, topK?: number): Promise<Answer> {
  return request<Answer>("/api/ask", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ question, topK }),
  });
}

export function ingestCorpus(): Promise<IngestResult> {
  return request<IngestResult>("/api/ingest", { method: "POST" });
}
