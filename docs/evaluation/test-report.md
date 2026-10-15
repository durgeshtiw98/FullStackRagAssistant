# Live project test report

Tested locally on October 15, 2026. The React frontend, actual Spring Boot application, real MiniLM ONNX
model, Wikipedia download, ingestion, saved-index reload, and all 50 evaluation requests ran successfully.
Groq was not configured because no API key was present. Answer quality remains limited in extractive mode.

## Verification

| Check | Result |
|---|---|
| Maven compile and JUnit tests | 28/28 passed, including the new HTTP client regression test |
| Frontend Vitest tests | 5/5 passed |
| TypeScript and Vite production build | Passed |
| Wikipedia corpus download | 13/13 articles, revision IDs recorded |
| Corpus keyword verification | All 50 questions passed |
| Actual ONNX ingestion | 1,740 passages, 384 dimensions, 15.864 seconds |
| Live API checks | 12/12 passed |
| Saved index | Reloaded all 1,740 passages after backend restart; live API checks passed again |
| Chrome desktop | 1440 × 1000; three example questions, rendered answers, citation navigation and expansion passed |
| Chrome phone | 390 × 844; manual question submission passed, no horizontal overflow |
| Browser runtime errors | None on final browser check |
| Answer + gold-article citation | 21/50 (42%), automatic keyword scoring, extractive mode |
| Gold article retrieved in top 5 | 49/50; this does not establish answer-bearing passage recall |

API checks covered blank/missing/overlong questions, invalid topK bounds, real finite 384-dimensional normalized
embeddings, identical-text determinism, empty embedding input rejection, all 13 indexed articles, requested topK,
extractive citation markers, and the Vite proxy. See `api-checks.json` and `browser-checks.json`.
Browser checks establish that the controls work; they do not assert factual correctness of the rendered answers.
For example, the Hubble example returned a passage describing JWST's launch, which is incorrect for that question.

## Fixes made while running

- Added `scripts/serve_embeddings.py` and pinned Python requirements so the project can run without the separate
  ONNX repository. The script downloads the real pinned public MiniLM ONNX model and tokenizer; it performs
  attention-mask mean pooling and L2 normalization with the existing 128-token limit.
- Set `HttpEmbeddingClient` to HTTP/1.1. Its default h2c upgrade caused the real Uvicorn service to receive an empty
  POST body and return 422 during ingestion. A JUnit regression test checks that the request contains JSON and no
  Upgrade header. Actual ingestion and all 50 evaluation requests succeeded after the fix.
- Added an inline empty favicon declaration to prevent a missing-favicon browser request.
- Updated README run instructions and recorded the measured extractive baseline.

Port 8000 was occupied by another local process. The new embedding service runs on 127.0.0.1:8001, and the backend
was started with `--rag.embedding-url=http://127.0.0.1:8001`. The existing process on port 8000 was left running.

## Evaluation findings

The original 50 questions and scoring rules were unchanged. Of 29 failures, 24 had an incorrect/incomplete answer
but the correct article citation, 2 had both an incorrect answer and article citation, 2 contained the expected
answer keywords but cited a different article, and 1 did not retrieve a gold article. The retrieval miss was q28,
asking for the first human in space. Some answers citing another article are factually supported there, so strict
gold-article scoring can reject an otherwise useful answer.

The fallback scores sentences by question-word overlap in only the first three retrieved passages. This can
select a passage about the right topic without answering the question. Retrieving the correct article in 49/50
cases is encouraging but is not evidence that the exact answer passage was present. Some short numeric keywords
also produce false positives. Do not describe this run as human-reviewed accuracy or Groq/Llama performance.
No chunk-size or answer-ranking tuning was performed on this evaluation set.

The original separate ONNX project's implementation and real Groq calls, retries, model behavior, and quotas were
not tested. The local embedding service uses the real model, but is an alternative to that missing project.

## Dependency audit

`npm audit --json` reported six affected development-tool packages: three moderate, one high, and two critical.
The affected package entries were `@vitest/mocker`, `esbuild`, `tinypool`, `vite`, `vite-node`, and `vitest`.
These include vulnerable development servers/test tooling; a successful production build does not resolve them.
Dependency upgrades were not mixed into this functional test run. The local Vite server binds to 127.0.0.1.
Before publishing, `pip-audit -r scripts/embedding-requirements.txt` found no known vulnerabilities in the resolved
Python requirements. The refreshed npm audit still reported the six development-tool advisories above.
No Java vulnerability audit was performed.

## Artifacts and running app

- `summary.md`: automatic score and breakdown.
- `failures.md`: all 29 failures with expected keywords and returned answers.
- `results.jsonl`: all 50 question results.
- `run-context.json`: date, pinned model revision, hashes, corpus revisions, runtime versions and settings.
- `api-checks.json`, `browser-checks.json`: live verification outcomes.
- Desktop and phone screenshots from the automated run remain in the ignored local `eval/results/` folder.
  The README's checked-in UI screenshots are in `docs/images/`.

App left running at http://127.0.0.1:5173, API at http://127.0.0.1:8080, and embeddings at http://127.0.0.1:8001.
The saved index is `data/index.bin`. Maven used `/private/tmp/rag-m2` for its dependency cache.
