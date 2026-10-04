#!/usr/bin/env bun
/**
 * Rewrites animators from bare names looked up through enclosing scopes (named expressions, node
 * and layer variables, the subject's values) to scoped names and definitions (`@define`, `@on`),
 * statically, the way the old lookup resolved them. A migration aid: the generator runs it as a
 * last pass until its builders write the format directly, and it converts hand-written files:
 *
 *   bun tools/kumo_scopes.ts <file.json>...   (rewrites them in place)
 *   bun tools/kumo_scopes.ts --stdin          (one animator on stdin, the result on stdout)
 */
import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { join } from "node:path";

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Obj = { [key: string]: any };

const BUILTINS = new Set(["elapsed", "duration", "nodeTicksElapsed", "layerTicksElapsed", "nodeFadeProgress", "nodeIsFadingIn", "nodeIsActive",
  "nodeIsFadingOut", "nodeIsFinished", "clipLength", "clipDuration"]);
const STEP_TURN_OUTPUTS = ["turnLag", "turnSpeed", "stepLift", "stepImpact", "stride"];
/** Each pose item's fields that are expressions. */
const EXPRESSION_FIELDS: Record<string, string[]> = {
  "core:clip": ["frame", "weight"],
  "core:axis_rotate": ["angle"],
  "core:vector": ["x", "y", "z"],
  "core:offset": ["x", "y", "z"],
  "core:accumulate": ["rate"],
  "core:spring": ["target"],
  "core:step_turn": ["weight"],
  "core:set": ["value"],
  "mobends:spider_idle_legs": ["groundLevel", "bodyX", "bodyZ"],
  "mobends:spider_moving_legs": ["groundLevel", "swing"],
};
const SPIDER = new Set(["mobends:spider_idle_legs", "mobends:spider_moving_legs"]);

const isObj = (v: unknown): v is Obj => v !== null && typeof v === "object" && !Array.isArray(v);
const kindOf = (o: Obj): string => Object.keys(o).find((k) => !k.startsWith("@"))!;

interface Place {
  animator: Set<string>;
  layer?: { exprs: Set<string>; vars: Set<string> };
  machines: Set<string>[];
  node?: { exprs: Set<string>; vars: Set<string> };
}

function resolve(name: string, place: Place): string {
  if (name.includes(".") || BUILTINS.has(name)) return name;
  if (place.node?.exprs.has(name)) return `node.${name}`;
  for (let i = place.machines.length - 1; i >= 0; i--) {
    if (place.machines[i].has(name)) {
      if (i !== place.machines.length - 1) throw new Error(`'${name}' is declared by an outer machine: move it to the layer`);
      return `machine.${name}`;
    }
  }
  if (place.layer?.exprs.has(name)) return `layer.${name}`;
  if (place.animator.has(name)) return `animator.${name}`;
  if (place.node?.vars.has(name)) return `node.${name}`;
  if (place.layer?.vars.has(name)) return `layer.${name}`;
  return name;
}

/** Rewrites the names in an expression. */
function expr(e: unknown, place: Place): unknown {
  if (typeof e === "string") return resolve(e, place);
  if (Array.isArray(e)) return e.map((x) => expr(x, place));
  if (isObj(e) && Object.keys(e).length === 1 && Array.isArray(Object.values(e)[0])) {
    const [op, args] = Object.entries(e)[0];
    // A registered operation's arguments are strings written out (an item id, a hand).
    if (op.includes(":")) return e;
    return { [op]: (args as unknown[]).map((x) => expr(x, place)) };
  }
  return e;
}

/** Damping maps: a value that is a string or an operation is an expression. */
function damping(d: unknown, place: Place): unknown {
  if (!isObj(d)) return d;
  return Object.fromEntries(Object.entries(d).map(([bone, v]) => [bone, typeof v === "string" || isObj(v) ? expr(v, place) : v]));
}

/** Every name an expression reads, before rewriting. */
function namesIn(e: unknown, into: Set<string>): void {
  if (typeof e === "string") into.add(e);
  else if (Array.isArray(e)) e.forEach((x) => namesIn(x, into));
  else if (isObj(e) && Object.keys(e).length === 1 && Array.isArray(Object.values(e)[0])) {
    const [op, args] = Object.entries(e)[0];
    if (!op.includes(":")) (args as unknown[]).forEach((x) => namesIn(x, into));
  }
}

function itemNames(item: Obj, into: Set<string>): void {
  const kind = kindOf(item);
  for (const field of EXPRESSION_FIELDS[kind] ?? []) if (field in item[kind]) namesIn(item[kind][field], into);
  if ("@when" in item) namesIn(item["@when"], into);
}

function statement(target: string, value: unknown, when?: unknown): Obj {
  return when === undefined ? { set: [target, value] } : { "@when": when, set: [target, value] };
}

function run(set: Obj | undefined, place: Place): Obj[] | undefined {
  if (!set) return undefined;
  return Object.entries(set).map(([name, value]) => statement(resolve(name, place), value));
}

