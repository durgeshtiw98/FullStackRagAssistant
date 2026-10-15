#!/usr/bin/env python3
"""Run the question set against the running API and write a pass/fail log.

    python eval/run_eval.py --url http://localhost:8080 --delay 3

Checks per question:
  answer     every answer keyword group appears in the answer text (automatic keyword match, a coarse proxy)
  citation   a gold article is among the sources the answer cites
  retrieval  a gold article is among the retrieved sources at all
A question passes when the answer check and the citation check both pass.
Outputs in --out: results.jsonl (everything), summary.md (counts), failures.md (one entry per failure).
"""
import argparse
import json
import sys
import time
import urllib.error
import urllib.request
from collections import Counter, defaultdict
from pathlib import Path

from common import keywords_match, load_questions


def ask(url: str, question: str, timeout: float) -> dict:
    body = json.dumps({"question": question}).encode()
    request = urllib.request.Request(url.rstrip("/") + "/api/ask", data=body, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        return json.load(response)


def evaluate(q: dict, response: dict) -> dict:
    sources = response.get("sources", [])
    gold = set(q["gold_docs"])
    retrieved_docs = [s["docTitle"] for s in sources]
    cited_docs = [s["docTitle"] for s in sources if s.get("cited")]
    answer_ok = keywords_match(q["answer_keywords"], response.get("answer", ""))
    citation_ok = bool(gold & set(cited_docs))
    retrieval_ok = bool(gold & set(retrieved_docs))
    if answer_ok and citation_ok:
        failure = None
    elif not retrieval_ok:
        failure = "retrieval miss"
    elif answer_ok:
        failure = "answer right, citation missing or from the wrong article"
    elif citation_ok:
        failure = "answer wrong or incomplete, citation right"
    else:
        failure = "answer wrong and citation wrong"
    return {
        "id": q["id"], "question": q["question"], "type": q.get("type", ""), "expected": q["answer_keywords"],
        "gold_docs": q["gold_docs"], "answer": response.get("answer", ""), "mode": response.get("mode"),
        "retrieved_docs": retrieved_docs, "cited_docs": cited_docs, "answer_ok": answer_ok,
        "citation_ok": citation_ok, "retrieval_ok": retrieval_ok, "passed": failure is None, "failure": failure,
        "note": response.get("note"),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--url", default="http://localhost:8080")
    parser.add_argument("--questions", default="eval/questions.jsonl")
    parser.add_argument("--out", default="eval/results")
    parser.add_argument("--delay", type=float, default=3.0, help="seconds between questions, for API rate limits")
    parser.add_argument("--timeout", type=float, default=120.0)
    parser.add_argument("--limit", type=int, default=0, help="only run the first N questions")
    args = parser.parse_args()

    questions = load_questions(Path(args.questions))
    if args.limit:
        questions = questions[: args.limit]
    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    results = []
    for index, q in enumerate(questions, 1):
        try:
            result = evaluate(q, ask(args.url, q["question"], args.timeout))
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError) as error:
            result = {"id": q["id"], "question": q["question"], "type": q.get("type", ""), "expected": q["answer_keywords"],
                      "gold_docs": q["gold_docs"], "answer": "", "mode": None, "retrieved_docs": [], "cited_docs": [],
                      "answer_ok": False, "citation_ok": False, "retrieval_ok": False, "passed": False,
                      "failure": f"request error: {error}", "note": None}
        results.append(result)
        print(f"[{index:>2}/{len(questions)}] {'PASS' if result['passed'] else 'FAIL'}  {q['question']}")
        time.sleep(args.delay)

    total = len(results)
    passed = sum(r["passed"] for r in results)
    modes = Counter(r["mode"] for r in results)
    with open(out_dir / "results.jsonl", "w", encoding="utf-8") as f:
        for r in results:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")

    by_type = defaultdict(lambda: [0, 0])
    for r in results:
        by_type[r["type"] or "other"][0] += r["passed"]
        by_type[r["type"] or "other"][1] += 1
    failures = [r for r in results if not r["passed"]]
    failure_counts = Counter(r["failure"].split(":")[0] for r in failures)

    lines = [
        "# Evaluation summary", "",
        f"**Passed {passed} of {total} ({passed / total:.0%}).**", "",
        f"- Answer check passed: {sum(r['answer_ok'] for r in results)} of {total}",
        f"- Citation check passed: {sum(r['citation_ok'] for r in results)} of {total}",
        f"- Gold article retrieved in the top results: {sum(r['retrieval_ok'] for r in results)} of {total}",
        f"- Answer modes: {dict(modes)}", "",
        "The answer check is a keyword match, not a human judgment. A correct answer worded differently can fail, and a",
        "wrong answer that happens to contain the keywords can pass.", "",
        "## By question type", "", "| Type | Passed | Total |", "|---|---|---|",
    ]
    lines += [f"| {t} | {p} | {n} |" for t, (p, n) in sorted(by_type.items())]
    lines += ["", "## Failure causes", ""]
    lines += [f"- {cause}: {count}" for cause, count in failure_counts.most_common()] or ["- none"]
    (out_dir / "summary.md").write_text("\n".join(lines) + "\n", encoding="utf-8")

    entries = ["# Failures", ""]
    for r in failures:
        entries += [
            f"## {r['id']}: {r['question']}", "",
            f"- Cause: {r['failure']}", f"- Expected keywords: {r['expected']}", f"- Gold articles: {r['gold_docs']}",
            f"- Retrieved: {r['retrieved_docs']}", f"- Cited: {r['cited_docs']}", f"- Answer: {r['answer']}", "",
        ]
    if not failures:
        entries.append("No failures.")
    (out_dir / "failures.md").write_text("\n".join(entries) + "\n", encoding="utf-8")

    print(f"\nPassed {passed} of {total}. Details in {out_dir}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
