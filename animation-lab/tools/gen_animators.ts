#!/usr/bin/env bun
/**
 * Generates the animator JSON files (biped, zombie, skeleton, pig zombie, player, squid, spider and
 * most of the mobs described by model definitions) and the hand-authored clips they use. The iron
 * golem's and the creeper's animators and clips, and the cow's animator, are edited by hand
 * instead.
 *
 * The animators are plain data; this script only spares us from writing the shared structure by
 * hand and from computing the quaternions of hand-authored constant poses. Run it through
 * `gradle generateAnimators` (or directly with the mod's resources dir as the argument).
 */
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { pretty } from "./kumo_format";

// Animator data is schemaless JSON built up and patched in place, like the files it becomes.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type Obj = { [key: string]: any };
type Axis = "X" | "Y" | "Z";
type Quaternion = number[];

const RES = process.argv[2] ?? join(import.meta.dir, "..", "..", "src", "main", "resources");
const ANIM = join(RES, "assets", "mobends", "bends", "animators");
const CLIPS = join(RES, "assets", "mobends", "bends", "animations");
mkdirSync(ANIM, { recursive: true });

const radians = (deg: number) => deg * (Math.PI / 180);
const degrees = (rad: number) => rad * (180 / Math.PI);
const range = (n: number, start = 0) => Array.from({ length: n - start }, (_, i) => start + i);
const round7 = (v: number) => Math.round(v * 1e7) / 1e7;
/** Floored modulo: the result takes the divisor's sign. */
const mod = (a: number, b: number) => ((a % b) + b) % b;

function clipKey(folder: string, name: string): string {
  return `mobends:bends/animations/${folder}/${name}.json`;
}

function writeClip(path: string, data: Obj): void {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, JSON.stringify(data));
}

// ---- condition helpers ----------------------------------------------------------------------
/** A condition: a boolean expression, or a boolean name. */
type Cond = Obj | string;
const COMPARISONS: Record<string, string> = { "<": "lt", "<=": "le", ">": "gt", ">=": "ge", "==": "eq", "!=": "ne" };
const cmp = (left: string | number | Obj, op: string, right: string | number | Obj): Obj => ({ [COMPARISONS[op]]: [left, right] });
/** A boolean name: a built-in (entityIsOnGround, ...), a definition (animator.jumping), or a state of the subject. */
const state = (s: string): Cond => s;
const AND = (...conditions: Cond[]): Obj => ({ and: conditions });
const OR = (...conditions: Cond[]): Obj => ({ or: conditions });
const NOT = (condition: Cond): Obj => ({ not: [condition] });

/**
 * When a selector chooses `target`: its branch's condition, the conditions of the branches before
 * it at every level negated. A layer that follows another reads this as a definition instead of
 * the other layer's node. It assumes every list ends with a branch that always holds, so the
 * selector always chooses (and the layer's node is a function of the frame's state).
 */
function chooses(branches: Obj[], target: string): Cond | null {
  const alternatives: Cond[] = [];
  const before: Cond[] = [];
  for (const b of branches) {
    const inner = Array.isArray(b.then) ? chooses(b.then, target) : b.then === target ? true : null;
    if (inner !== null) {
      const parts: Cond[] = before.map((c) => NOT(c));
      if (b.when !== undefined) parts.push(b.when);
      if (inner !== true) parts.push(inner);
      alternatives.push(parts.length === 1 ? parts[0] : AND(...parts));
    }
    if (b.when === undefined) break;
    before.push(b.when);
  }
  return alternatives.length === 0 ? null : alternatives.length === 1 ? alternatives[0] : OR(...alternatives);
}

// ---- values ----------------------------------------------------------------------------------
// A value the procedural bits computed as a variable scaled, offset, clamped, eased and shaped, as an
// expression ({"add": [{"mul": ["entityLimbSwing", 0.6662]}, 3.14]}, see misc/kumo-format.md),
// keeping the order of operations these steps always had.
type ValueOptions = { scale?: number; offset?: number; min?: number; max?: number; ease?: string; power?: number;
                      clampFirst?: boolean; fn?: string; mul?: number; add?: number };
const EASINGS: { [key: string]: string } = { pow: "pow", ease_in: "easeIn", ease_out: "easeOut", ease_in_out: "easeInOut" };

function value(variable: string | Obj, { scale = 1, offset = 0, min, max, ease, power = 2, clampFirst = false, fn, mul = 1, add = 0 }: ValueOptions = {}): unknown {
  const clamp = (e: unknown): unknown =>
    min !== undefined && max !== undefined ? { clamp: [e, min, max] }
      : min !== undefined ? { max: [e, min] }
      : max !== undefined ? { min: [e, max] }
      : e;
  const affine = (e: unknown): unknown => {
    if (scale !== 1) e = { mul: [e, scale] };
    if (offset !== 0) e = { add: [e, offset] };
    return e;
  };
  let e: unknown = variable;
  if (!ease && !clampFirst) {
    // Scale and offset first, then clamp.
    e = clamp(affine(e));
  } else {
    // Clamp the raw variable, shape it, then scale and offset.
    e = clamp(e);
    if (ease) {
      if (!(ease in EASINGS)) throw new Error("unknown easing " + ease);
      e = { [EASINGS[ease]]: [e, power] };
    }
    e = affine(e);
  }
  if (fn) e = { [fn]: [e] };
  if (mul !== 1) e = { mul: [e, mul] };
  if (add !== 0) e = { add: [e, add] };
  return e;
}

// ---- clip frames: where in a clip it is, in the clip's units (see misc/kumo-format.md, "Clips") ----
/** Wraps a frame around the clip, for the clips that loop. */
const looped = (frame: unknown): Obj => ({ mod: [frame, "clipLength"] });
const scaled = (variable: string, scale: number): Obj => ({ mul: [variable, scale] });

// ---- pose items: one key naming what it is, its fields, then its modifiers (misc/kumo-format.md) ----
/** An item's modifiers, written without their "@" ({space: "pre"} is "@space"); "@when" goes first. */
type Mods = { when?: Cond; space?: string; damping?: Obj; snap?: boolean; mirror?: boolean; swapSides?: boolean; vectorModes?: Obj };

function item(kind: string, fields: Obj, mods: Mods = {}): Obj {
  const out: Obj = {};
  if (mods.when !== undefined) out["@when"] = mods.when;
  out[kind] = fields;
  for (const [k, v] of Object.entries(mods)) if (k !== "when") out["@" + k] = v;
  return out;
}

/** {"core:clip": {"animationKey": ..., ...fields}} */
const clip = (animationKey: string, fields: Obj = {}, mods: Mods = {}): Obj => item("core:clip", { animationKey, ...fields }, mods);

function withDamping(it: Obj, damping: Obj): Obj {
  return { ...it, "@damping": { ...damping } };
}

/** The item, applying while {@code cond} holds. */
function when(it: Obj, cond: Cond): Obj {
  const { "@when": _, ...rest } = it;
  return { "@when": cond, ...rest };
}

const mirrored = (it: Obj): Obj => ({ ...it, "@mirror": true });
const swapped = (it: Obj): Obj => ({ ...it, "@swapSides": true });
const withSnap = (it: Obj): Obj => ({ ...it, "@snap": true });

// ---- nodes and scopes: definitions, statements, connections (misc/kumo-format.md, "Definitions and statements") ----
const live = (e: unknown): Obj => ({ live: e });
/** A state, {@code initial} when its scope starts; statements and drivers change it. */
const variable = (initial: unknown = 0): Obj => ({ state: initial });
/** {"set": [target, value]}, with its "@when". */
function set(target: string, to: unknown, cond?: Cond): Obj {
  return cond === undefined ? { set: [target, to] } : { "@when": cond, set: [target, to] };
}
/** {"when": ..., "then": ...}: a connection, and the statements it runs when it moves the layer. */
function conn(then: string, cond: Cond, run: Obj[] = []): Obj {
  return run.length ? { when: cond, then, do: run } : { when: cond, then };
}

type Scope = { define?: Obj; on?: Obj; connections?: Obj[] };
/** {"core:pose": {pose, enterPose}}, with its scope's definitions, statement lists and connections. */
function poseNode(pose: Obj[], { enterPose, define, on, connections }: { enterPose?: Obj[] } & Scope = {}): Obj {
  const out: Obj = { "core:pose": enterPose ? { pose, enterPose } : { pose } };
  if (define) out["@define"] = define;
  if (on) out["@on"] = on;
  if (connections) out["@connections"] = connections;
  return out;
}
const poseOf = (node: Obj): Obj[] => node["core:pose"].pose;

// ---- quaternion helpers for hand-authored constant poses -------------------------------------
function axis(a: Axis, deg: number): number[] {
  const h = radians(deg) / 2;
  const [x, y, z] = { X: [1, 0, 0], Y: [0, 1, 0], Z: [0, 0, 1] }[a];
  const s = Math.sin(h);
  return [x * s, y * s, z * s, Math.cos(h)];
}

function mul(a: number[], b: number[]): number[] {
  const [ax, ay, az, aw] = a;
  const [bx, by, bz, bw] = b;
  return [ax * bw + aw * bx + ay * bz - az * by,
          ay * bw + aw * by + az * bx - ax * bz,
          az * bw + aw * bz + ax * by - ay * bx,
          aw * bw - ax * bx - ay * by - az * bz];
}

/** rotate*(a).rotate*(b)... in the procedural order: each call pre-multiplies (q = R * q). */
function rotations(...calls: [Axis, number][]): Quaternion {
  let q = [0, 0, 0, 1];
  for (const [a, deg] of calls) {
    q = mul(axis(a, deg), q);
  }
  return calls.length === 0 ? q : q.map((v) => round7(v));
}

function poseClip(path: string, bones: Record<string, Quaternion>, vectors: Record<string, number[]> = {}): void {
  const data: Obj = { bones: {}, duration: 0 };
  for (const [bone, q] of Object.entries(bones)) {
    data.bones[bone] = { keyframes: [{ position: [0, 0, 0], rotation: q }] };
  }
  for (const [bone, v] of Object.entries(vectors)) {
    data.bones[bone] = { keyframes: [{ position: v, rotation: [0, 0, 0, 1] }] };
  }
  writeClip(path, data);
}

// ---- shared biped locomotion ------------------------------------------------------------------
const B = (n: string) => clipKey("biped", n);
const jumping = OR(NOT(state("entityIsOnGround")), cmp("entityTicksAfterTouchdown", "<", 1));
/** A new jump while still in the air (a bounce): the jump starts over. */
const bounced = AND(cmp("entityPrevMotionY", "<", 0), cmp("entityMotionY", ">", 0));
/** stand / walk / jump: the layer's selector over the nodes of those names (animator.jumping is the animator's definition). */
const locomotionSelect: Obj[] = [
  { when: "animator.jumping", then: "jump" },
  { when: state("entityIsStandingStill"), then: "stand" },
  { then: "walk" },
];
const limbFrame = looped(scaled("entityLimbSwing", 0.6662));
const headLook: Obj[] = [
  item("core:axis_rotate", { bone: "head", axis: "y", angle: "entityHeadYaw" }, { space: "pre" }),
  item("core:axis_rotate", { bone: "head", axis: "x", angle: "entityHeadPitch" }, { space: "post" }),
];
const kneel = clip(B("kneel"), { frame: "entityTicksAfterTouchdown" }, { when: cmp("entityTicksAfterTouchdown", "<", 1 / 0.15), damping: { body: 1 }, vectorModes: { root: "snap" } });
const resetDamping = { root: 0.3, localOffset: 0.3, renderRotation: 0.3, centerRotation: 0.3,
                       rightHeldItem: 0.3, leftHeldItem: 0.3 };

