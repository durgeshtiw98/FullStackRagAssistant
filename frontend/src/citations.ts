export type Segment =
  | { type: "text"; value: string }
  | { type: "cite"; numbers: number[]; raw: string };

const CITATION = /\[(\d{1,4}(?:\s*[,;]\s*\d{1,4})*)\]/g;

/**
 * Splits an answer into plain text and citation markers such as [1], [2][3] or [1, 2].
 * Numbers above maxSource are kept as plain text, because they do not point at a real source.
 */
export function splitCitations(text: string, maxSource: number): Segment[] {
  const segments: Segment[] = [];
  let last = 0;
  for (const match of text.matchAll(CITATION)) {
    const start = match.index ?? 0;
    const numbers = match[1]
      .split(/\s*[,;]\s*/)
      .map((part) => Number.parseInt(part, 10))
      .filter((n) => n >= 1 && n <= maxSource);
    if (numbers.length === 0) {
      continue;
    }
    if (start > last) {
      segments.push({ type: "text", value: text.slice(last, start) });
    }
    segments.push({ type: "cite", numbers, raw: match[0] });
    last = start + match[0].length;
  }
  if (last < text.length) {
    segments.push({ type: "text", value: text.slice(last) });
  }
  return segments;
}
