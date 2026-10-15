# Evaluation summary

Run: October 15, 2026. **Real MiniLM ONNX retrieval with extractive answers; Groq was not configured or tested.**
Model: `sentence-transformers/all-MiniLM-L6-v2`, revision `1110a243fdf4706b3f48f1d95db1a4f5529b4d41`.
Corpus: 13 Wikipedia articles, 1,740 chunks. All 50 corpus keyword checks passed.
Settings: 80-word chunks, 15-word overlap, 128-token embedding limit, top-k 5, min-score 0.05.
Command: `python3 -u eval/run_eval.py --delay 0`.
Full settings, corpus revisions and hashes: [run-context.json](run-context.json).

**Passed 21 of 50 (42%).**

- Answer check passed: 24 of 50
- Citation check passed: 45 of 50
- Gold article retrieved in the top results: 49 of 50
- Answer modes: {'extractive': 50}

The answer check is a keyword match, not a human judgment. A correct answer worded differently can fail, and a
wrong answer that happens to contain the keywords can pass.
Gold-article retrieval only checks the article title, not whether the retrieved passage contains the answer.
These counts must not be described as Llama/Groq or human-rated answer accuracy.

## By question type

| Type | Passed | Total |
|---|---|---|
| cause | 0 | 2 |
| date | 5 | 10 |
| list | 2 | 4 |
| name | 5 | 16 |
| number | 5 | 9 |
| person | 3 | 8 |
| place | 1 | 1 |

## Failure causes

- answer wrong or incomplete, citation right: 24
- answer wrong and citation wrong: 2
- answer right, citation missing or from the wrong article: 2
- retrieval miss: 1
