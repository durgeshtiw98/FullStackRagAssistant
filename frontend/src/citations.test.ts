import { describe, expect, it } from "vitest";
import { splitCitations } from "./citations";

describe("splitCitations", () => {
  it("separates text from a single citation", () => {
    expect(splitCitations("Armstrong commanded it [1].", 3)).toEqual([
      { type: "text", value: "Armstrong commanded it " },
      { type: "cite", numbers: [1], raw: "[1]" },
      { type: "text", value: "." },
    ]);
  });

  it("handles adjacent and grouped citations", () => {
    expect(splitCitations("A [1][2] and B [2, 3]", 3)).toEqual([
      { type: "text", value: "A " },
      { type: "cite", numbers: [1], raw: "[1]" },
      { type: "cite", numbers: [2], raw: "[2]" },
      { type: "text", value: " and B " },
      { type: "cite", numbers: [2, 3], raw: "[2, 3]" },
    ]);
  });

  it("keeps out-of-range numbers and ordinary brackets as text", () => {
    expect(splitCitations("See [9] and [note].", 3)).toEqual([{ type: "text", value: "See [9] and [note]." }]);
  });

  it("returns a single text segment when there are no citations", () => {
    expect(splitCitations("No sources here.", 3)).toEqual([{ type: "text", value: "No sources here." }]);
  });

  it("returns nothing for empty text", () => {
    expect(splitCitations("", 3)).toEqual([]);
  });
});
