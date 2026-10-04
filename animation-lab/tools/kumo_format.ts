/** Animator JSON as authored: two-space indent, anything that fits in the width on one line. */

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Obj = { [key: string]: any };

const WIDTH = 110;
function compactJson(v: unknown): string {
  if (Array.isArray(v)) return "[" + v.map(compactJson).join(", ") + "]";
  if (v !== null && typeof v === "object") {
    return "{" + Object.entries(v as Obj).map(([k, x]) => JSON.stringify(k) + ": " + compactJson(x)).join(", ") + "}";
  }
  return JSON.stringify(v);
}
const isObj = (v: unknown): v is Obj => v !== null && typeof v === "object" && !Array.isArray(v);
const inline = (v: unknown, indent: number): boolean => compactJson(v).length + indent <= WIDTH;
export function pretty(v: unknown, indent = 0): string {
  const pad = " ".repeat(indent);
  if (Array.isArray(v)) {
    if (v.length === 0) return "[]";
    if (inline(v, indent)) return compactJson(v);
    return "[\n" + v.map((x) => pad + "  " + pretty(x, indent + 2)).join(",\n") + "\n" + pad + "]";
  }
  if (isObj(v)) {
    const entries = Object.entries(v);
    if (entries.length === 0) return "{}";
    if (inline(v, indent)) return compactJson(v);
    return "{\n" + entries.map(([k, x]) => pad + "  " + JSON.stringify(k) + ": " + pretty(x, indent + 2)).join(",\n") + "\n" + pad + "}";
  }
  return JSON.stringify(v);
}