function connection(c: Obj, place: Place): Obj {
  const { when, then, set, ...rest } = c;
  const out: Obj = { when: expr(when, place), then, ...rest };
  const statements = run(set, place);
  if (statements) out.do = statements;
  return out;
}

function branch(b: Obj, place: Place): Obj {
  const { when, then, set, ...rest } = b;
  const out: Obj = {};
  if (when !== undefined) out.when = expr(when, place);
  out.then = Array.isArray(then) ? then.map((x: Obj) => branch(x, place)) : then;
  Object.assign(out, rest);
  const statements = run(set, place);
  if (statements) out.do = statements;
  return out;
}

function item(i: Obj, place: Place): Obj {
  const kind = kindOf(i);
  let own: Obj = { ...i[kind] };
  for (const field of EXPRESSION_FIELDS[kind] ?? []) if (field in own) own[field] = expr(own[field], place);
  if (kind === "core:accumulate" || kind === "core:spring") {
    const { name, initial, ...rest } = own;
    own = { inout: `node.${name}`, ...rest };
  }
  const out: Obj = {};
  for (const [k, v] of Object.entries(i)) {
    if (k === kind) out[k] = own;
    else if (k === "@when") out[k] = expr(v, place);
    else if (k === "@damping") out[k] = damping(v, place);
    else out[k] = v;
  }
  return out;
}

function node(n: Obj, layer: NonNullable<Place["layer"]>, machines: Set<string>[], animator: Set<string>, layerVars: Set<string>): Obj {
  const type = kindOf(n);
  const content: Obj = n[type];
  const pose: Obj[] = content.pose ?? [];
  const items = [...pose, ...(content.enterPose ?? [])];
  const exprs = new Set(Object.keys(n["@expressions"] ?? {}));

  // The node's variables: what its drivers write, with their initial values.
  const states: Record<string, unknown> = {};
  const read = new Set<string>();
  for (const i of items) itemNames(i, read);
  for (const c of n["@connections"] ?? []) namesIn(c.when, read);
  for (const e of Object.values(n["@expressions"] ?? {})) namesIn(e, read);
  for (const i of items) {
    const kind = kindOf(i);
    const own = i[kind];
    if (kind === "core:accumulate" || kind === "core:spring") states[own.name] = own.initial ?? 0;
    if (kind === "core:set" && own.scope === "node") states[own.variable] = 0;
    if (kind === "core:step_turn") for (const o of STEP_TURN_OUTPUTS) if (read.has(o)) states[o] = 0;
    if (SPIDER.has(kind) && read.has("groundLevel")) states.groundLevel = 0;
  }
  const place: Place = { animator, layer, machines, node: { exprs, vars: new Set(Object.keys(states)) } };

  const update: Obj[] = [];
  const convert = (list: Obj[]) => list.filter((i) => {
    const kind = kindOf(i);
    if (kind !== "core:set") return true;
    const own = i[kind];
    const target = own.scope === "node" ? `node.${own.variable}` : `layer.${own.variable}`;
    update.push(statement(target, expr(own.value, place), "@when" in i ? expr(i["@when"], place) : undefined));
    return false;
  }).map((i) => {
    const out = item(i, place);
    const kind = kindOf(out);
    if (kind === "core:step_turn") {
      const outputs = STEP_TURN_OUTPUTS.filter((o) => o in states);
      if (outputs.length) out[kind].out = Object.fromEntries(outputs.map((o) => [o, `node.${o}`]));
    }
    if (SPIDER.has(kind)) {
      if ("groundLevel" in states) out[kind].out = { groundLevel: "node.groundLevel" };
      const reset = out[kind].resetVariable ?? "resetLimbs";
      delete out[kind].resetVariable;
      if (layerVars.has(reset)) out[kind].reset = `layer.${reset}`;
    }
    return out;
  });

  const newContent: Obj = { ...content };
  if (content.pose) newContent.pose = convert(content.pose);
  if (content.enterPose) newContent.enterPose = convert(content.enterPose);
  if (content.damping) newContent.damping = damping(content.damping, place);

  const out: Obj = {};
  if ("@comment" in n) out["@comment"] = n["@comment"];
  out[type] = newContent;
  const define: Obj = {};
  for (const [name, initial] of Object.entries(states)) define[name] = { state: initial };
  for (const [name, e] of Object.entries(n["@expressions"] ?? {})) {
    if (name in define) throw new Error(`node ${name} is both a variable and an expression`);
    define[name] = { live: expr(e, place) };
  }
  if (Object.keys(define).length) out["@define"] = define;
  const on: Obj = {};
  const enter = run(n["@set"], place);
  if (enter) on.enter = enter;
  if (update.length) on.update = update;
  if (Object.keys(on).length) out["@on"] = on;
  if (n["@connections"]) out["@connections"] = n["@connections"].map((c: Obj) => connection(c, place));
  if (n["@tags"]) out["@tags"] = n["@tags"];
  return out;
}

