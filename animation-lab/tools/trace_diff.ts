#!/usr/bin/env bun
/**
 * Prints, frame by frame, the bones where a KUMO trace deviates from its golden.
 *
 * usage: trace_diff.ts <entity>/<scenario> [bone ...] [--from N] [--to N] [--key r|rt] [--thresh deg] [--vec v|vt]
 *
 *   --key r     compare the smoothed rotations (default); rt: the targets the animation wrote
 *   --vec v     compare the smoothed offset vectors instead (vt: their targets)
 *
 * Reads golden/<scenario>.json.gz and build/kumo-traces/<scenario>.json.gz (written by `gradle compare`).
 * Angles print as "<angle>@[axis]".
 */
import { readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { gunzipSync } from "node:zlib";

type Bone = Record<string, number[]>;
type Trace = { frames: { tick: number; bones: Record<string, Bone> }[] };

const root = dirname(import.meta.dir);
const args = process.argv.slice(2);
const scen = args.shift();
if (scen === undefined) {
  console.error("usage: trace_diff.ts <entity>/<scenario> [bone ...] [--from N] [--to N] [--key r|rt] [--thresh deg] [--vec v|vt]");
  process.exit(2);
}
const opts: Record<string, string | number | null> = { from: 0, to: 1e9, key: "r", thresh: 0.1, vec: null };
const bones: string[] = [];
while (args.length > 0) {
  const a = args.shift()!;
  if (a.startsWith("--")) {
    const k = a.slice(2);
    const v = args.shift()!;
    opts[k] = k === "thresh" ? parseFloat(v) : k === "from" || k === "to" ? parseInt(v, 10) : v;
  } else {
    bones.push(a);
  }
}
const load = (path: string) => JSON.parse(gunzipSync(readFileSync(path)).toString("utf8")) as Trace;
const g = load(`${root}/golden/${scen}.json.gz`);
const k = load(join(root, "build", "kumo-traces", `${scen}.json.gz`));

const degrees = (rad: number) => rad * (180 / Math.PI);

function ang(a: number[], b: number[]): number {
  const d = Math.sqrt(a.reduce((sum, x, i) => sum + (x - b[i]) ** 2, 0));
  const s = Math.sqrt(a.reduce((sum, x, i) => sum + (x + b[i]) ** 2, 0));
  return degrees(2 * Math.atan2(Math.min(d, s), Math.max(d, s)));
}

function euler(q: number[]): string {
  const [x, y, z, w] = q;
  // ZYX-ish readout: just print quaternion + axis-angle for readability
  const n = Math.sqrt(x * x + y * y + z * z);
  if (n < 1e-9) return "id";
  const f = (v: number) => (v < 0 ? "" : "+") + v.toFixed(2);
  return `${degrees(2 * Math.atan2(n, w)).toFixed(1).padStart(6)}@[${f(x / n)},${f(y / n)},${f(z / n)}]`;
}

const lines: string[] = [];
const key = opts.key as string;
const frames = Math.min(g.frames.length, k.frames.length);
for (let i = 0; i < frames; i++) {
  const fg = g.frames[i];
  const fk = k.frames[i];
  if (i < (opts.from as number) || i > (opts.to as number)) continue;
  const out: string[] = [];
  const names = bones.length > 0 ? bones : Object.keys(fg.bones).sort();
  for (const b of names) {
    const bg = fg.bones[b];
    const bk = fk.bones[b];
    if (bg === undefined || bk === undefined) continue;
    if (opts.vec) {
      const v = opts.vec as string;
      if (v in bg && v in bk) {
        const [vg, vk] = [bg[v], bk[v]];
        const differs = vg.length !== vk.length || vg.some((x, j) => x !== vk[j]);
        if (differs) {
          const dv = Math.max(...vg.slice(0, vk.length).map((x, j) => Math.abs(x - vk[j])));
          if (dv > 0.01) out.push(`${b}.${v} ref=${JSON.stringify(vg)} kumo=${JSON.stringify(vk)}`);
        }
      }
      continue;
    }
    if (!(key in bg)) continue;
    const e = ang(bg[key], bk[key]);
    if (e > (opts.thresh as number)) {
      out.push(`${b}: ${e.toFixed(2).padStart(5)} ref=${euler(bg[key])} kumo=${euler(bk[key])}`);
    }
  }
  if (out.length > 0) {
    lines.push(`-- frame ${i} tick ${fg.tick.toFixed(2)}`);
    for (const o of out) lines.push(`    ${o}`);
  }
}

// a closed pipe (e.g. `| head`) ends the output quietly
process.stdout.on("error", (error: NodeJS.ErrnoException) => process.exit(error.code === "EPIPE" ? 0 : 1));
process.stdout.write(lines.length > 0 ? lines.join("\n") + "\n" : "");
