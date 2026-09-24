/**
 * Python-compatible number formatting and JSON output for the lab tools.
 *
 * The tools were first written in Python, and the generated assets keep its conventions so that
 * regenerating them does not churn every file: a float that happens to be integral is written as
 * "1.0" (a JS number cannot tell 1 from 1.0, so such values are wrapped in {@link Float}),
 * floats use Python's repr ("1e-05", not "1e-5"), rounding is half-even on the exact binary value,
 * and json.dump's separators and indentation are reproduced.
 */

/** A number Python treats as a float: written as "1.0" even when integral. */
export class Float extends Number {}

export const float = (value: number): Float => new Float(value);

export type Json = null | boolean | number | Float | string | Json[] | { [key: string]: Json };

/** The exact value of a finite double as mantissa * 2^exponent. */
function decompose(x: number): [bigint, number] {
  const view = new DataView(new ArrayBuffer(8));
  view.setFloat64(0, Math.abs(x));
  const bits = view.getBigUint64(0);
  const biased = Number((bits >> 52n) & 0x7ffn);
  const fraction = bits & ((1n << 52n) - 1n);
  return biased === 0 ? [fraction, -1074] : [fraction | (1n << 52n), biased - 1075];
}

function isNegative(x: number): boolean {
  return x < 0 || Object.is(x, -0);
}

/** The digits of |x| rounded half-even to `digits` decimals, as Python's '%.Nf' and round() do. */
function fixedDigits(x: number, digits: number): string {
  const [mantissa, exponent] = decompose(x);
  let scaled = mantissa * 10n ** BigInt(digits);
  if (exponent >= 0) {
    scaled <<= BigInt(exponent);
  } else {
    const denominator = 1n << BigInt(-exponent);
    const quotient = scaled / denominator;
    const twiceRemainder = (scaled % denominator) * 2n;
    const roundUp = twiceRemainder > denominator || (twiceRemainder === denominator && (quotient & 1n) === 1n);
    scaled = roundUp ? quotient + 1n : quotient;
  }
  const text = scaled.toString().padStart(digits + 1, "0");
  return digits === 0 ? text : `${text.slice(0, -digits)}.${text.slice(-digits)}`;
}

/** Python's round(x, digits) for a float. */
export function pyRound(x: number, digits: number): number {
  const rounded = Number(fixedDigits(x, digits));
  return isNegative(x) ? -rounded : rounded;
}

/** Python's format(x, '[+][width].{digits}f'). */
export function formatFixed(x: number, digits: number, { width = 0, plus = false } = {}): string {
  const sign = isNegative(x) ? "-" : plus ? "+" : "";
  return (sign + fixedDigits(x, digits)).padStart(width);
}

/** Python's repr() of a float: the shortest round-trip digits, exponent below 1e-4 and from 1e16. */
export function floatRepr(x: number): string {
  if (!Number.isFinite(x)) return Number.isNaN(x) ? "NaN" : x > 0 ? "Infinity" : "-Infinity";
  const sign = isNegative(x) ? "-" : "";
  const [mantissa, exponent] = Math.abs(x).toExponential().split("e");
  const digits = mantissa.replace(".", "");
  const point = Number(exponent) + 1; // digits before the decimal point
  if (point <= -4 || point > 16) {
    const e = point - 1;
    const body = digits.length > 1 ? `${digits[0]}.${digits.slice(1)}` : digits;
    return `${sign}${body}e${e < 0 ? "-" : "+"}${String(Math.abs(e)).padStart(2, "0")}`;
  }
  if (point <= 0) return `${sign}0.${"0".repeat(-point)}${digits}`;
  if (point >= digits.length) return `${sign}${digits}${"0".repeat(point - digits.length)}.0`;
  return `${sign}${digits.slice(0, point)}.${digits.slice(point)}`;
}

/** A number as Python prints it: an integral plain number is an int, anything else a float. */
export function numberRepr(value: number | Float): string {
  if (value instanceof Float) return floatRepr(value.valueOf());
  return Number.isInteger(value) ? String(value === 0 ? 0 : value) : floatRepr(value);
}

function stringRepr(text: string): string {
  // json.dumps escapes everything outside ASCII (ensure_ascii)
  return JSON.stringify(text).replace(/[\u0080-￿]/g, (c) => `\\u${c.charCodeAt(0).toString(16).padStart(4, "0")}`);
}

/** Python's json.dumps(value) (compact, ", " and ": ") or json.dumps(value, indent=n). */
export function dumps(value: Json, indent?: number, depth = 0): string {
  if (value === null) return "null";
  if (typeof value === "boolean") return value ? "true" : "false";
  if (typeof value === "number" || value instanceof Float) return numberRepr(value);
  if (typeof value === "string") return stringRepr(value);
  const entries = Array.isArray(value)
    ? value.map((item) => dumps(item, indent, depth + 1))
    : Object.entries(value).map(([key, item]) => `${stringRepr(key)}: ${dumps(item, indent, depth + 1)}`);
  const [open, close] = Array.isArray(value) ? ["[", "]"] : ["{", "}"];
  if (entries.length === 0) return open + close;
  if (indent === undefined) return open + entries.join(", ") + close;
  const inner = "\n" + " ".repeat(indent * (depth + 1));
  return open + inner + entries.join("," + inner) + "\n" + " ".repeat(indent * depth) + close;
}

/** copy.deepcopy for JSON data (floats are immutable and shared). */
export function deepcopy<T extends Json>(value: T): T {
  if (Array.isArray(value)) return value.map((item) => deepcopy(item)) as T;
  if (value !== null && typeof value === "object" && !(value instanceof Float)) {
    return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, deepcopy(item)])) as T;
  }
  return value;
}

/** Python's float % (the result takes the divisor's sign). */
export function pyMod(a: number, b: number): number {
  const mod = a % b;
  if (mod === 0) return b < 0 ? -0 : 0;
  return b < 0 !== mod < 0 ? mod + b : mod;
}

/** json.load: numbers written with a point or an exponent come back as {@link Float}s. */
export function parseJson(text: string): Json {
  return JSON.parse(text, function (_key, value, context?: { source?: string }) {
    if (typeof value === "number" && context?.source && /[.eE]/.test(context.source)) return new Float(value);
    return value;
  });
}
