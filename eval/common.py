"""Shared helpers for the evaluation scripts."""
import json
import re
from pathlib import Path


def normalize(text: str) -> str:
    """Lowercase, drop commas and extra whitespace, so '2,000' and '2000' and 'July 20, 1969' compare fairly."""
    text = text.lower().replace("‑", "-").replace("–", "-").replace(" ", " ")
    text = text.replace(",", "")
    return re.sub(r"\s+", " ", text).strip()


def group_matches(group: str, text_norm: str) -> bool:
    """A group is one or more alternatives separated by '|'. It matches if any alternative is in the text."""
    return any(normalize(option) in text_norm for option in group.split("|"))


def keywords_match(groups: list[str], text: str) -> bool:
    """Every group must match (AND), and within a group any alternative may match (OR)."""
    text_norm = normalize(text)
    return all(group_matches(group, text_norm) for group in groups)


def load_questions(path: Path) -> list[dict]:
    questions = []
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if line.strip():
            try:
                questions.append(json.loads(line))
            except json.JSONDecodeError as error:
                raise SystemExit(f"{path}:{number}: invalid JSON ({error})")
    return questions


def load_corpus(directory: Path) -> dict[str, str]:
    """Map article title to its body text."""
    docs = {}
    for file in sorted(directory.glob("*.txt")):
        title, lines, in_header = file.stem, [], True
        for line in file.read_text(encoding="utf-8").splitlines():
            if in_header:
                if line.startswith("TITLE:"):
                    title = line[len("TITLE:"):].strip()
                    continue
                if line.startswith(("URL:", "REVISION:")):
                    continue
                in_header = False
                if not line.strip():
                    continue
            lines.append(line)
        docs[title] = "\n".join(lines)
    return docs
