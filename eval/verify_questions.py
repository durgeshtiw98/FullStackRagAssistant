#!/usr/bin/env python3
"""Check the question file against the downloaded corpus before running the evaluation.

    python eval/verify_questions.py

For every question it confirms that the gold article exists in the corpus and that each answer keyword appears in
at least one gold article. A keyword that is missing means the question is wrong, the keyword is too strict, or the
article changed. Fix those before trusting a pass rate. This does not prove an answer is correct: read the
questions yourself once, since you own this test set.
"""
import argparse
import sys
from pathlib import Path

from common import group_matches, load_corpus, load_questions, normalize


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--questions", default="eval/questions.jsonl")
    parser.add_argument("--corpus", default="data/corpus")
    args = parser.parse_args()

    questions = load_questions(Path(args.questions))
    corpus = load_corpus(Path(args.corpus))
    if not corpus:
        print(f"No .txt files in {args.corpus}. Run scripts/fetch_wikipedia.py first.", file=sys.stderr)
        return 2
    normalized = {title: normalize(text) for title, text in corpus.items()}

    problems = []
    ids = set()
    for q in questions:
        for key in ("id", "question", "answer_keywords", "gold_docs"):
            if key not in q:
                problems.append(f"{q.get('id', '?')}: missing field {key!r}")
        if q.get("id") in ids:
            problems.append(f"{q['id']}: duplicate id")
        ids.add(q.get("id"))
        gold = [d for d in q.get("gold_docs", []) if d in normalized]
        for d in q.get("gold_docs", []):
            if d not in normalized:
                problems.append(f"{q['id']}: gold article {d!r} is not in the corpus")
        for group in q.get("answer_keywords", []):
            if gold and not any(group_matches(group, normalized[d]) for d in gold):
                problems.append(f"{q['id']}: keyword {group!r} not found in {gold} ({q['question']})")

    print(f"{len(questions)} questions, {len(corpus)} articles.")
    if len(questions) != 50:
        problems.append(f"expected 50 questions, found {len(questions)}")
    for p in problems:
        print("PROBLEM", p)
    print("All questions check out." if not problems else f"{len(problems)} problem(s) found.")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
