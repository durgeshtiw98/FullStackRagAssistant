#!/usr/bin/env python3
"""Download Wikipedia articles as plain text into data/corpus.

    python scripts/fetch_wikipedia.py                       # uses scripts/topics.txt
    python scripts/fetch_wikipedia.py --topics my_topics.txt --out data/corpus

Each output file starts with TITLE, URL and REVISION lines, a blank line, then the article text.
Wikipedia text is available under CC BY-SA 4.0. The REVISION line records exactly which version was used.
"""
import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

API = "https://en.wikipedia.org/w/api.php"
USER_AGENT = "rag-knowledge-assistant/0.1 (educational project; https://github.com/)"
LEVEL2_HEADING = re.compile(r"^==\s*([^=].*?)\s*==\s*$")
TRAILING_SECTIONS = {
    "see also", "notes", "references", "external links", "further reading",
    "bibliography", "footnotes", "sources", "citations",
}


def fetch_page(title: str) -> dict:
    params = {
        "action": "query", "format": "json", "formatversion": "2", "redirects": "1",
        "prop": "extracts|revisions|info", "explaintext": "1", "rvprop": "ids",
        "inprop": "url", "titles": title,
    }
    request = urllib.request.Request(API + "?" + urllib.parse.urlencode(params), headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        data = json.load(response)
    pages = data.get("query", {}).get("pages", [])
    if not pages or pages[0].get("missing"):
        raise ValueError(f"No Wikipedia page found for {title!r}")
    return pages[0]


def trim_trailing_sections(text: str) -> str:
    kept = []
    for line in text.splitlines():
        match = LEVEL2_HEADING.match(line)
        if match and match.group(1).strip().lower() in TRAILING_SECTIONS:
            break
        kept.append(line)
    return re.sub(r"\n{3,}", "\n\n", "\n".join(kept)).strip()


def slugify(title: str) -> str:
    return re.sub(r"[^a-z0-9]+", "-", title.lower()).strip("-") or "article"


def read_topics(path: Path) -> list[str]:
    topics = []
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            topics.append(line)
    return topics


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--topics", default="scripts/topics.txt")
    parser.add_argument("--out", default="data/corpus")
    parser.add_argument("--delay", type=float, default=1.0, help="seconds between requests")
    args = parser.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    failures = 0
    for topic in read_topics(Path(args.topics)):
        try:
            page = fetch_page(topic)
            body = trim_trailing_sections(page.get("extract", ""))
            if len(body.split()) < 200:
                raise ValueError("article text is suspiciously short")
            revision = page.get("revisions", [{}])[0].get("revid", "unknown")
            header = f"TITLE: {page['title']}\nURL: {page.get('fullurl', '')}\nREVISION: {revision}\n\n"
            (out_dir / f"{slugify(page['title'])}.txt").write_text(header + body + "\n", encoding="utf-8")
            print(f"ok    {page['title']} ({len(body.split()):,} words, revision {revision})")
        except (ValueError, urllib.error.URLError, json.JSONDecodeError) as error:
            failures += 1
            print(f"FAIL  {topic}: {error}", file=sys.stderr)
        time.sleep(args.delay)
    print(f"Done. {failures} failure(s). Files are in {out_dir}.")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