const stand: Obj = poseNode([
    clip(B("stand"), { frame: looped(scaled("ticks", 0.1)) }, { damping: { ...resetDamping, body: 1, rightArm: 0.4, leftArm: 0.4 }, vectorModes: { root: "slide", localOffset: "slide" } }),
    ...headLook,
    kneel,
  ], { enterPose: [clip(B("stand_enter"), {}, { when: cmp("entityTicksAfterTouchdown", "<", 0.5 / 0.15) })] });
const walk: Obj = poseNode([
    clip(B("walk_base"), { frame: limbFrame }, { damping: { ...resetDamping, body: 0.5, head: 0.5, rightArm: 0.8, leftArm: 0.8, rightForeArm: 0.8, leftForeArm: 0.8,
                 rightLeg: 1, leftLeg: 1, root: [0.3, 0.6, 0.3] }, vectorModes: { root: "retarget", localOffset: "slide" } }),
    clip(B("walk_forelegs"), { frame: limbFrame }, { damping: { leftForeLeg: 0.5, rightForeLeg: 0.5 } }),
    clip(B("walk_swing"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { space: "post" }),
    item("core:axis_rotate", { bone: "body", axis: "z", angle: value("entityHeadYaw", { scale: -0.1, min: -10, max: 10 }) }, { space: "pre" }),
    ...headLook,
    kneel,
  ]);
const jump: Obj = poseNode([
    clip(B("jump"), { frame: "entityTicksInAir" }, { damping: { ...resetDamping, centerRotation: 0.7, body: 0.2, rightArm: 0.05, leftArm: 0.05, rightForeArm: 0.3, leftForeArm: 0.3 }, vectorModes: { root: "slide" } }),
    ...headLook,
    clip(B("jump_moving_base"), { frame: limbFrame }, { when: NOT(state("entityIsStandingStill")), damping: { rightLeg: 1, leftLeg: 1, leftForeArm: 0.3, rightForeArm: 0.3 } }),
    clip(B("jump_moving_forelegs"), { frame: limbFrame }, { when: NOT(state("entityIsStandingStill")), damping: { leftForeLeg: 0.3, rightForeLeg: 0.3 } }),
    clip(B("jump_moving_swing"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { space: "post", when: NOT(state("entityIsStandingStill")) }),
    clip(B("jump_still"), {}, { when: state("entityIsStandingStill"), damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3 } }),
  ], { enterPose: [clip(B("jump_enter"))], connections: [conn("jump", "animator.bounced")] });

// The layers of the animators extending it follow the locomotion through these, not its nodes.
const biped: Obj = {
  formatVersion: 2,
  "@define": { jumping: live(jumping), bounced: live(bounced), standing: live(chooses(locomotionSelect, "stand")),
               walking: live(chooses(locomotionSelect, "walk")), canSpinAttack: live(true) },
  layers: [
    { select: locomotionSelect, nodes: { stand, walk, jump } },
  ] };

// ---- zombie: animation sets -----------------------------------------------------------------------
const Z = (n: string) => clipKey("zombie", n);
const zombie: Obj = {
  formatVersion: 2,
  extends: "mobends:bends/animators/biped.json",
  layers: [
    { mode: "additive", additiveSpace: { "@default": "pre", body: "post", root: "override" },
      "@when": cmp("entity.animationSet", "==", 0), defaultOnEntry: "lean", nodes: { lean: poseNode([
          clip(Z("lean"), {}, { damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }),
          clip(Z("lean_arms_up"), {}, { space: "override", when: AND(NOT(state("entityIsStandingStill")), cmp("entity.walkingState", "==", 1)) }),
        ]) } },
    { "@when": cmp("entity.animationSet", "==", 1), defaultOnEntry: "stumble", nodes: { stumble: poseNode([
          clip(Z("stumble_base"), { frame: limbFrame }, { damping: { rightLeg: 1, leftLeg: 1, rightArm: 1, leftArm: 1, body: 0.5 } }),
          clip(Z("stumble_swing"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { space: "post" }),
          clip(Z("stumble_head"), { frame: limbFrame }, { space: "pre" }),
        ]) } },
  ] };

// ---- skeleton: strafing legs ------------------------------------------------------------------------
const S = (n: string) => clipKey("skeleton", n);
const skeleton: Obj = {
  formatVersion: 2,
  extends: "mobends:bends/animators/biped.json",
  layers: [
    { "@when": AND("animator.walking", state("entityIsStrafing")), defaultOnEntry: "strafe", nodes: { strafe: poseNode([
          clip(S("strafe_base"), { frame: limbFrame }, { damping: { rightLeg: 1, leftLeg: 1 } }),
          clip(S("strafe_swing"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { space: "post" }),
        ]) } },
  ] };

// ---- pig zombie: hunched pose + slash attack -------------------------------------------------------
const P = (n: string) => clipKey("pigzombie", n);
// The constant pose is the rotate* / localRotate* calls of the pig zombie stand/walk bits.
poseClip(join(CLIPS, "pigzombie", "pose_post.json"), { body: rotations(["X", 20]) });
poseClip(join(CLIPS, "pigzombie", "pose_pre.json"), {
  body: rotations(["Z", -10]),
  head: rotations(["X", -20]),
  rightArm: rotations(["X", -20], ["Z", 10]),
  leftArm: rotations(["X", -20], ["Z", 10]),
  rightLeg: rotations(["Z", 10], ["X", -30]),
  leftLeg: rotations(["Z", -10], ["X", -10], ["Y", -10]),
  rightForeLeg: rotations(["X", 25]),
  leftForeLeg: rotations(["X", 25]),
});
poseClip(join(CLIPS, "pigzombie", "stand_offset.json"), {}, { root: [0, -3, 0] });
const pigZombie: Obj = {
  formatVersion: 2,
  extends: "mobends:bends/animators/biped.json",
  layers: [
    { mode: "additive", additiveSpace: { "@default": "pre", root: "override" },
      "@when": OR("animator.standing", "animator.walking"), defaultOnEntry: "hunch", nodes: { hunch: poseNode([
          clip(P("pose_post"), {}, { space: "post" }),
          clip(P("pose_pre"), {}, { space: "pre" }),
          clip(P("stand_offset"), {}, { when: "animator.standing", damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }),
          clip(P("walk_bob"), { frame: limbFrame }, { when: "animator.walking", damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }),
        ]) } },
    { "@when": cmp("entitySwingProgress", ">", 0), defaultOnEntry: "slash",
      mirror: { "@when": state("entityIsLeftHanded"),
                pairs: [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                        ["leftHeldItem", "rightHeldItem"]] },
      nodes: { slash: poseNode([
          clip(P("slash"), { frame: "entityTicksAfterAttack" }, { mirror: true, damping: { body: 0.9, head: 0.9, rightArm: 0.9, leftArm: 0.3, rightForeArm: 0.3, leftForeArm: 0.3, localOffset: 0.3 }, vectorModes: { localOffset: "slide" } }),
          // the head looks where the zombie looks, whichever hand it uses: not mirrored
          headLook[0], mirrored(headLook[1]),
          // the still-standing legs are the same for both hands; the render rotation is not
          clip(P("slash_still"), { bones: ["leftLeg", "rightLeg", "rightForeLeg", "root"] }, { when: AND(state("entityIsStandingStill"), NOT(state("entityIsRiding"))), damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }),
          clip(P("slash_still"), { bones: ["renderRotation"] }, { mirror: true, when: AND(state("entityIsStandingStill"), NOT(state("entityIsRiding"))), damping: { renderRotation: 0.3 } }),
          item("core:axis_rotate", { bone: "rightHeldItem", axis: "x", angle: 50 }, { space: "override", snap: true, mirror: true, damping: { rightHeldItem: 0.9 } }),
        ]) } },
  ] };


// ---- player ----------------------------------------------------------------------------------------
const PL = (n: string) => clipKey("player", n);

/** Minecraft's table-based cosine, so analytic clips match the procedural output exactly. */
function mcCos(v: number): number {
  const i = (Math.trunc(v * 10430.378) + 16384) & 65535;
  return Math.sin(i * Math.PI * 2 / 65536);
}

/** Generates a clip meant to loop (its last keyframe is its first) from a function phase -> {bone: quaternion}. */
function cycleClip(path: string, boneFn: (phase: number) => Record<string, Quaternion>, count = 64, period = 2 * Math.PI): void {
  const frames = range(count + 1).map((i) => boneFn(i < count ? period * i / count : 0.0));
  const data: Obj = { bones: {}, duration: period };
  for (const bone of Object.keys(frames[0])) {
    data.bones[bone] = { keyframes: frames.map((f) => ({ position: [0, 0, 0], rotation: f[bone] })) };
  }
  writeClip(path, data);
}

// The underwater arm pose is Rx(c) * Ry(-90 t) * Rx(a) with a, c on the tick clock and t a ramp;
// the two clock-driven parts are generated here so the ramp can sit between them.
const armSway = (phase: number) => (mcCos(phase) + 1) / 2;
cycleClip(join(CLIPS, "player", "swim_deep_arm_inner.json"),
          (p) => ({ leftArm: rotations(["X", armSway(p) * -120]), rightArm: rotations(["X", armSway(p) * -120]) }));
cycleClip(join(CLIPS, "player", "swim_deep_arm_outer.json"),
          (p) => ({ leftArm: rotations(["X", armSway(p) * 20]), rightArm: rotations(["X", armSway(p) * 20]) }));

type DrvOptions = ValueOptions & { space?: string; const?: number };

function drv(bone: string, ax: Axis, variable: string | Obj | null = null, { space = "pre", const: constant, ...shape }: DrvOptions = {}): Obj {
  const angle = constant !== undefined ? constant : variable === null ? 0 : value(variable, shape);
  return item("core:axis_rotate", { bone, axis: ax.toLowerCase(), angle }, { space });
}

const resetItems = { rightHeldItem: rotations(), leftHeldItem: rotations() };
poseClip(join(CLIPS, "player", "reset_items.json"), resetItems);
poseClip(join(CLIPS, "player", "torch_forearm_right.json"), { rightForeArm: rotations(["X", -5]) });
poseClip(join(CLIPS, "player", "torch_forearm_left.json"), { leftForeArm: rotations(["X", -5]) });
poseClip(join(CLIPS, "player", "elytra.json"), {
  body: rotations(), leftForeArm: rotations(), rightForeArm: rotations(),
  leftLeg: rotations(["Z", -5]), rightLeg: rotations(["Z", 5]),
  leftForeLeg: rotations(), rightForeLeg: rotations(),
  centerRotation: rotations(), renderRotation: rotations(), head: rotations(),
  leftArm: rotations(), rightArm: rotations() }, { root: [0, 0, 0] });
poseClip(join(CLIPS, "player", "fly_common.json"), { renderRotation: rotations() }, { root: [0, 0, 0] });
poseClip(join(CLIPS, "player", "fly_sprint.json"), {
  leftForeArm: rotations(), rightForeArm: rotations(), leftLeg: rotations(["Z", -5]), rightLeg: rotations(["Z", 5]),
  leftForeLeg: rotations(), rightForeLeg: rotations(), body: rotations(), head: rotations(), leftArm: rotations(), rightArm: rotations(), centerRotation: rotations() });
poseClip(join(CLIPS, "player", "fly_hover_rest.json"), { centerRotation: rotations(), head: rotations() });
poseClip(join(CLIPS, "player", "fly_moving.json"), {
  centerRotation: rotations(), body: rotations(), leftArm: rotations(), rightArm: rotations(),
  leftForeArm: rotations(), rightForeArm: rotations(),
  leftLeg: rotations(["X", -45]), rightLeg: rotations(["X", -6]), leftForeLeg: rotations(["X", 30]), rightForeLeg: rotations(["X", 10]), head: rotations() });
poseClip(join(CLIPS, "player", "falling_head.json"), { head: rotations(["X", -20]) });
poseClip(join(CLIPS, "player", "falling_rest.json"), { body: rotations(), centerRotation: rotations(), head: rotations() });
for (const leg of ["right", "left"]) {
  const m = leg === "right" ? 1 : -1;
  poseClip(join(CLIPS, "player", `sprint_jump_${leg}.json`), {
    body: rotations(["Y", 20 * m]),
    rightLeg: rotations(["Z", 5], ["X", -45 * m]), leftLeg: rotations(["Z", -5], ["X", 45 * m]),
    rightArm: rotations(["Z", 10], ["X", 50 * m]), leftArm: rotations(["Z", -10], ["X", -50 * m]),
    centerRotation: rotations(), head: rotations() }, { root: [0, 0, 0] });
}
poseClip(join(CLIPS, "player", "ladder_rest.json"), { centerRotation: rotations(), head: rotations(), renderRotation: rotations() });
poseClip(join(CLIPS, "player", "ladder_ledge_forearms.json"), { leftForeArm: rotations(["X", -10]), rightForeArm: rotations(["X", -10]) });
poseClip(join(CLIPS, "player", "swim_common.json"), { head: rotations(), renderRotation: rotations() }, { localOffset: [0, 0, 0] });
poseClip(join(CLIPS, "player", "riding_head.json"), { head: rotations() });
poseClip(join(CLIPS, "player", "riding_moving_head.json"), { head: rotations(["X", -25]) });

// --- locomotion nodes shared with bipeds, plus the player's head override while attacking -----------
const attackHead = [
  when(clip(PL("riding_head"), {}, { damping: { head: 0.5 } }), cmp("entityTicksAfterAttack", "<", 10)),
  when(drv("head", "Y", "entityHeadYaw", { space: "pre" }), cmp("entityTicksAfterAttack", "<", 10)),
  when(drv("head", "X", "entityHeadPitch", { space: "post" }), cmp("entityTicksAfterAttack", "<", 10)),
];
const pWalk = structuredClone(walk);
poseOf(pWalk).push(...attackHead);
const sprintFrame = looped(scaled("entityLimbSwing", 0.6662 * 0.8));
const pSprint: Obj = poseNode([
    clip(PL("sprint_base"), { frame: sprintFrame }, { damping: { ...resetDamping, body: 0.8, head: 0.5, rightArm: 0.8, leftArm: 0.8, rightLeg: 1, leftLeg: 1,
                 rightForeLeg: 0.7, leftForeLeg: 0.7, root: [0.1, 0.9, 0.1] }, vectorModes: { root: "retarget", localOffset: "slide" } }),
    clip(PL("sprint_forearms"), { frame: sprintFrame }, { damping: { leftForeArm: 0.3, rightForeArm: 0.3 } }),
    clip(PL("sprint_swing"), { frame: sprintFrame, weight: "entityLimbSwingAmount" }, { space: "post" }),
    drv("body", "Z", "entityHeadYaw", { scale: -0.3, min: -10, max: 10 }),
    ...headLook,
    ...attackHead,
  ]);

/**
 * The PlayerController decision tree: the first state that applies, most important first (see
 * misc/kumo-format.md, "Selectors").
 */
const playerSelect: Obj[] = [
  { when: state("entityIsSleeping"), then: "sleeping" },
  { when: state("entityIsRiding"), then: [{ when: state("entityIsRidingLiving"), then: "riding" }, { then: "sitting" }] },
  { when: cmp("entityTicksElytraFlying", ">", 4), then: "elytra" },
  { when: state("entityIsClimbing"), then: "ladder" },
  { when: state("entityIsInWater"), then: "swimming" },
  { when: "animator.jumping", then: [
    { when: { "core:is_flying": [] }, then: "flying" },
    { when: cmp("entityTicksFalling", ">", 10), then: "falling" },
    // the sprint jump leads with the leg the entity data picked
    { when: state("entityIsSprinting"), then: [{ when: state("entity.sprintJumpLeg"), then: "sprint_jump_right" }, { then: "sprint_jump_left" }] },
    { then: "jump" },
  ] },
  { when: state("entityIsStandingStill"), then: "stand" },
  { when: state("entityIsSprinting"), then: "sprint" },
  { then: "walk" },
];

const pStand = structuredClone(stand);
const pJump = structuredClone(jump);

const pSleeping: Obj = poseNode([
  clip(PL("sleeping"), { frame: looped(scaled("ticks", 0.1)) }, { damping: { ...resetDamping, head: 1, rightArm: 0.4, leftArm: 0.4 }, vectorModes: { root: "slide", localOffset: "slide" } })]);
const pSitting: Obj = poseNode([
  clip(PL("sitting"), {}, { damping: { centerRotation: 0.3, body: 0.5 } }), ...headLook]);
// The rider's measured speed (the mount carries it) above which it rides fast, in blocks per tick:
// a little under a player's walking pace, so a galloping horse, a boat or a minecart at speed.
const RIDING_FAST = 0.2;
const pRiding: Obj = poseNode([
  clip(PL("riding"), {}, { damping: { centerRotation: 0.3, body: 0.5, localOffset: 0.3 }, vectorModes: { localOffset: "slide" } }),
  ...headLook,
  drv("body", "Z", "entityRidingRelativeHeadYaw", { scale: -0.25, min: -20, max: 20, space: "override" }),
  clip(PL("riding_legs"), { frame: { add: ["entityRidingRelativeYaw", 180] } }),
  // the moving clip carries the head as Rx(-25) sampled at zero look; that part is applied in PRE space below
  when(clip(PL("riding_moving"), { bones: ["body", "leftArm", "rightArm", "leftForeArm", "rightForeArm"] }), NOT(state("entityIsStandingStill"))),
  when(clip(PL("riding_moving_head"), {}, { space: "pre" }), AND(NOT(state("entityIsStandingStill")), cmp("entityXZSpeed", "<=", RIDING_FAST))),
  when(clip(PL("riding_fast"), { frame: looped(scaled("ticks", 0.5)) }, { damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }),
       AND(NOT(state("entityIsStandingStill")), cmp("entityXZSpeed", ">", RIDING_FAST))),
]);
// riding_fast carries head as an absolute value (Rx(-body) sampled at zero look); it must compose
// after the look drivers, so it is split: body/arms/root absolute, head PRE.
poseOf(pRiding)[poseOf(pRiding).length - 1] = when(clip(PL("riding_fast"), { bones: ["body", "leftArm", "rightArm", "root"], frame: looped(scaled("ticks", 0.5)) }, { damping: { root: [null, 0.6, null] }, vectorModes: { root: "retarget" } }), AND(NOT(state("entityIsStandingStill")), cmp("entityXZSpeed", ">", RIDING_FAST)));
poseOf(pRiding).push(when(clip(PL("riding_fast"), { bones: ["head"], frame: looped(scaled("ticks", 0.5)) }, { space: "pre" }),
                       AND(NOT(state("entityIsStandingStill")), cmp("entityXZSpeed", ">", RIDING_FAST))));

const pElytra: Obj = poseNode([
  clip(PL("elytra"), {}, { damping: { head: 1, body: 0.7, leftArm: 0.7, rightArm: 0.7, leftForeArm: 0.7, rightForeArm: 0.7,
                                           leftLeg: 0.7, rightLeg: 0.7, leftForeLeg: 0.7, rightForeLeg: 0.7, centerRotation: 1, renderRotation: 0.7, root: 0.7 }, vectorModes: { root: "slide" } }),
  drv("head", "Y", "entityHeadYaw", { space: "override" }), drv("head", "X", null, { const: -90 }),
  drv("leftArm", "Z", null, { const: -60 }), drv("leftArm", "Z", "entity.flightSpeedFactor", { scale: 55 }), drv("leftArm", "Z", { abs: ["entityHeadYaw"] }, { scale: -0.5 }),
  drv("rightArm", "Z", null, { const: 60 }), drv("rightArm", "Z", "entity.flightSpeedFactor", { scale: -55 }), drv("rightArm", "Z", { abs: ["entityHeadYaw"] }, { scale: 0.5 }),
]);

const flySprint = AND(state("entityIsSprinting"), NOT(state("entityIsDrawingBow")), cmp("entityTicksAfterAttack", ">=", 10));
const flyHover = AND(NOT(flySprint), cmp("entitySpeed", "<", 0.1));
const flyMoving = AND(NOT(flySprint), cmp("entitySpeed", ">=", 0.1));
const bodyRotXDrv = (bone: string, sign: number, space: string) =>
  drv(bone, "X", "entityHeadPitch", { scale: 0.8 * sign, min: Math.min(0, -60 * sign), max: Math.max(0, -60 * sign), space });
// the forward and sideways momentum are clamped to [-1, 1] before they are scaled, as the bit did
const momentum: ValueOptions = { min: -1, max: 1, clampFirst: true };
const pFlying: Obj = poseNode([
  clip(PL("fly_common"), {}, { damping: { renderRotation: 0.7, root: 0.7 }, vectorModes: { root: "slide" } }),
  // sprint-flying
  when(clip(PL("fly_sprint"), {}, { damping: { centerRotation: 1, head: 1, body: 0.7, leftArm: 0.7, rightArm: 0.7, leftForeArm: 0.7, rightForeArm: 0.7,
                                                    leftLeg: 0.7, rightLeg: 0.7, leftForeLeg: 0.7, rightForeLeg: 0.7 } }), flySprint),
  when(drv("centerRotation", "X", "entity.flightPitch", { space: "override" }), flySprint), when(drv("centerRotation", "Z", "entityHeadYaw"), flySprint),
  when(bodyRotXDrv("body", 1, "override"), flySprint),
  when(drv("head", "Y", "entityHeadYaw", { space: "override" }), flySprint), when(drv("head", "X", "entityHeadPitch"), flySprint),
  when(bodyRotXDrv("head", -1, "pre"), flySprint), when(drv("head", "X", "entity.flightPitch", { scale: -1 }), flySprint),
  when(bodyRotXDrv("leftArm", -1, "override"), flySprint), when(drv("leftArm", "Z", null, { const: -60 }), flySprint), when(drv("leftArm", "Z", "entity.flightSpeedFactor", { scale: 55 }), flySprint), when(drv("leftArm", "Z", { abs: ["entityHeadYaw"] }, { scale: -0.5 }), flySprint),
  when(bodyRotXDrv("rightArm", -1, "override"), flySprint), when(drv("rightArm", "Z", null, { const: 60 }), flySprint), when(drv("rightArm", "Z", "entity.flightSpeedFactor", { scale: -55 }), flySprint), when(drv("rightArm", "Z", { abs: ["entityHeadYaw"] }, { scale: 0.5 }), flySprint),
  // hovering
  when(clip(PL("fly_hover_arms"), { frame: looped(scaled("ticks", 0.0825)) }, { damping: { leftArm: 0.3, rightArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3 } }), flyHover),
  when(clip(PL("fly_hover_legs"), { frame: looped(scaled("ticks", 0.125)) }, { damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }), flyHover),
  when(clip(PL("fly_hover_rest"), {}, { damping: { head: 1 } }), flyHover),
  when(drv("head", "X", "entityHeadPitch", { space: "override" }), flyHover), when(drv("head", "Y", "entityHeadYaw"), flyHover),
  // moving
  when(clip(PL("fly_moving"), {}, { damping: { head: 1 } }), flyMoving),
  when(drv("centerRotation", "X", "entityForwardMomentum", { ...momentum, scale: 50 }), flyMoving),
  when(drv("leftArm", "X", "entityForwardMomentum", { ...momentum, scale: 90, space: "override" }), flyMoving), when(drv("leftArm", "Z", "entitySidewaysMomentum", { ...momentum, scale: -80, offset: -20, space: "post" }), flyMoving),
  when(drv("rightArm", "X", "entityForwardMomentum", { ...momentum, scale: 90, space: "override" }), flyMoving), when(drv("rightArm", "Z", "entitySidewaysMomentum", { ...momentum, scale: -80, offset: 20, space: "post" }), flyMoving),
  when(drv("leftLeg", "Z", "entitySidewaysMomentum", { ...momentum, scale: -40, offset: -5, space: "post" }), flyMoving),
  when(drv("rightLeg", "Z", "entitySidewaysMomentum", { ...momentum, scale: -40, offset: 5, space: "post" }), flyMoving),
  when(drv("head", "X", "entityHeadPitch", { space: "override" }), flyMoving), when(drv("head", "X", "entityForwardMomentum", { ...momentum, scale: -50 }), flyMoving),
  when(drv("centerRotation", "Y", "entityHeadYaw", { scale: -1, space: "post" }), AND(flyMoving, NOT(state("entityIsDrawingBow")))),
]);

const fallDamp = value("entityTicksFalling", { scale: 0.9 / 80, offset: -0.9 * 10 / 80, min: 0, max: 0.9 });
const fallBones = ["leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "renderRotation"];
const pFalling: Obj = poseNode([
  clip(PL("falling_rest"), {}, { damping: { centerRotation: 0.3, body: 0.5 } }),
  drv("head", "X", "entityHeadPitch", { space: "override" }), drv("head", "Y", "entityHeadYaw"),
  clip(PL("falling"), { frame: looped(scaled("ticks", 0.5)) }, { damping: Object.fromEntries(fallBones.map((b) => [b, fallDamp])) }),
  clip(PL("falling_head"), {}, { space: "pre", damping: { head: fallDamp } }),
]);

function sprintJumpNode(leg: string): Obj {
  const m = leg === "right" ? 1 : -1;
  const [mainFl, offFl] = leg === "right" ? ["rightForeLeg", "leftForeLeg"] : ["leftForeLeg", "rightForeLeg"];
  return poseNode([
    clip(PL(`sprint_jump_${leg}`), {}, { damping: { centerRotation: 0.3, root: 0.5, body: 0.3, rightLeg: 0.8, leftLeg: 0.8, rightArm: 0.3, leftArm: 0.3 }, vectorModes: { root: "slide" } }),
    // body lean from the vertical motion, applied *inside* the Y twist (orientX then rotateY)
    drv("body", "X", "entityMotionY", { scale: -100, offset: 20, min: -0.2, max: 0.2, clampFirst: true, space: "post" }),
    drv(mainFl, "X", "node.relax", { scale: -80, offset: 80, ease: "pow", power: 0.25, min: 0, max: 1, space: "override" }),
    drv(offFl, "X", "node.relax", { scale: 70, ease: "pow", power: 0.25, min: 0, max: 1, space: "override" }),
    drv("head", "X", "entityHeadPitch", { offset: -20, space: "override" }), drv("head", "Y", "entityHeadYaw", { offset: -20 * m }),
  ], {
    // the legs relax over the first ten ticks
    define: { relax: live({ linstep: ["nodeTicksElapsed", 0, 10] }) },
    // a bounce starts the sprint jump over, like the jump
    connections: [conn(`sprint_jump_${leg}`, "animator.bounced")] });
}
// the sprint-jump body: orientX(lean).rotateY(20m) = Ry(20m) * Rx(lean): the clip holds Ry, the driver adds Rx in POST space

const pLadder: Obj = poseNode([
  clip(PL("ladder"), { frame: looped("entityClimbingCycle") }, { damping: { body: 0.5, leftArm: 0.5, rightArm: 0.5, leftForeArm: 0.5, rightForeArm: 0.5, leftLeg: 0.5, rightLeg: 0.5, leftForeLeg: 0.5, rightForeLeg: 0.5, localOffset: [null, null, 0.6] }, vectorModes: { localOffset: "slide" } }),
  clip(PL("ladder_rest"), {}, { damping: { centerRotation: 0.3, renderRotation: 0.6 } }),
  drv("renderRotation", "Y", "entityClimbingRenderYaw", { space: "override" }),
  drv("head", "X", "entityHeadPitch", { space: "override" }), drv("head", "Y", "entityClimbingHeadYaw"),
  when(drv("body", "X", "entityLedgeHeight", { scale: 50, offset: -30, space: "override" }), cmp("entityLedgeHeight", ">=", 0.6)),
  when(drv("leftArm", "X", "entityLedgeHeight", { scale: 40, offset: -124, space: "override" }), cmp("entityLedgeHeight", ">=", 0.6)),
  when(drv("rightArm", "X", "entityLedgeHeight", { scale: 40, offset: -124, space: "override" }), cmp("entityLedgeHeight", ">=", 0.6)),
  when(clip(PL("ladder_ledge_forearms"), {}, { damping: { leftForeArm: 0.5, rightForeArm: 0.5 } }), cmp("entityLedgeHeight", ">=", 0.6)),
]);
for (const it of poseOf(pLadder)) {
  const rotate = it["core:axis_rotate"];
  if (rotate && ["body", "leftArm", "rightArm"].includes(rotate.bone)) {
    it["@damping"] = { [rotate.bone]: 0.5 };
  }
}

const surface = OR(state("entityIsStandingStill"), state("entityIsDrawingBow"), cmp("entityTicksAfterAttack", "<", 10), NOT(state("entityIsUnderwater")));
const deep = NOT(surface);
const deepT: ValueOptions = { ease: "ease_in_out", power: 3, min: 0, max: 1 };
const pSwimming: Obj = poseNode([
  // 0 at the surface, going to 1 over ten ticks under it, and back
  item("core:accumulate", { inout: "node.deep", rate: { if: [deep, 0.1, -0.1] }, min: 0, max: 1 }),
  clip(PL("swim_common"), {}, { damping: { head: 1, renderRotation: 0.7, localOffset: 0.3 }, vectorModes: { localOffset: "slide" } }),
  when(clip(PL("swim_surface_arms"), { frame: looped(scaled("ticks", 0.0825)) }, { damping: { leftArm: 0.3, rightArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3 } }), surface),
  when(clip(PL("swim_surface_legs"), { frame: looped(scaled("ticks", 0.2625)) }, { damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }), surface),
  when(clip(PL("swim_deep_arm_inner"), { frame: looped(scaled("ticks", 0.1625)) }, { damping: { leftArm: 0.3, rightArm: 0.3 } }), deep),
  when(drv("leftArm", "Y", "node.deep", { scale: -90, ease: "ease_in_out", power: 3, min: 0, max: 1 }), deep),
  when(drv("rightArm", "Y", "node.deep", { scale: 90, ease: "ease_in_out", power: 3, min: 0, max: 1 }), deep),
  when(clip(PL("swim_deep_arm_outer"), { frame: looped(scaled("ticks", 0.1625)) }, { space: "pre" }), deep),
  when(clip(PL("swim_deep_arms"), { frame: looped(scaled("ticks", 0.1625)) }, { damping: { leftForeArm: 0.3, rightForeArm: 0.3, body: 0.5, rightHeldItem: 0.3 } }), deep),
  when(clip(PL("swim_deep_legs"), { frame: looped(scaled("ticks", 0.4625)) }, { damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }), deep),
  drv("head", "X", "entityHeadPitch", { space: "override" }), drv("head", "Y", "entityHeadYaw"), drv("head", "X", "node.deep", { scale: -80, ease: "ease_in_out", power: 3, min: 0, max: 1 }),
  drv("renderRotation", "X", "node.deep", { scale: 80, ease: "ease_in_out", power: 3, min: 0, max: 1, space: "override" }),
  item("core:vector", { bone: "root", y: value("node.deep", { ...deepT, scale: 14 }), z: value("node.deep", { ...deepT, scale: -20 }) }, { damping: { root: [null, 0.7, 0.7] }, vectorModes: { root: "slide" } }),
], { define: { deep: variable(0) } });

const nodes: Record<string, Obj> = {
  stand: pStand, walk: pWalk, sprint: pSprint, jump: pJump, sprint_jump_right: sprintJumpNode("right"),
  sprint_jump_left: sprintJumpNode("left"), falling: pFalling, flying: pFlying, swimming: pSwimming,
  ladder: pLadder, elytra: pElytra, riding: pRiding, sitting: pSitting, sleeping: pSleeping,
};

// ---- player: the action layer (BipedActionController and its item actions) ---------------------------
// Right-handed only (the bits read the primary hand); use actions get a clip per active hand.
function mcSin(v: number): number {
  const i = Math.trunc(v * 10430.378) & 65535;
  return Math.sin(i * Math.PI * 2 / 65536);
}

/** A clip with explicit keyframe times: boneFn(t) -> {bone: quaternion}. */
function curveClip(path: string, boneFn: (t: number) => Record<string, Quaternion>, samples: number[], duration: number): void {
  const data: Obj = { bones: {}, duration, times: samples.map((t) => round7(t)) };
  const frames = samples.map((t) => boneFn(t));
  for (const bone of Object.keys(frames[0])) {
    data.bones[bone] = { keyframes: frames.map((f) => ({ position: [0, 0, 0], rotation: f[bone] })) };
  }
  writeClip(path, data);
}

/** What the old string properties asked, as the operations that ask it now. */
function prop(name: string, value: string | null = null, unset = false): Obj {
  switch (name) {
    case "mainHandItem": return { "core:holds_item": ["main_hand", value] };
    case "offHandItem": return { "core:holds_item": ["off_hand", value] };
    case "activeHandSide": return { "core:active_hand_side": [value!.toLowerCase()] };
    case "attackActionType": return { "mobends:attack_action": [value!.toLowerCase()] };
    case "useActionType":
      if (unset) return NOT(OR(...["food", "bow", "shield"].map((a) => ({ "mobends:use_action": [a] }))));
      return { "mobends:use_action": [value!.toLowerCase()] };
  }
  throw new Error(`unknown property ${name}`);
}

// An action bit's slideY() over a base layer that re-slides the same vector restarts every
// frame; the core detects the conflicting write and restarts the slide, so SLIDE is exact.
const tAA = "entityTicksAfterAttack";
const dec = { decreased: [tAA] };
const stillNotRiding = AND(state("entityIsStandingStill"), NOT(state("entityIsRiding")));
const stanceWindow = AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60), state("entityIsOnGround"));
const stanceSprintCond = AND(stanceWindow, state("entityIsSprinting"));
const stanceStillCond = AND(stanceWindow, NOT(state("entityIsSprinting")), state("entityIsStandingStill"));
/** The combo starts over a while after the last attack. */
const comboReset = set("machine.combo", 0, cmp(tAA, ">", 20));
const localOffsetZero = clip(PL("localoffset_zero"), {}, { damping: { localOffset: 0.3 }, vectorModes: { localOffset: "slide" } });
poseClip(join(CLIPS, "player", "localoffset_zero.json"), {}, { localOffset: [0, 0, 0] });


// --- sword slashes: baked clips, the look direction wrapped around the baked head part -------------------
// Everything the bit computes with its hand multiplier is marked "@mirror"; the still-standing legs
// are not (the bit poses them the same way for both hands).
function slashNode(name: string, clipname: string, byAttack: boolean, mainDamp: number, mainSnap: boolean, headDamp: number | null,
                   stillCond: Obj, itemDamp: number | null, itemSnap: boolean, stillExtra: Obj[] | null = null): Obj {
  const frame = byAttack ? tAA : null;
  const part = (bones: string[], mods: Mods): Obj => clip(PL(clipname), frame ? { bones, frame } : { bones }, mods);
  const pose = [
    mirrored(part(["body", "leftArm", "leftForeArm", "rightForeArm", "localOffset"],
                  { damping: { body: 0.9, leftArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3, localOffset: 0.3 }, vectorModes: { localOffset: "slide" } })),
    mirrored(part(["rightArm"], { damping: { rightArm: mainDamp }, snap: mainSnap })),
    mirrored(part(["rightHeldItem"], { damping: itemDamp ? { rightHeldItem: itemDamp } : {}, snap: itemSnap })),
    mirrored(headDamp ? withDamping(drv("head", "X", "entityHeadPitch", { space: "override" }), { head: headDamp }) : drv("head", "X", "entityHeadPitch", { space: "override" })),
    mirrored(part(["head"], { space: "pre" })),
    // the head looks where the player looks, whichever hand swings: not mirrored
    drv("head", "Y", "entityHeadYaw"),
    when(clip(PL(clipname + "_still"), { bones: ["leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "root"] }, { damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.3, rightForeLeg: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "slide" } }), stillCond),
    when(mirrored(clip(PL(clipname + "_still"), { bones: ["renderRotation"] }, { damping: { renderRotation: 0.3 } })), stillCond),
  ];
  if (stillExtra) pose.push(...stillExtra.map((x) => when(mirrored(x), stillCond)));
  return poseNode(pose);
}

// up / inward: legs, fore leg and render rotation are not damped by the bit (keep their rate)
function slashNodeKeepLegs(node: Obj): Obj {
  poseOf(node).at(-2)["@damping"] = { root: [null, 0.6, null] };
  return node;
}

const swordTrail = when(item("mobends:sword_trail", { resetOnEnter: true }), AND(cmp(tAA, "<", 4), prop("attackActionType", "SWORD")));
const slashes: Record<string, Obj> = {
  slash_up: slashNodeKeepLegs(slashNode("attack_slash_up", "slash_up", true, 0.9, false, 0.9, stillNotRiding, 0.9, true)),
  slash_inward: slashNodeKeepLegs(slashNode("attack_slash_inward", "slash_inward", true, 0.9, false, 0.9, stillNotRiding, 0.9, true)),
  slash_down: slashNode("attack_slash_down", "slash_down", false, 0.3, true, 0.9, stillNotRiding, null, true, [drv("head", "Y", null, { const: -30 })]),
  slash_outward: slashNode("attack_slash_outward", "slash_outward", false, 0.3, true, 0.9, stillNotRiding, null, true, [drv("head", "Y", null, { const: -30 })]),
};
// the whirl: head undamped, render rotation snapped, the global offset always dips
const whirl = slashNode("attack_whirl_slash", "slash_whirl", true, 0.3, true, null, state("entityIsStandingStill"), 0.9, false);
poseOf(whirl)[poseOf(whirl).length - 1] = when(clip(PL("slash_whirl_still"), {}, { damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.3, rightForeLeg: 0.3 } }), state("entityIsStandingStill"));
poseOf(whirl).splice(3, 0, clip(PL("slash_whirl"), { bones: ["root"], frame: tAA }, { damping: { root: [null, 0.6, null] }, vectorModes: { root: "slide" } }));
poseOf(whirl).splice(4, 0, clip(PL("slash_whirl"), { bones: ["renderRotation"], frame: tAA }, { snap: true }));
slashes.slash_whirl = whirl;
for (const node of Object.values(slashes)) {
  poseOf(node).unshift(swordTrail);
}
// the whirl feeds the trail for its whole duration and clears it while its first half tick lasts
poseOf(whirl)[0] = item("mobends:sword_trail", {});
poseOf(whirl).unshift(when(item("mobends:sword_trail", { add: false, resetEachFrame: true }), cmp(tAA, "<", 0.5)));

// --- attack stance: two breathing clocks (sin(t/5), cos(t/5.7)) split into two clips ----------------------
// The bit's arm angles are 60h + 5 sin(t/5) etc.: the constant carries the hand multiplier, the
// breathing does not, so the constants mirror while the sways only change arm.
cycleClip(join(CLIPS, "player", "stance_breath0.json"), (p) => ({
  body: rotations(["X", 20 + Math.sin(p) * 2]),
  rightArm: rotations(["Z", Math.sin(p) * 5]),
  head: rotations(["Y", -30], ["X", -(20 + Math.sin(p) * 2)]) }));
cycleClip(join(CLIPS, "player", "stance_breath1.json"), (p) => ({
  leftArm: rotations(["Z", Math.cos(p) * 5]),
  rightArm: rotations(["Y", Math.cos(p) * 5]) }));
poseClip(join(CLIPS, "player", "stance_arms.json"), { rightArm: rotations(["Z", 60]), leftArm: rotations(["Z", -60]) });
poseClip(join(CLIPS, "player", "stance_const.json"), {
  rightLeg: rotations(["X", -30], ["Z", 10], ["Y", 25]), leftLeg: rotations(["X", -30], ["Z", -10], ["Y", -25]),
  rightForeLeg: rotations(["X", 30]), leftForeLeg: rotations(["X", 30]),
  rightForeArm: rotations(["X", -20]), leftForeArm: rotations(["X", -60]),
  rightHeldItem: rotations(["X", 65]), renderRotation: rotations(["Y", -30]) }, { root: [0, -2, 0] });
const b0frame = looped(scaled("ticks", 1 / 5.0));
const b1frame = looped(scaled("ticks", 1 / 5.7));
const stance: Obj = poseNode([
  mirrored(clip(PL("stance_arms"), {}, { damping: { rightArm: 0.3, leftArm: 0.3 } })),
  clip(PL("stance_breath0"), { frame: b0frame, bones: ["body"] }, { damping: { body: 0.3 } }),
  swapped(clip(PL("stance_breath0"), { frame: b0frame, bones: ["rightArm"] }, { space: "post" })),
  mirrored(clip(PL("stance_breath0"), { frame: b0frame, bones: ["head"] }, { space: "pre" })),
  swapped(clip(PL("stance_breath1"), { frame: b1frame, bones: ["leftArm"] }, { space: "post" })),
  swapped(clip(PL("stance_breath1"), { frame: b1frame, bones: ["rightArm"] }, { space: "pre" })),
  mirrored(clip(PL("stance_const"), {}, { damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, rightForeArm: 0.3, leftForeArm: 0.3,
                                                          rightHeldItem: 0.3, renderRotation: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "slide" } })),
  clip(PL("stance_kneel"), { frame: "entityTicksAfterTouchdown" }, { when: cmp("entityTicksAfterTouchdown", "<", 1 / 0.15), damping: { body: 1 }, vectorModes: { root: "snap" } }),
], { on: { update: [comboReset] } });
poseClip(join(CLIPS, "player", "stance_sprint_abs.json"), { rightArm: rotations(["Z", 60], ["Y", 60]), rightHeldItem: rotations(["X", 45]) });
poseClip(join(CLIPS, "player", "stance_sprint_pre.json"), { body: rotations(["Y", 20]), head: rotations(["Y", -20]), leftArm: rotations(["Z", -30]) });
const stanceSprint: Obj = poseNode([
  when(item("mobends:sword_trail", { velocity: [0, 0, -10] }), prop("attackActionType", "SWORD")),
  localOffsetZero,
  mirrored(clip(PL("stance_sprint_abs"), {}, { damping: { rightHeldItem: 0.3 } })),
  mirrored(clip(PL("stance_sprint_pre"), {}, { space: "pre" })),
], { on: { update: [comboReset] } });
const swordIdle: Obj = poseNode([], { on: { update: [comboReset] } });

// --- fists: punches (alternating arm) and the guard -----------------------------------------------------
const legsStill = { rightLeg: rotations(["X", -30], ["Z", 10]), leftLeg: rotations(["X", -30], ["Y", -25], ["Z", -10]),
                    rightForeLeg: rotations(["X", 30]), leftForeLeg: rotations(["X", 30]) };
poseClip(join(CLIPS, "player", "punch_still.json"), legsStill, { root: [0, -2, 0] });
poseClip(join(CLIPS, "player", "punch_right_abs.json"), {
  rightArm: rotations(["Y", -90]), leftArm: rotations(["Z", -20], ["X", -90]),
  rightForeArm: rotations(), leftForeArm: rotations(["X", -80]), body: rotations(["Y", -20]), renderRotation: rotations() });
poseClip(join(CLIPS, "player", "punch_right_pre.json"), { rightArm: rotations(["Y", 10]), head: rotations(["Y", 20]) });
poseClip(join(CLIPS, "player", "punch_right_still.json"), { body: rotations(["Y", -40]), renderRotation: rotations(["Y", -20]) });
poseClip(join(CLIPS, "player", "punch_left_abs.json"), {
  leftArm: rotations(["Y", 100]), rightArm: rotations(["X", -90], ["Z", 20]),
  leftForeArm: rotations(), rightForeArm: rotations(["X", -80]), body: rotations(["Y", 20]), renderRotation: rotations() });
poseClip(join(CLIPS, "player", "punch_left_pre.json"), { leftArm: rotations(["Y", -16]), head: rotations(["Y", -20]) });
poseClip(join(CLIPS, "player", "punch_left_still.json"), { body: rotations(), renderRotation: rotations(["Y", -20]) });
function punchNode(side: string): Obj {
  const [arm, other] = side === "right" ? ["rightArm", "leftArm"] : ["leftArm", "rightArm"];
  const [fore, otherFore] = [arm.replace("Arm", "ForeArm"), other.replace("Arm", "ForeArm")];
  return poseNode([
    clip(PL(`punch_${side}_abs`), {}, { damping: { [arm]: 0.9, [other]: 0.3, [fore]: 0.9, [otherFore]: 0.3, body: 0.6 } }),
    drv(arm, "X", "entityHeadPitch", { offset: -90 }),
    clip(PL(`punch_${side}_pre`), {}, { space: "pre" }),
    when(clip(PL("punch_still"), {}, { damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "slide" } }), state("entityIsStandingStill")),
    when(clip(PL(`punch_${side}_still`), {}, { damping: { body: 0.6 } }), state("entityIsStandingStill")),
  ]);
}
poseClip(join(CLIPS, "player", "fist_guard_abs.json"), {
  ...legsStill,
  renderRotation: rotations(["Y", -20]),
  rightArm: rotations(["X", -90], ["Z", 20]), leftArm: rotations(["X", -90], ["Z", -20]),
  rightForeArm: rotations(["X", -80]), leftForeArm: rotations(["X", -80]) }, { root: [0, -2, 0] });
poseClip(join(CLIPS, "player", "fist_guard_pre.json"), { body: rotations(["X", 10]), head: rotations(["X", -10], ["Y", -20]) });
const fistGuardBones = ["rightArm", "leftArm", "rightForeArm", "leftForeArm", "rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg", "root"];
const fistGuard: Obj = poseNode([
  when(clip(PL("fist_guard_abs"), { bones: fistGuardBones }, { damping: { rightArm: 0.3, leftArm: 0.3, rightForeArm: 0.3, leftForeArm: 0.3,
                                                                              rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "slide" } }), state("entityIsStandingStill")),
  when(mirrored(clip(PL("fist_guard_abs"), { bones: ["renderRotation"] }, { damping: { renderRotation: 0.3 } })), state("entityIsStandingStill")),
  when(clip(PL("fist_guard_pre"), { bones: ["body"] }, { space: "pre" }), state("entityIsStandingStill")),
  when(mirrored(clip(PL("fist_guard_pre"), { bones: ["head"] }, { space: "pre" })), state("entityIsStandingStill")),
]);
const fistsIdle: Obj = poseNode([]);

// --- tool swing: curves over the swing progress (sin(sqrt(p) * 2pi) is steep near 0: dense samples there) -
const swingSamples = range(129).map((k) => (k / 128) ** 2);
const swingPhase = (p: number) => Math.sqrt(p) * 6.2831855;
curveClip(join(CLIPS, "player", "tool_body.json"), (p) => ({ body: rotations(["Y", mcSin(swingPhase(p)) * 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_head.json"), (p) => ({ head: rotations(["Y", -mcSin(swingPhase(p)) * 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_arm.json"), (p) => ({ rightArm: rotations(["X", mcSin(swingPhase(p)) * 50 - 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_arm_post.json"), (p) => ({ rightArm: rotations(["Z", mcCos(swingPhase(p)) * -20 + 10]) }), swingSamples, 1);
poseClip(join(CLIPS, "player", "tool_rest.json"), { centerRotation: rotations() }, { localOffset: [0, 0, 0] });
poseClip(join(CLIPS, "player", "tool_sneak_body.json"), { body: rotations(["X", 20]) });
const swingFrame = "entitySwingProgress";
function alsoWhen(it: Obj, cond: Obj): Obj {
  return when(it, "@when" in it ? AND(it["@when"], cond) : cond);
}
const tool: Obj = poseNode([
  clip(PL("tool_rest"), {}, { damping: { centerRotation: 0.3, localOffset: 0.3 }, vectorModes: { localOffset: "slide" } }),
  clip(PL("tool_body"), { frame: swingFrame }, { damping: { body: 0.8 } }),
  when(clip(PL("tool_sneak_body"), {}, { space: "pre" }), state("entityIsSneaking")),
  when(withDamping(drv("head", "X", "entityHeadPitch", { space: "override" }), { head: 0.8 }), NOT(state("entityIsSneaking"))),
  when(withDamping(drv("head", "X", "entityHeadPitch", { offset: -20, space: "override" }), { head: 0.8 }), state("entityIsSneaking")),
  drv("head", "Y", "entityHeadYaw"),
  clip(PL("tool_head"), { frame: swingFrame }, { space: "pre" }),
  clip(PL("tool_arm"), { frame: swingFrame }, { snap: true }),
  clip(PL("tool_arm_post"), { frame: swingFrame }, { space: "post" }),
].map((x) => {
  const it = alsoWhen(x, state("entityIsSwinging"));
  // the head looks where the player looks, whichever hand swings: not mirrored
  const rotate = x["core:axis_rotate"];
  if (rotate && rotate.bone === "head" && rotate.axis === "y") return it;
  return (JSON.stringify(x).includes("tool_arm") ? swapped : mirrored)(it);
}));

// --- item use: eating, bow, shield; one node per active hand ---------------------------------------------
function useNodes(): Record<string, Obj> {
  const nodes: Record<string, Obj> = {};
  for (const [side, h] of [["right", 1], ["left", -1]] as const) {
    const [arm, other] = side === "right" ? ["rightArm", "leftArm"] : ["leftArm", "rightArm"];
    const [fore, otherFore] = [arm.replace("Arm", "ForeArm"), other.replace("Arm", "ForeArm")];
    const eatSamples = range(65).map((k) => k / 64);
    curveClip(join(CLIPS, "player", `eat_arm_${side}.json`), (b) => ({ [arm]: rotations(["X", b * -80], ["Z", 45 * b * h]) }), eatSamples, 1);
    cycleClip(join(CLIPS, "player", `eat_head_${side}.json`), (p) => ({ head: rotations(["X", mcCos(p) * 5], ["Y", 15 * h]) }));
    // the arm comes up over 1 / 0.15 ticks, then the head chews
    const eatUp = 1 / 0.15;
    nodes[`eat_${side}`] = poseNode([
      clip(PL(`eat_arm_${side}`), { frame: "node.bringUp" }),
      drv(fore, "X", "node.bringUp", { scale: -45, space: "override" }),
      when(clip(PL(`eat_head_${side}`), { frame: looped("ticks") }), cmp("nodeTicksElapsed", ">=", eatUp)),
    ], { define: { bringUp: live({ linstep: ["nodeTicksElapsed", 0, eatUp] }) } });
    // bow: the off arm's Z part is a curve over the head pitch
    const pitchSamples = range(65).map((k) => -90 + 180 * k / 64);
    // keyframe times run 0..180 for a pitch of -90..90
    curveClip(join(CLIPS, "player", `bow_offarm_${side}.json`),
              (t) => ({ [other]: rotations(["Z", (-mcCos((t - 90) / 180 * 3.1415927) * 40 + 40) * h]) }), pitchSamples.map((p) => p + 90), 180);
    nodes[`bow_${side}`] = poseNode([
      localOffsetZero,
      withDamping(drv("head", "X", "entityHeadPitch", { space: "override" }), { head: 0.5 }),
      // on a ladder the body faces the wall and the head only pitches
      when(drv("head", "Y", "node.aimedBowTicks", { scale: -5 * h, offset: 50 * h }), NOT(state("entityIsClimbing"))),
      when(withDamping(drv("body", "Y", "node.aimedBowTicks", { scale: 5 * h, offset: -50 * h, space: "override" }), { body: 0.8 }), NOT(state("entityIsClimbing"))),
      when(drv("body", "Y", "entityHeadYaw"), NOT(state("entityIsClimbing"))),
      when(withDamping(drv("body", "Y", "entityClimbingBodyYaw", { space: "override" }), { body: 0.8 }), state("entityIsClimbing")),
      withDamping(drv(arm, "X", "entityHeadPitch", { offset: -90, space: "override" }), { [arm]: 0.8 }),
      drv(arm, "Y", "node.aimedBowTicks", { scale: -5 * h, offset: 50 * h }),
      withDamping(drv(other, "Y", null, { const: 80 * h, space: "override" }), { [other]: 1 }),
      clip(PL(`bow_offarm_${side}`), { frame: { add: ["entityHeadPitch", 90] } }, { space: "pre" }),
      drv(other, "X", "entityHeadPitch", { offset: -90, min: -160 }),
      withDamping(drv(fore, "X", null, { const: 0, space: "override" }), { [fore]: 1 }),
      drv(otherFore, "X", "node.aimedBowTicks", { scale: -3, space: "override" }),
    ], { define: { aimedBowTicks: live({ min: ["entityItemUseTicks", 15] }) } });
    nodes[`shield_${side}`] = poseNode([
      drv(arm, "Y", "node.bringUp", { scale: -45 * h, space: "override" }),
      drv(fore, "X", "node.bringUp", { scale: -45, space: "override" }),
    ], { define: { bringUp: live({ linstep: ["nodeTicksElapsed", 0, 1 / 0.7] }) } });
  }
  return nodes;
}

// --- the node graph ---------------------------------------------------------------------------------------
// Using an item comes first (a node per active hand), then the attack family of the held item. A family
// is a machine: its selector says where it rests, its connections play the moves on every attack.
const useTypes: [string, string][] = [["eat", "FOOD"], ["bow", "BOW"], ["shield", "SHIELD"]];
const useBranches: Obj[] = useTypes.map(([base, typ]) => ({
  when: prop("useActionType", typ),
  then: ["right", "left"].map((side) => ({ when: prop("activeHandSide", side.toUpperCase()), then: `${base}_${side}` })),
}));
const attackBranch: Obj = {
  when: prop("useActionType", null, true),
  then: [
    // a fresh SwordAction or PunchingAction starts its count over: the machine's state does on entry
    { when: prop("attackActionType", "SWORD"), then: "sword" },
    { when: prop("attackActionType", "FISTS"), then: "fists" },
    { when: prop("attackActionType", "TOOL"), then: "tool" },
  ],
};
const slashOrder = ["slash_up", "slash_down", "slash_inward", "slash_outward", "slash_whirl"];
// The fifth slash is the whirl, when the player allows it (ModConfig.performSpinAttack) and isn't
// riding; otherwise the combo starts over.
// The sword combo ends with a whirl, where the animator allows it (animator.canSpinAttack).
const canSpin = "animator.canSpinAttack";
const slashByCombo = [
  ...slashOrder.map((n, k) => conn(n, AND("machine.attacked", cmp("machine.combo", "==", k), ...(k === 4 ? [canSpin] : [])), [set("machine.combo", (k + 1) % 5)])),
  conn(slashOrder[0], AND("machine.attacked", cmp("machine.combo", "==", 4), NOT(canSpin)), [set("machine.combo", 1)]),
];
const punchConns = [conn("punch_right", AND("machine.attacked", cmp("machine.fist", "==", 0)), [set("machine.fist", 1)]),
                    conn("punch_left", AND("machine.attacked", cmp("machine.fist", "==", 1)), [set("machine.fist", 0)])];

// No move plays until the next attack; the stance while in its window. A slash isn't in the
// selector, which chooses nothing for ten ticks after an attack, so it plays until then.
const swordMachine: Obj = {
  defaultOnEntry: "sword_idle",
  "@define": { attacked: live(dec), combo: variable(0) },
  select: [
    { when: stanceSprintCond, then: "stance_sprint" },
    { when: stanceStillCond, then: "stance" },
    { when: cmp(tAA, ">=", 10), then: "sword_idle" },
  ],
  "@connections": slashByCombo,
  nodes: { sword_idle: swordIdle, stance, stance_sprint: stanceSprint, ...slashes },
};
// A fresh PunchingAction starts with the left fist; after a punch the guard, then the fists rest.
const fistsMachine: Obj = {
  defaultOnEntry: "punch_left",
  "@define": { attacked: live(dec), fist: variable(0) },
  select: [
    { when: AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60)), then: "fist_guard" },
    { when: cmp(tAA, ">=", 60), then: "fists_idle" },
  ],
  "@connections": punchConns,
  nodes: { fists_idle: fistsIdle, punch_right: punchNode("right"), punch_left: punchNode("left"), fist_guard: fistGuard },
};

// Idle only until the first item or attack: nothing leads back to it.
const actionLayer: Obj = { "@when": NOT(state("entityIsSleeping")), defaultOnEntry: "idle",
                           select: [...useBranches, attackBranch],
                           nodes: { idle: poseNode([]), tool, ...useNodes() },
                           machines: { sword: swordMachine, fists: fistsMachine },
                           // a left-handed player plays the hand-dependent items as their mirror image
                           mirror: { "@when": state("entityIsLeftHanded"),
                                     pairs: [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                                             ["leftHeldItem", "rightHeldItem"]] } };

const groundAction = OR("animator.standing", "animator.walking", "animator.sprinting");
function torchArm(side: string): Obj[] {
  const arm = side + "Arm";
  return [drv(arm, "X", "entityHeadPitch", { scale: 0.5, offset: -90, space: "override" }), drv(arm, "Y", "entityHeadYaw", { scale: 0.7 }),
          clip(PL(`torch_forearm_${side}`))];
}
const torchMain = prop("mainHandItem", "minecraft:torch");
const torchOff = prop("offHandItem", "minecraft:torch");
// The upper-body layers follow the locomotion through these, not its nodes.
const player: Obj = {
  formatVersion: 2,
  "@define": { jumping: live(jumping), bounced: live(bounced), standing: live(chooses(playerSelect, "stand")),
               walking: live(chooses(playerSelect, "walk")), sprinting: live(chooses(playerSelect, "sprint")),
               // unless the player turned it off, or is riding
               canSpinAttack: live(AND({ "mobends:spin_attack_enabled": [] }, NOT(state("entityIsRiding")))) },
  layers: [
    // item rotations are reset every frame before the layers run (keeps their damping)
    { defaultOnEntry: "reset", nodes: { reset: poseNode([clip(PL("reset_items"))]) } },
    { select: playerSelect, nodes },
    // sneaking overlay on the ground states
    { "@when": AND(state("entityIsSneaking"), groundAction), defaultOnEntry: "sneak", nodes: { sneak: poseNode([
          clip(PL("sneak_base"), { frame: limbFrame }, { damping: { rightLeg: 1, leftLeg: 1, rightArm: 0.8, leftArm: 0.8, root: [null, 0.6, null], localOffset: 0.3 }, vectorModes: { root: "retarget", localOffset: "slide" } }),
          clip(PL("sneak_forelegs"), { frame: limbFrame }, { damping: { leftForeLeg: 0.3, rightForeLeg: 0.3 } }),
          clip(PL("sneak_swing"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { space: "post" }),
          clip(PL("sneak_body"), { frame: limbFrame }, { space: "post" }),
          clip(PL("sneak_head"), { frame: limbFrame }, { space: "pre" }),
        ]) } },
    // torch holding while standing or walking (not sprinting)
    { "@when": AND(OR("animator.standing", "animator.walking"), OR(torchMain, torchOff)), defaultOnEntry: "torch", nodes: { torch: poseNode([
          // the main hand holds the torch if it has one, else the off hand; the arm follows the primary hand
          ...torchArm("right").map((x) => when(x, AND(torchMain, NOT(state("entityIsLeftHanded"))))),
          ...torchArm("left").map((x) => when(x, AND(torchMain, state("entityIsLeftHanded")))),
          ...torchArm("left").map((x) => when(x, AND(NOT(torchMain), torchOff, NOT(state("entityIsLeftHanded"))))),
          ...torchArm("right").map((x) => when(x, AND(NOT(torchMain), torchOff, state("entityIsLeftHanded")))),
        ]) } },
    // items and attacks (the BipedActionController)
    actionLayer,
    // the cape is physics, kept as a driver
    { defaultOnEntry: "cape", nodes: { cape: poseNode([item("mobends:cape", { bone: "cape" })]) } },
  ] };


// ---- squid: the vanilla tentacle wave over the interpolated squid rotation --------------------------
const SQ = (n: string) => clipKey("squid", n);
function squidBase(t: number, active: boolean): Record<string, Quaternion> {
  const out: Record<string, Quaternion> = {};
  const sr = t + 1.1;
  const f = Math.max(0.0, sr / Math.PI);
  const angle = active ? mcSin(f * f * Math.PI) * 60 : 0;
  for (const i of range(8)) {
    out[`tentacle_${i}_0`] = rotations(["X", angle], ["Y", i * -360.0 / 8 + 90.0]);
  }
  return out;
}
function squidSections(t: number, active: boolean): Record<string, Quaternion> {
  const out: Record<string, Quaternion> = {};
  for (const i of range(8)) {
    for (const j of range(9, 1)) {
      out[`tentacle_${i}_${j}`] = rotations(["X", active ? -(mcSin((t + 1.1) + j * 0.1) * 10) : 0]);
    }
  }
  return out;
}
const squidSamples = range(513).map((k) => 2 * Math.PI * k / 512);
curveClip(join(CLIPS, "squid", "swim_base.json"), (t) => squidBase(t, true), squidSamples, 2 * Math.PI);
poseClip(join(CLIPS, "squid", "swim_base_rest.json"), squidBase(0, false));
curveClip(join(CLIPS, "squid", "swim_sections.json"), (t) => squidSections(t, true), squidSamples, 2 * Math.PI);
poseClip(join(CLIPS, "squid", "swim_sections_rest.json"), squidSections(0, false));
const squidBaseDamp = Object.fromEntries(range(8).map((i) => [`tentacle_${i}_0`, 0.1]));
const squidSectionDamp = Object.fromEntries(range(8).flatMap((i) => range(9, 1).map((j) => [`tentacle_${i}_${j}`, 0.1])));
const squidFrame = "entity.rotation";
const squid: Obj = { formatVersion: 2, layers: [{ defaultOnEntry: "swim", nodes: { swim: poseNode([
  when(clip(SQ("swim_base"), { frame: squidFrame }, { damping: squidBaseDamp }), state("entity.prevRotationLow")),
  when(clip(SQ("swim_base_rest"), {}, { damping: squidBaseDamp }), NOT(state("entity.prevRotationLow"))),
  when(clip(SQ("swim_sections"), { frame: squidFrame }, { damping: squidSectionDamp }), state("entity.rotationLow")),
  when(clip(SQ("swim_sections_rest"), {}, { damping: squidSectionDamp }), NOT(state("entity.rotationLow"))),
]) } }] };


// ---- spider: IK leg drivers for the ground gaits, data for the jump and the death ----------------------
const SP = (n: string) => clipKey("spider", n);
const spiderHead = [withSnap(drv("head", "X", "entityHeadPitch", { space: "override" })), drv("head", "Y", "entityHeadYaw")];
poseClip(join(CLIPS, "spider", "rest.json"), { centerRotation: rotations(), renderRotation: rotations() }, { localOffset: [0, 0, 0] });
const spiderRest = clip(SP("rest"), {}, { damping: { localOffset: 1 }, vectorModes: { localOffset: "slide" } });
const spiderLimbs = [
  { phase: 0, minDist: 20, maxDist: 10, minRot: -80, maxRot: -50 }, { phase: 0.3, minDist: 20, maxDist: 10, minRot: -80, maxRot: -50 },
  { phase: 0.3, minDist: 15, maxDist: 15, minRot: -30, maxRot: 10 }, { phase: 0, minDist: 15, maxDist: 15, minRot: -30, maxRot: 10 },
  { phase: 0.4, minDist: 7, maxDist: 15, minRot: 20, maxRot: 50 }, { phase: 0.7, minDist: 7, maxDist: 15, minRot: 20, maxRot: 50 },
  { phase: 0.7, minDist: 10, maxDist: 20, minRot: 60, maxRot: 80 }, { phase: 0.4, minDist: 10, maxDist: 20, minRot: 60, maxRot: 80 },
];
/**
 * The legs drivers' state: where they publish the ground level (a node state, which the body
 * follows), and the layer state that has them replant the feet.
 */
const spiderLegsState = (publishes: boolean): Obj =>
  (publishes ? { out: { groundLevel: "node.groundLevel" }, reset: "layer.resetLimbs" } : { reset: "layer.resetLimbs" });
const bodyBob = (fn: string, sign: number) => value("ticks", { scale: 0.2, fn, mul: 0.4 * sign });
const spIdle: Obj = poseNode([
  item("mobends:spider_idle_legs", { groundLevel: value("ticks", { scale: 0.1, fn: "sin", mul: 0.5 }), bodyX: bodyBob("sin", 1), bodyZ: bodyBob("cos", 1), kneelDuration: 10, kneelAmplitude: 4, kneelLead: 0,
                                     ...spiderLegsState(true) }),
  withSnap(item("core:vector", { bone: "root", x: bodyBob("sin", 1), y: value("node.groundLevel", { scale: -1 }), z: bodyBob("cos", -1) })),
  ...spiderHead, spiderRest,
], { define: { groundLevel: variable(0) } });
const spMove: Obj = poseNode([
  item("mobends:spider_moving_legs", { swing: value("entityLimbSwing", { scale: 0.6662 }), groundLevel: value("ticks", { scale: 0.6, fn: "mcsin", mul: 1.2 }), kneelDuration: 10, kneelAmplitude: 3, kneelLead: 0.2, limbs: spiderLimbs,
                                       ...spiderLegsState(true) }),
  withSnap(item("core:vector", { bone: "root", x: bodyBob("mcsin", 1), y: value("node.groundLevel", { scale: -1 }), z: bodyBob("mccos", -1) })),
  ...spiderHead, spiderRest,
], { define: { groundLevel: variable(0) } });
poseClip(join(CLIPS, "spider", "crawl_rest.json"), { centerRotation: rotations() }, { localOffset: [0, -10, 0] });
const spCrawl: Obj = poseNode([
  item("mobends:spider_moving_legs", { swing: value("entityDistanceMoved", { scale: 5 }), groundLevel: value("entityDistanceMoved", { scale: 3, fn: "mcsin", mul: 1.2 }), limbs: spiderLimbs,
                                       ...spiderLegsState(false) }),
  ...spiderHead,
  withDamping(drv("renderRotation", "X", null, { const: -90, space: "override" }), { renderRotation: 0.6 }), drv("renderRotation", "Y", "entity.crawlYaw"),
  clip(SP("crawl_rest"), {}, { damping: { localOffset: 0.5 }, vectorModes: { localOffset: "slide" } }),
]);
// jump: legs fan out to their natural yaw and bend with the vertical motion
function naturalYaw(i: number): number {
  const ny = -((i / 7) * 2 - 1);
  return degrees(i % 2 === 1 ? -ny * 1.3 : ny * 1.3);
}
poseClip(join(CLIPS, "spider", "jump.json"), Object.fromEntries(range(8).map((i) => [`leg${i + 1}`, rotations(["Y", naturalYaw(i)])])), { root: [0, 0, 0] });
const jumpMotion = (mul: number, add: number) => value("entityInterpolatedMotionY", { scale: -5, min: -1, max: 1, mul, add });
const alternate = (i: number) => (i % 2 ? -1 : 1);
// Nothing in the jump reads resetLimbs: the next legs driver to start does, and replants the feet.
const spJump: Obj = poseNode([
  withSnap(clip(SP("jump"), { bones: ["root"] })),
  clip(SP("jump"), { bones: range(8).map((i) => `leg${i + 1}`) }, { damping: Object.fromEntries(range(8).map((i) => [`leg${i + 1}`, 1])) }),
  ...range(8).map((i) => drv(`leg${i + 1}`, "Z", jumpMotion(25 * alternate(i), -20 * alternate(i)), { space: "post" })),
  ...range(8).map((i) => withDamping(drv(`foreLeg${i + 1}`, "Z", jumpMotion(-40 * alternate(i), -70 * alternate(i)), { space: "override" }),
                                     { [`foreLeg${i + 1}`]: 1 })),
  spiderRest,
], { on: { enter: [set("layer.resetLimbs", 1)] } });
// death: legs splay (instant), sway with the last limb swing, and wiggle with a decaying speed
const deathZ = [-45, 45, -33.3, 33.3, -33.3, 33.3, -45, 45];
const deathY = [45, -45, 22.5, -22.5, -22.5, 22.5, -45, 45];
poseClip(join(CLIPS, "spider", "death.json"), {
  ...Object.fromEntries(range(8).map((i) => [`leg${i + 1}`, rotations(["Z", deathZ[i]], ["Y", deathY[i]])])),
  ...Object.fromEntries(range(8).map((i) => [`foreLeg${i + 1}`, rotations(["Z", i % 2 ? 89 : -89])])) });
const swingPhases = [0.0, 0.0, Math.PI, Math.PI, Math.PI / 2, Math.PI / 2, Math.PI * 3 / 2, Math.PI * 3 / 2];
cycleClip(join(CLIPS, "spider", "death_sway_y.json"), (ls) => Object.fromEntries(range(8).map((i) =>
  [`leg${i + 1}`, rotations(["Y", -(mcCos(ls * 2.0 + swingPhases[i]) * 0.4) * alternate(i)])])), 128);
cycleClip(join(CLIPS, "spider", "death_sway_z.json"), (ls) => Object.fromEntries(range(8).map((i) =>
  [`leg${i + 1}`, rotations(["Z", Math.abs(mcSin(ls + swingPhases[i]) * 0.4) * alternate(i)])])), 128);
const wigglePhases = [0, Math.PI / 4, Math.PI / 2, Math.PI / 4 * 3];
cycleClip(join(CLIPS, "spider", "death_wiggle.json"), (ph) => Object.fromEntries(range(8).map((i) =>
  [`leg${i + 1}`, rotations(["Z", mcCos(ph + wigglePhases[i % 4])])])), 128);
const amountDeg = value("entityLimbSwingAmount", { scale: 180 / Math.PI });
const spDeath: Obj = poseNode([
  item("core:accumulate", { inout: "node.wiggleSpeed", rate: -0.1, min: 0 }),
  item("core:accumulate", { inout: "node.wigglePhase", rate: value("node.wiggleSpeed", { scale: 2, offset: 0.3 }) }),
  item("core:vector", { bone: "root", y: 10 }, { damping: { root: [null, 0.3, null] }, vectorModes: { root: "slide" } }),
  ...spiderHead,
  withSnap(clip(SP("death"))),
  clip(SP("death_sway_y"), { frame: limbFrame, weight: amountDeg }, { space: "pre" }),
  clip(SP("death_sway_z"), { frame: limbFrame, weight: amountDeg }, { space: "pre" }),
  clip(SP("death_wiggle"), { frame: looped("node.wigglePhase"), weight: value("node.wiggleSpeed", { scale: 10, offset: 10 }) }, { space: "pre" }),
], { define: { wiggleSpeed: variable(1), wigglePhase: variable(0) } });
// the controller's decision chain
const spider: Obj = { formatVersion: 2, layers: [{ "@define": { resetLimbs: variable(1), jumping: live(jumping) }, defaultOnEntry: "idle",
  select: [
    { when: cmp("entityHealth", "<=", 0), then: "death" },
    { when: { "mobends:is_beside_climbable": [] }, then: "crawl" },
    { when: "layer.jumping", then: "jump" },
    { when: state("entityIsStandingStill"), then: "idle" },
    { then: "move" },
  ],
  nodes: { idle: spIdle, move: spMove, jump: spJump, crawl: spCrawl, death: spDeath } }] };

// the skeleton's controller runs the biped action controller as well: bow, sword, tool, fists
const skeletonActions = structuredClone(actionLayer);
delete skeletonActions["@when"]; // only players sleep
skeleton.layers.push(skeletonActions);

// the zombie villager's controller is the zombie's
const zombieVillager: Obj = { formatVersion: 2, extends: "mobends:bends/animators/zombie.json" };


// ---- mobs described by model definitions (bends/models/*.json): generic walkers ---------------------
// Their legs are split at the knee by the definition; the gait swings the upper segment like the
// vanilla model did and bends the lower one when the leg trails.
type Leg = [upper: string, lower: string | null, phase: number];
const W = (folder: string, n: string) => clipKey(folder, n);
const legBones = (legs: Leg[]) => legs.flatMap(([upper, lower]) => (lower ? [upper, lower] : [upper]));

/** legs: list of (upper, lower, phase). A looping cycle over the limb swing at unit amplitude. */
function walkerGait(folder: string, legs: Leg[], upperAmp = 55, lowerAmp = 35): Obj {
  const frame = (p: number) => {
    const out: Record<string, Quaternion> = {};
    for (const [upper, lower, phase] of legs) {
      const swing = mcCos(p + phase);
      out[upper] = rotations(["X", swing * upperAmp]);
      if (lower) {
        out[lower] = rotations(["X", (1 - swing) * 0.5 * lowerAmp]);
      }
    }
    return out;
  };
  cycleClip(join(CLIPS, folder, "walk.json"), frame);
  return clip(W(folder, "walk"), { frame: limbFrame, weight: "entityLimbSwingAmount" }, { damping: Object.fromEntries(legBones(legs).map((b) => [b, 0.8])) });
}

/** A slow breathing sway of the listed bones on the tick clock. */
function walkerIdle(folder: string, bones: [string, number][]): Obj {
  cycleClip(join(CLIPS, folder, "idle.json"), (p) => Object.fromEntries(bones.map(([b, amp]) => [b, rotations(["X", mcCos(p) * amp])])));
  return clip(W(folder, "idle"), { frame: looped(scaled("ticks", 0.09)) }, { damping: Object.fromEntries(bones.map(([b]) => [b, 0.5])) });
}

/**
 * The legs hanging straight: under the idle, so a mob that stops walking settles instead of
 * holding its last stride (nothing else in the stand pose touches the legs).
 */
function walkerRest(folder: string, bones: string[], damping = 0.5): Obj {
  poseClip(join(CLIPS, folder, "rest.json"), Object.fromEntries(bones.map((b) => [b, rotations()])));
  return clip(W(folder, "rest"), {}, { damping: Object.fromEntries(bones.map((b) => [b, damping])) });
}

function walkerJump(folder: string, legs: Leg[], upperAngle = -20, lowerAngle = 35): Obj {
  poseClip(join(CLIPS, folder, "jump.json"), {
    ...Object.fromEntries(legs.map(([upper]) => [upper, rotations(["X", upperAngle])])),
    ...Object.fromEntries(legs.filter(([, lower]) => lower).map(([, lower]) => [lower!, rotations(["X", lowerAngle])])) });
  return clip(W(folder, "jump"), {}, { damping: Object.fromEntries(legBones(legs).map((b) => [b, 0.3])) });
}

/**
 * The head following the look direction. Over a clip that poses the head, the yaw composes onto it
 * (PRE); where nothing else poses the head it has to set it (OVERRIDE): a PRE / POST rotation with
 * nothing under it composes onto the bone's last target, so it would add up frame after frame.
 */
function headLookOver(headBone: string, clipPosesHead: boolean): Obj[] {
  return [withDamping(drv(headBone, "Y", "entityHeadYaw", { space: clipPosesHead ? "pre" : "override" }), { [headBone]: 0.5 }),
          drv(headBone, "X", "entityHeadPitch", { space: "post" })];
}

function walkerAnimator(folder: string, legs: Leg[], idleBones: [string, number][], extra: Obj[] = [], headBone = "head"): Obj {
  const look = headLookOver(headBone, false);
  const idleLook = headLookOver(headBone, idleBones.some(([bone]) => bone === headBone));
  const stand = poseNode([walkerRest(folder, legBones(legs)), walkerIdle(folder, idleBones), ...idleLook, ...extra]);
  const walk = poseNode([walkerGait(folder, legs), ...look, ...extra]);
  const jump = poseNode([walkerJump(folder, legs), ...look, ...extra]);
  return { formatVersion: 2, "@define": { jumping: live(jumping) }, layers: [{ select: locomotionSelect, nodes: { stand, walk, jump } }] };
}

// vanilla ModelQuadruped: legs 1 and 4 swing together, 2 and 3 opposite
const quadLegs: Leg[] = [["leg1", "foreLeg1", 0.0], ["leg2", "foreLeg2", Math.PI], ["leg3", "foreLeg3", Math.PI], ["leg4", "foreLeg4", 0.0]];
const quadruped = walkerAnimator("quadruped", quadLegs, [["head", 2.0], ["body", 1.0]]);
// the cow's layer, which extends it, follows the walk through it
quadruped["@define"].walking = live(chooses(locomotionSelect, "walk"));
const villager = walkerAnimator("villager", [["rightLeg", "foreRightLeg", 0.0], ["leftLeg", "foreLeftLeg", Math.PI]], [["head", 1.5], ["arms", 2.0]]);
const chickenWings = [withDamping(drv("rightWing", "Z", "entity.wingAngle", { space: "override" }), { rightWing: 1 }),
                      withDamping(drv("leftWing", "Z", "entity.wingAngle", { scale: -1, space: "override" }), { leftWing: 1 })];
const chicken = walkerAnimator("chicken", [["rightLeg", "foreRightLeg", 0.0], ["leftLeg", "foreLeftLeg", Math.PI]], [["head", 2.0]], chickenWings);

const animators: [string, Obj][] = [
  ["biped", biped], ["zombie", zombie], ["skeleton", skeleton], ["pig_zombie", pigZombie], ["player", player], ["squid", squid], ["spider", spider], ["zombie_villager", zombieVillager],
  ["quadruped", quadruped], ["villager", villager], ["chicken", chicken],
];
// ---- biped body rates ------------------------------------------------------------------------
// The rates above are the ones the procedural bits set, but biped entity data used to advance
// "root" and "renderRotation" twice per frame, so on bipeds those two moved at twice the rate.
// The smoothing is linear, so asking for the doubled rate once per frame looks exactly the same.
const BIPEDS = new Set(["biped", "zombie", "skeleton", "pig_zombie", "player", "zombie_villager"]);
const DOUBLED_BONES = ["root", "globalOffset", "renderRotation"];

function doubled(rate: unknown): unknown {
  if (typeof rate === "number") return rate * 2;
  if (Array.isArray(rate)) return rate.map((r) => (r === null ? null : doubled(r)));
  return { mul: [rate, 2] };
}

function doubleBodyRates(node: unknown): void {
  if (Array.isArray(node)) {
    node.forEach(doubleBodyRates);
    return;
  }
  if (typeof node !== "object" || node === null) return;
  for (const [key, value] of Object.entries(node as Obj)) {
    if ((key === "damping" || key === "@damping") && typeof value === "object" && value !== null && !Array.isArray(value)) {
      for (const bone of DOUBLED_BONES) {
        if (bone in (value as Obj)) (value as Obj)[bone] = doubled((value as Obj)[bone]);
      }
    } else {
      doubleBodyRates(value);
    }
  }
}

for (const [name, built] of animators) {
  // A deep copy: the builders share value objects between items.
  const data = JSON.parse(JSON.stringify(built));
  if (BIPEDS.has(name)) doubleBodyRates(data);
  writeFileSync(join(ANIM, name + ".json"), pretty(data) + "\n");
  console.log("wrote", name + ".json");
}