/** All the layer variables: declared, set by branches, connections and nodes, or by core:set. */
function layerVariables(layer: Obj): Set<string> {
  const vars = new Set(Object.keys(layer.variables ?? {}));
  const visit = (o: unknown): void => {
    if (Array.isArray(o)) o.forEach(visit);
    else if (isObj(o)) {
      for (const [k, v] of Object.entries(o)) {
        if ((k === "set" || k === "@set") && isObj(v)) Object.keys(v).forEach((n) => vars.add(n));
        if (k === "core:set" && isObj(v) && v.scope !== "node") vars.add(v.variable);
        visit(v);
      }
    }
  };
  visit(layer);
  return vars;
}

function machine(m: Obj, layer: NonNullable<Place["layer"]>, outer: Set<string>[], animator: Set<string>, layerVars: Set<string>, isLayer: boolean): Obj {
  const exprs = isLayer ? new Set<string>() : new Set(Object.keys(m["@expressions"] ?? {}));
  const machines = isLayer ? outer : [...outer, exprs];
  const place: Place = { animator, layer, machines };
  const out: Obj = {};
  for (const [k, v] of Object.entries(m)) {
    if (k === "@expressions" || k === "variables") {
      if (isLayer) continue; // the layer's, written below
      out["@define"] = Object.fromEntries(Object.entries(v as Obj).map(([n, e]) => [n, { live: expr(e, place) }]));
    } else if (k === "select") out.select = v.map((b: Obj) => branch(b, place));
    else if (k === "@connections") out["@connections"] = v.map((c: Obj) => connection(c, place));
    else if (k === "@when") out["@when"] = expr(v, place);
    else if (k === "mirror") out.mirror = Object.fromEntries(Object.entries(v as Obj).map(([mk, mv]) => [mk, mk === "@when" ? expr(mv, place) : mv]));
    else if (k === "damping") out.damping = damping(v, place);
    else if (k === "nodes") out.nodes = Object.fromEntries(Object.entries(v as Obj).map(([n, x]) => [n, node(x, layer, machines, animator, layerVars)]));
    else if (k === "machines") out.machines = Object.fromEntries(Object.entries(v as Obj).map(([n, x]) => [n, machine(x, layer, machines, animator, layerVars, false)]));
    else out[k] = v;
  }
  return out;
}

function layer(l: Obj, animator: Set<string>): Obj {
  const vars = layerVariables(l);
  const exprs = new Set(Object.keys(l["@expressions"] ?? {}));
  const info = { exprs, vars };
  const place: Place = { animator, layer: info, machines: [] };
  const define: Obj = {};
  for (const name of vars) define[name] = { state: l.variables?.[name] ?? 0 };
  for (const [name, e] of Object.entries(l["@expressions"] ?? {})) {
    if (name in define) throw new Error(`layer ${name} is both a variable and an expression`);
    define[name] = { live: expr(e, place) };
  }
  const converted = machine(l, info, [], animator, vars, true);
  // @define goes where the layer's expressions or variables were, or before its select / nodes.
  const out: Obj = {};
  let placed = false;
  for (const [k, v] of Object.entries(converted)) {
    if (!placed && Object.keys(define).length && ["select", "nodes", "machines", "@connections", "defaultOnEntry"].includes(k)) {
      out["@define"] = define;
      placed = true;
    }
    out[k] = v;
  }
  if (!placed && Object.keys(define).length) out["@define"] = define;
  return out;
}

/** The names an animator (and those it extends) declares at its root. */
function animatorNames(doc: Obj, load: (key: string) => Obj | null): Set<string> {
  const names = new Set<string>([...Object.keys(doc["@expressions"] ?? {}), ...Object.keys(doc["@define"] ?? {})]);
  if (doc.extends) {
    const parent = load(doc.extends);
    if (parent) for (const n of animatorNames(parent, load)) names.add(n);
  }
  return names;
}

export function toScopes(doc: Obj, load: (key: string) => Obj | null = () => null): Obj {
  const animator = animatorNames(doc, load);
  const place: Place = { animator, machines: [] };
  const out: Obj = {};
  for (const [k, v] of Object.entries(doc)) {
    if (k === "@expressions") out["@define"] = Object.fromEntries(Object.entries(v as Obj).map(([n, e]) => [n, { live: expr(e, place) }]));
    else if (k === "layers") out.layers = v.map((l: Obj) => layer(l, animator));
    else out[k] = v;
  }
  return out;
}

if (import.meta.main) {
  const RES = join(import.meta.dir, "..", "..", "src", "main", "resources", "assets");
  const load = (key: string): Obj | null => {
    const [ns, path] = key.split(":");
    const file = join(RES, ns, path);
    return existsSync(file) ? JSON.parse(readFileSync(file, "utf8")) : null;
  };
  const { pretty } = await import("./kumo_format");
  const args = process.argv.slice(2);
  if (args[0] === "--stdin") {
    const doc = JSON.parse(readFileSync(0, "utf8"));
    process.stdout.write(JSON.stringify(toScopes(doc, load)));
  } else {
    for (const file of args) {
      writeFileSync(file, pretty(toScopes(JSON.parse(readFileSync(file, "utf8")), load)) + "\n");
      console.log("converted", file);
    }
  }
}
