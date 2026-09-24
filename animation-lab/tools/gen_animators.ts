#!/usr/bin/env bun
/**
 * Generates the animator JSON files (biped, zombie, skeleton, pig zombie, player, squid, spider and
 * the mobs described by model definitions) and the hand-authored clips they use.
 *
 * The animators are plain data; this script only spares us from writing the shared structure by
 * hand and from computing the quaternions of hand-authored constant poses. Run it through
 * `gradle generateAnimators` (or directly with the mod's resources dir as the argument).
 */
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";

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

function clip(folder: string, name: string): string {
  return `mobends:bends/animations/${folder}/${name}.json`;
}

function writeClip(path: string, data: Obj): void {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, JSON.stringify(data));
}

// ---- condition helpers ----------------------------------------------------------------------
const cmp = (variable: string, op: string, value: number): Obj => ({ type: "core:compare", variable, op, value });
const state = (s: string): Obj => ({ type: "core:state", state: s });
const action = (tag: string): Obj => ({ type: "core:action", tag });
const AND = (...conditions: Obj[]): Obj => ({ type: "core:and", conditions });
const OR = (...conditions: Obj[]): Obj => ({ type: "core:or", conditions });
const NOT = (condition: Obj): Obj => ({ type: "core:not", condition });

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
  const data: Obj = { bones: {}, duration: 0, loop: false };
  for (const [bone, q] of Object.entries(bones)) {
    data.bones[bone] = { keyframes: [{ position: [0, 0, 0], rotation: q, scale: [1, 1, 1] }] };
  }
  for (const [bone, v] of Object.entries(vectors)) {
    data.bones[bone] = { keyframes: [{ position: v, rotation: [0, 0, 0, 1], scale: [1, 1, 1] }] };
  }
  writeClip(path, data);
}

// ---- shared biped locomotion ------------------------------------------------------------------
const B = (n: string) => clip("biped", n);
const jumping = OR(state("AIRBORNE"), cmp("ticksAfterTouchdown", "<", 1));
const grounded = AND(state("ON_GROUND"), cmp("ticksAfterTouchdown", ">=", 1));
const limbTime = { variable: "limbSwing", scale: 0.6662 };
const headLook: Obj[] = [
  { driver: "core:axis_rotate", bone: "head", axis: "Y", angle: { variable: "headYaw" }, space: "PRE" },
  { driver: "core:axis_rotate", bone: "head", axis: "X", angle: { variable: "headPitch" }, space: "POST" },
];
const kneel = { animationKey: B("kneel"), time: { variable: "ticksAfterTouchdown" },
                when: cmp("ticksAfterTouchdown", "<", 1 / 0.15), damping: { body: 1 }, vectorModes: { root: "SNAP" } };
const resetDamping = { root: 0.3, localOffset: 0.3, renderRotation: 0.3, centerRotation: 0.3,
                       renderRightItemRotation: 0.3, renderLeftItemRotation: 0.3 };

const stand: Obj = {
  type: "core:pose", tags: ["stand"],
  enterPose: [{ animationKey: B("stand_enter"), when: cmp("ticksAfterTouchdown", "<", 0.5 / 0.15) }],
  pose: [
    { animationKey: B("stand"), time: { variable: "ticks", scale: 0.1 },
      damping: { ...resetDamping, body: 1, rightArm: 0.4, leftArm: 0.4 },
      vectorModes: { root: "SLIDE", localOffset: "SLIDE" } },
    ...headLook,
    kneel,
  ],
  connections: [
    { target: "jump", triggerCondition: jumping },
    { target: "walk", triggerCondition: state("MOVING_HORIZONTALLY") },
  ] };
const walk: Obj = {
  type: "core:pose", tags: ["walk"],
  pose: [
    { animationKey: B("walk_base"), time: limbTime,
      damping: { ...resetDamping, body: 0.5, head: 0.5, rightArm: 0.8, leftArm: 0.8, rightForeArm: 0.8, leftForeArm: 0.8,
                 rightLeg: 1, leftLeg: 1, root: [0.3, 0.6, 0.3] },
      vectorModes: { root: "RETARGET", localOffset: "SLIDE" } },
    { animationKey: B("walk_forelegs"), time: limbTime, damping: { leftForeLeg: 0.5, rightForeLeg: 0.5 } },
    { animationKey: B("walk_swing"), time: limbTime, weight: { variable: "limbSwingAmount" }, space: "POST" },
    { driver: "core:axis_rotate", bone: "body", axis: "Z", angle: { variable: "headYaw", scale: -0.1, min: -10, max: 10 }, space: "PRE" },
    ...headLook,
    kneel,
  ],
  connections: [
    { target: "jump", triggerCondition: jumping },
    { target: "stand", triggerCondition: state("STANDING_STILL") },
  ] };
const jump: Obj = {
  type: "core:pose", tags: ["jump"],
  enterPose: [{ animationKey: B("jump_enter") }],
  pose: [
    { animationKey: B("jump"), time: { variable: "ticksInAir" },
      damping: { ...resetDamping, centerRotation: 0.7, body: 0.2, rightArm: 0.05, leftArm: 0.05, rightForeArm: 0.3, leftForeArm: 0.3 },
      vectorModes: { root: "SLIDE" } },
    ...headLook,
    { animationKey: B("jump_moving_base"), time: limbTime, when: state("MOVING_HORIZONTALLY"),
      damping: { rightLeg: 1, leftLeg: 1, leftForeArm: 0.3, rightForeArm: 0.3 } },
    { animationKey: B("jump_moving_forelegs"), time: limbTime, when: state("MOVING_HORIZONTALLY"),
      damping: { leftForeLeg: 0.3, rightForeLeg: 0.3 } },
    { animationKey: B("jump_moving_swing"), time: limbTime, weight: { variable: "limbSwingAmount" }, space: "POST", when: state("MOVING_HORIZONTALLY") },
    { animationKey: B("jump_still"), when: state("STANDING_STILL"),
      damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3 } },
  ],
  connections: [
    { target: "stand", triggerCondition: AND(grounded, state("STANDING_STILL")) },
    { target: "walk", triggerCondition: AND(grounded, state("MOVING_HORIZONTALLY")) },
    { target: "jump", triggerCondition: AND(cmp("prevMotionY", "<", 0), cmp("motionY", ">", 0)) },
  ] };

const biped: Obj = {
  formatVersion: 2,
  layers: [
    { type: "KEYFRAME", entryNode: "stand", nodes: { stand, walk, jump } },
  ] };

// ---- zombie: animation sets -----------------------------------------------------------------------
const Z = (n: string) => clip("zombie", n);
const zombie: Obj = {
  formatVersion: 2,
  extends: "mobends:bends/animators/biped.json",
  layers: [
    { type: "KEYFRAME", mode: "ADDITIVE", additiveSpace: { default: "PRE", body: "POST", root: "OVERRIDE" },
      when: cmp("animationSet", "==", 0), entryNode: "lean", nodes: { lean: {
        type: "core:pose", tags: ["lean"],
        pose: [
          { animationKey: Z("lean"), damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } },
          { animationKey: Z("lean_arms_up"), space: "OVERRIDE", when: AND(state("MOVING_HORIZONTALLY"), cmp("currentWalkingState", "==", 1)) },
        ] } } },
    { type: "KEYFRAME", when: cmp("animationSet", "==", 1), entryNode: "stumble", nodes: { stumble: {
        type: "core:pose", tags: ["stumbling"],
        pose: [
          { animationKey: Z("stumble_base"), time: limbTime,
            damping: { rightLeg: 1, leftLeg: 1, rightArm: 1, leftArm: 1, body: 0.5 } },
          { animationKey: Z("stumble_swing"), time: limbTime, weight: { variable: "limbSwingAmount" }, space: "POST" },
          { animationKey: Z("stumble_head"), time: limbTime, space: "PRE" },
        ] } } },
  ] };

// ---- skeleton: strafing legs ------------------------------------------------------------------------
const S = (n: string) => clip("skeleton", n);
const skeleton: Obj = {
  formatVersion: 2,
  extends: "mobends:bends/animators/biped.json",
  layers: [
    { type: "KEYFRAME", when: AND(action("walk"), state("STRAFING")), entryNode: "strafe", nodes: { strafe: {
        type: "core:pose", tags: ["strafe"],
        pose: [
          { animationKey: S("strafe_base"), time: limbTime, damping: { rightLeg: 1, leftLeg: 1 } },
          { animationKey: S("strafe_swing"), time: limbTime, weight: { variable: "limbSwingAmount" }, space: "POST" },
        ] } } },
  ] };

// ---- pig zombie: hunched pose + slash attack -------------------------------------------------------
const P = (n: string) => clip("pigzombie", n);
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
    { type: "KEYFRAME", mode: "ADDITIVE", additiveSpace: { default: "PRE", root: "OVERRIDE" },
      when: OR(action("stand"), action("walk")), entryNode: "hunch", nodes: { hunch: {
        type: "core:pose", tags: ["hunch"],
        pose: [
          { animationKey: P("pose_post"), space: "POST" },
          { animationKey: P("pose_pre"), space: "PRE" },
          { animationKey: P("stand_offset"), when: action("stand"), damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } },
          { animationKey: P("walk_bob"), time: limbTime, when: action("walk"), damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } },
        ] } } },
    { type: "KEYFRAME", when: cmp("entitySwingProgress", ">", 0), entryNode: "slash",
      mirror: { when: state("LEFT_HANDED"), negate: ["headYaw"],
                pairs: [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                        ["renderLeftItemRotation", "renderRightItemRotation"]] },
      nodes: { slash: {
        type: "core:pose", tags: ["attack", "attack_slash_inward"],
        pose: [
          { animationKey: P("slash"), time: { variable: "ticksAfterAttack" }, mirror: true,
            damping: { body: 0.9, head: 0.9, rightArm: 0.9, leftArm: 0.3, rightForeArm: 0.3, leftForeArm: 0.3, localOffset: 0.3 },
            vectorModes: { localOffset: "SLIDE" } },
          ...headLook.map((x) => ({ ...x, mirror: true })),
          // the still-standing legs are the same for both hands; the render rotation is not
          { animationKey: P("slash_still"), bones: ["leftLeg", "rightLeg", "rightForeLeg", "root"], when: AND(state("STANDING_STILL"), NOT(state("RIDING"))),
            damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } },
          { animationKey: P("slash_still"), bones: ["renderRotation"], mirror: true, when: AND(state("STANDING_STILL"), NOT(state("RIDING"))), damping: { renderRotation: 0.3 } },
          { driver: "core:axis_rotate", bone: "renderRightItemRotation", axis: "X", angle: 50, space: "OVERRIDE", snap: true, mirror: true, damping: { renderRightItemRotation: 0.9 } },
        ] } } },
  ] };


// ---- player ----------------------------------------------------------------------------------------
const PL = (n: string) => clip("player", n);

/** Minecraft's table-based cosine, so analytic clips match the procedural output exactly. */
function mcCos(v: number): number {
  const i = (Math.trunc(v * 10430.378) + 16384) & 65535;
  return Math.sin(i * Math.PI * 2 / 65536);
}

/** Generates a looping clip from a function phase -> {bone: quaternion}. */
function cycleClip(path: string, boneFn: (phase: number) => Record<string, Quaternion>, count = 64, period = 2 * Math.PI): void {
  const frames = range(count + 1).map((i) => boneFn(i < count ? period * i / count : 0.0));
  const data: Obj = { bones: {}, duration: period, loop: true };
  for (const bone of Object.keys(frames[0])) {
    data.bones[bone] = { keyframes: frames.map((f) => ({ position: [0, 0, 0], rotation: f[bone], scale: [1, 1, 1] })) };
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

type DrvOptions = { scale?: number; offset?: number; space?: string; const?: number; [key: string]: unknown };

function drv(bone: string, ax: Axis, variable: string | null = null,
             { scale = 1, offset = 0, space = "PRE", const: constant, ...kw }: DrvOptions = {}): Obj {
  const angle = constant !== undefined ? constant : { variable, scale, offset, ...kw };
  return { driver: "core:axis_rotate", bone, axis: ax, angle, space };
}

function withDamping(item: Obj, damping: Obj): Obj {
  return { ...item, damping: { ...damping } };
}

function when(item: Obj, cond: Obj): Obj {
  return { ...item, when: cond };
}

const resetItems = { renderRightItemRotation: rotations(), renderLeftItemRotation: rotations() };
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
  when({ animationKey: PL("riding_head"), damping: { head: 0.5 } }, cmp("ticksAfterAttack", "<", 10)),
  when(drv("head", "Y", "headYaw", { space: "PRE" }), cmp("ticksAfterAttack", "<", 10)),
  when(drv("head", "X", "headPitch", { space: "POST" }), cmp("ticksAfterAttack", "<", 10)),
];
const pWalk = structuredClone(walk);
pWalk.pose.push(...attackHead);
const sprintTime = { variable: "limbSwing", scale: 0.6662 * 0.8 };
const pSprint: Obj = {
  type: "core:pose", tags: ["sprint"],
  pose: [
    { animationKey: PL("sprint_base"), time: sprintTime,
      damping: { ...resetDamping, body: 0.8, head: 0.5, rightArm: 0.8, leftArm: 0.8, rightLeg: 1, leftLeg: 1,
                 rightForeLeg: 0.7, leftForeLeg: 0.7, root: [0.1, 0.9, 0.1] },
      vectorModes: { root: "RETARGET", localOffset: "SLIDE" } },
    { animationKey: PL("sprint_forearms"), time: sprintTime, damping: { leftForeArm: 0.3, rightForeArm: 0.3 } },
    { animationKey: PL("sprint_swing"), time: sprintTime, weight: { variable: "limbSwingAmount" }, space: "POST" },
    drv("body", "Z", "headYaw", { scale: -0.3, min: -10, max: 10 }),
    ...headLook,
    ...attackHead,
  ] };

/**
 * The PlayerController decision tree, in priority order, as connections. Each branch is
 * guarded by the negation of the branches above it, so a node whose own condition still holds
 * never falls through to a lower-priority target.
 */
function playerConnections(exclude: string | null): Obj[] {
  const sleeping = state("SLEEPING");
  const riding = state("RIDING");
  const elytra = state("ELYTRA_FLYING");
  const climbing = state("CLIMBING");
  const inWater = state("IN_WATER");
  const groups: [Obj | null, [string, Obj | null][]][] = [
    [sleeping, [["sleeping", null]]],
    [riding, [["riding", state("RIDING_LIVING")], ["sitting", NOT(state("RIDING_LIVING"))]]],
    [elytra, [["elytra", null]]],
    [climbing, [["ladder", null]]],
    [inWater, [["swimming", null]]],
    [jumping, [
      ["flying", state("FLYING")],
      ["falling", AND(NOT(state("FLYING")), cmp("ticksFalling", ">", 10))],
      ["sprint_jump", AND(NOT(state("FLYING")), cmp("ticksFalling", "<=", 10), state("SPRINTING"))],
      ["jump", AND(NOT(state("FLYING")), cmp("ticksFalling", "<=", 10), NOT(state("SPRINTING")))],
    ]],
    [null, [
      ["stand", state("STANDING_STILL")],
      ["sprint", AND(state("MOVING_HORIZONTALLY"), state("SPRINTING"))],
      ["walk", AND(state("MOVING_HORIZONTALLY"), NOT(state("SPRINTING")))],
    ]],
  ];
  const conns: Obj[] = [];
  const prior: Obj[] = [];
  for (const [group, branches] of groups) {
    for (const [target, branch] of branches) {
      const parts = prior.map((p) => NOT(p));
      if (group !== null) parts.push(group);
      if (branch !== null) parts.push(branch);
      const cond = parts.length === 1 ? parts[0] : AND(...parts);
      if (target !== exclude) {
        conns.push({ target, triggerCondition: cond });
      }
    }
    if (group !== null) {
      prior.push(group);
    }
  }
  return conns;
}

const pStand = structuredClone(stand);
const pJump = structuredClone(jump);
// the biped jump's self-restart stays last
const jumpRestart = pJump.connections.filter((c: Obj) => c.target === "jump");

const pSleeping: Obj = { type: "core:pose", tags: ["sleeping"], pose: [
  { animationKey: PL("sleeping"), time: { variable: "ticks", scale: 0.1 },
    damping: { ...resetDamping, head: 1, rightArm: 0.4, leftArm: 0.4 }, vectorModes: { root: "SLIDE", localOffset: "SLIDE" } }] };
const pSitting: Obj = { type: "core:pose", tags: ["sitting"], pose: [
  { animationKey: PL("sitting"), damping: { centerRotation: 0.3, body: 0.5 } }, ...headLook] };
const pRiding: Obj = { type: "core:pose", tags: ["riding"], pose: [
  { animationKey: PL("riding"), damping: { centerRotation: 0.3, body: 0.5, localOffset: 0.3 }, vectorModes: { localOffset: "SLIDE" } },
  ...headLook,
  drv("body", "Z", "ridingRelativeHeadYaw", { scale: -0.25, min: -20, max: 20, space: "OVERRIDE" }),
  { animationKey: PL("riding_legs"), time: { variable: "ridingRelativeYaw", offset: 180 } },
  // the moving clip carries the head as Rx(-25) sampled at zero look; that part is applied in PRE space below
  when({ animationKey: PL("riding_moving"), bones: ["body", "leftArm", "rightArm", "leftForeArm", "rightForeArm"] }, state("MOVING_HORIZONTALLY")),
  when({ animationKey: PL("riding_moving_head"), space: "PRE" }, AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", "<=", 0.01))),
  when({ animationKey: PL("riding_fast"), time: { variable: "ticks", scale: 0.5 }, damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } },
       AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01))),
] };
// riding_fast carries head as an absolute value (Rx(-body) sampled at zero look); it must compose
// after the look drivers, so it is split: body/arms/root absolute, head PRE.
pRiding.pose[pRiding.pose.length - 1] = when({ animationKey: PL("riding_fast"), bones: ["body", "leftArm", "rightArm", "root"], time: { variable: "ticks", scale: 0.5 },
                                                damping: { root: [null, 0.6, null] }, vectorModes: { root: "RETARGET" } }, AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01)));
pRiding.pose.push(when({ animationKey: PL("riding_fast"), bones: ["head"], time: { variable: "ticks", scale: 0.5 }, space: "PRE" },
                       AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01))));

const pElytra: Obj = { type: "core:pose", tags: ["elytra"], pose: [
  { animationKey: PL("elytra"), damping: { head: 1, body: 0.7, leftArm: 0.7, rightArm: 0.7, leftForeArm: 0.7, rightForeArm: 0.7,
                                           leftLeg: 0.7, rightLeg: 0.7, leftForeLeg: 0.7, rightForeLeg: 0.7, centerRotation: 1, renderRotation: 0.7, root: 0.7 },
    vectorModes: { root: "SLIDE" } },
  drv("head", "Y", "headYaw", { space: "OVERRIDE" }), drv("head", "X", null, { const: -90 }),
  drv("leftArm", "Z", null, { const: -60 }), drv("leftArm", "Z", "flightSpeedFactor", { scale: 55 }), drv("leftArm", "Z", "headYawAbs", { scale: -0.5 }),
  drv("rightArm", "Z", null, { const: 60 }), drv("rightArm", "Z", "flightSpeedFactor", { scale: -55 }), drv("rightArm", "Z", "headYawAbs", { scale: 0.5 }),
] };

const flySprint = AND(state("SPRINTING"), NOT(state("DRAWING_BOW")), cmp("ticksAfterAttack", ">=", 10));
const flyHover = AND(NOT(flySprint), cmp("motionMagnitude", "<", 0.1));
const flyMoving = AND(NOT(flySprint), cmp("motionMagnitude", ">=", 0.1));
const bodyRotXDrv = (bone: string, sign: number, space: string) =>
  drv(bone, "X", "headPitch", { scale: 0.8 * sign, min: Math.min(0, -60 * sign), max: Math.max(0, -60 * sign), space });
const pFlying: Obj = { type: "core:pose", tags: ["flying"], pose: [
  { animationKey: PL("fly_common"), damping: { renderRotation: 0.7, root: 0.7 }, vectorModes: { root: "SLIDE" } },
  // sprint-flying
  when({ animationKey: PL("fly_sprint"), damping: { centerRotation: 1, head: 1, body: 0.7, leftArm: 0.7, rightArm: 0.7, leftForeArm: 0.7, rightForeArm: 0.7,
                                                    leftLeg: 0.7, rightLeg: 0.7, leftForeLeg: 0.7, rightForeLeg: 0.7 } }, flySprint),
  when(drv("centerRotation", "X", "flightPitch", { space: "OVERRIDE" }), flySprint), when(drv("centerRotation", "Z", "headYaw"), flySprint),
  when(bodyRotXDrv("body", 1, "OVERRIDE"), flySprint),
  when(drv("head", "Y", "headYaw", { space: "OVERRIDE" }), flySprint), when(drv("head", "X", "headPitch"), flySprint),
  when(bodyRotXDrv("head", -1, "PRE"), flySprint), when(drv("head", "X", "flightPitch", { scale: -1 }), flySprint),
  when(bodyRotXDrv("leftArm", -1, "OVERRIDE"), flySprint), when(drv("leftArm", "Z", null, { const: -60 }), flySprint), when(drv("leftArm", "Z", "flightSpeedFactor", { scale: 55 }), flySprint), when(drv("leftArm", "Z", "headYawAbs", { scale: -0.5 }), flySprint),
  when(bodyRotXDrv("rightArm", -1, "OVERRIDE"), flySprint), when(drv("rightArm", "Z", null, { const: 60 }), flySprint), when(drv("rightArm", "Z", "flightSpeedFactor", { scale: -55 }), flySprint), when(drv("rightArm", "Z", "headYawAbs", { scale: 0.5 }), flySprint),
  // hovering
  when({ animationKey: PL("fly_hover_arms"), time: { variable: "ticks", scale: 0.0825 }, damping: { leftArm: 0.3, rightArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3 } }, flyHover),
  when({ animationKey: PL("fly_hover_legs"), time: { variable: "ticks", scale: 0.125 }, damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }, flyHover),
  when({ animationKey: PL("fly_hover_rest"), damping: { head: 1 } }, flyHover),
  when(drv("head", "X", "headPitch", { space: "OVERRIDE" }), flyHover), when(drv("head", "Y", "headYaw"), flyHover),
  // moving
  when({ animationKey: PL("fly_moving"), damping: { head: 1 } }, flyMoving),
  when(drv("centerRotation", "X", "forwardMomentum", { scale: 50 }), flyMoving),
  when(drv("leftArm", "X", "forwardMomentum", { scale: 90, space: "OVERRIDE" }), flyMoving), when(drv("leftArm", "Z", "sidewaysMomentum", { scale: -80, offset: -20, space: "POST" }), flyMoving),
  when(drv("rightArm", "X", "forwardMomentum", { scale: 90, space: "OVERRIDE" }), flyMoving), when(drv("rightArm", "Z", "sidewaysMomentum", { scale: -80, offset: 20, space: "POST" }), flyMoving),
  when(drv("leftLeg", "Z", "sidewaysMomentum", { scale: -40, offset: -5, space: "POST" }), flyMoving),
  when(drv("rightLeg", "Z", "sidewaysMomentum", { scale: -40, offset: 5, space: "POST" }), flyMoving),
  when(drv("head", "X", "headPitch", { space: "OVERRIDE" }), flyMoving), when(drv("head", "X", "forwardMomentum", { scale: -50 }), flyMoving),
  when(drv("centerRotation", "Y", "headYaw", { scale: -1, space: "POST" }), AND(flyMoving, NOT(state("DRAWING_BOW")))),
] };
// clamps for forward/sideways momentum: the bit clamps them to [-1, 1]
for (const item of pFlying.pose) {
  const a = item.angle;
  if (typeof a === "object" && a !== null && ["forwardMomentum", "sidewaysMomentum"].includes(a.variable)) {
    a.min = -1;
    a.max = 1;
    a.clampFirst = true;
  }
}

const fallDamp = { variable: "ticksFalling", scale: 0.9 / 80, offset: -0.9 * 10 / 80, min: 0, max: 0.9 };
const fallBones = ["leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "renderRotation"];
const pFalling: Obj = { type: "core:pose", tags: ["falling"], pose: [
  { animationKey: PL("falling_rest"), damping: { centerRotation: 0.3, body: 0.5 } },
  drv("head", "X", "headPitch", { space: "OVERRIDE" }), drv("head", "Y", "headYaw"),
  { animationKey: PL("falling"), time: { variable: "ticks", scale: 0.5 }, damping: Object.fromEntries(fallBones.map((b) => [b, fallDamp])) },
  { animationKey: PL("falling_head"), space: "PRE", damping: { head: fallDamp } },
] };

function sprintJumpNode(leg: string): Obj {
  const m = leg === "right" ? 1 : -1;
  const [mainFl, offFl] = leg === "right" ? ["rightForeLeg", "leftForeLeg"] : ["leftForeLeg", "rightForeLeg"];
  return { type: "core:pose", tags: ["sprint_jump"], pose: [
    { driver: "core:ramp", name: "relax", speed: 0.1, downSpeed: 0 },
    { animationKey: PL(`sprint_jump_${leg}`), damping: { centerRotation: 0.3, root: 0.5, body: 0.3, rightLeg: 0.8, leftLeg: 0.8, rightArm: 0.3, leftArm: 0.3 }, vectorModes: { root: "SLIDE" } },
    // body lean from the vertical motion, applied *inside* the Y twist (orientX then rotateY)
    drv("body", "X", "motionY", { scale: -100, offset: 20, min: -0.2, max: 0.2, clampFirst: true, space: "POST" }),
    drv(mainFl, "X", "relax", { scale: -80, offset: 80, ease: "pow", power: 0.25, min: 0, max: 1, space: "OVERRIDE" }),
    drv(offFl, "X", "relax", { scale: 70, ease: "pow", power: 0.25, min: 0, max: 1, space: "OVERRIDE" }),
    drv("head", "X", "headPitch", { offset: -20, space: "OVERRIDE" }), drv("head", "Y", "headYaw", { offset: -20 * m }),
  ] };
}
// the sprint-jump body: orientX(lean).rotateY(20m) = Ry(20m) * Rx(lean): the clip holds Ry, the driver adds Rx in POST space

const pLadder: Obj = { type: "core:pose", tags: ["ladder_climb"], pose: [
  { animationKey: PL("ladder"), time: { variable: "climbingCycle" },
    damping: { body: 0.5, leftArm: 0.5, rightArm: 0.5, leftForeArm: 0.5, rightForeArm: 0.5, leftLeg: 0.5, rightLeg: 0.5, leftForeLeg: 0.5, rightForeLeg: 0.5, localOffset: [null, null, 0.6] },
    vectorModes: { localOffset: "SLIDE" } },
  { animationKey: PL("ladder_rest"), damping: { centerRotation: 0.3, renderRotation: 0.6 } },
  drv("renderRotation", "Y", "climbingRenderYaw", { space: "OVERRIDE" }),
  drv("head", "X", "headPitch", { space: "OVERRIDE" }), drv("head", "Y", "climbingHeadYaw"),
  when(drv("body", "X", "ledgeHeight", { scale: 50, offset: -30, space: "OVERRIDE" }), cmp("ledgeHeight", ">=", 0.6)),
  when(drv("leftArm", "X", "ledgeHeight", { scale: 40, offset: -124, space: "OVERRIDE" }), cmp("ledgeHeight", ">=", 0.6)),
  when(drv("rightArm", "X", "ledgeHeight", { scale: 40, offset: -124, space: "OVERRIDE" }), cmp("ledgeHeight", ">=", 0.6)),
  when({ animationKey: PL("ladder_ledge_forearms"), damping: { leftForeArm: 0.5, rightForeArm: 0.5 } }, cmp("ledgeHeight", ">=", 0.6)),
] };
for (const item of pLadder.pose) {
  if (item.driver === "core:axis_rotate" && ["body", "leftArm", "rightArm"].includes(item.bone)) {
    item.damping = { [item.bone]: 0.5 };
  }
}

const surface = OR(state("STANDING_STILL"), state("DRAWING_BOW"), cmp("ticksAfterAttack", "<", 10), NOT(state("UNDERWATER")));
const deep = NOT(surface);
const deepT = { variable: "deep", ease: "ease_in_out", power: 3, min: 0, max: 1 };
const pSwimming: Obj = { type: "core:pose", tags: ["swimming"], pose: [
  { driver: "core:ramp", name: "deep", speed: 0.1, when: deep, readBeforeAdvance: true },
  { animationKey: PL("swim_common"), damping: { head: 1, renderRotation: 0.7, localOffset: 0.3 }, vectorModes: { localOffset: "SLIDE" } },
  when({ animationKey: PL("swim_surface_arms"), time: { variable: "ticks", scale: 0.0825 }, damping: { leftArm: 0.3, rightArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3 } }, surface),
  when({ animationKey: PL("swim_surface_legs"), time: { variable: "ticks", scale: 0.2625 }, damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }, surface),
  when({ animationKey: PL("swim_deep_arm_inner"), time: { variable: "ticks", scale: 0.1625 }, damping: { leftArm: 0.3, rightArm: 0.3 } }, deep),
  when(drv("leftArm", "Y", "deep", { scale: -90, ease: "ease_in_out", power: 3, min: 0, max: 1 }), deep),
  when(drv("rightArm", "Y", "deep", { scale: 90, ease: "ease_in_out", power: 3, min: 0, max: 1 }), deep),
  when({ animationKey: PL("swim_deep_arm_outer"), time: { variable: "ticks", scale: 0.1625 }, space: "PRE" }, deep),
  when({ animationKey: PL("swim_deep_arms"), time: { variable: "ticks", scale: 0.1625 }, damping: { leftForeArm: 0.3, rightForeArm: 0.3, body: 0.5, renderRightItemRotation: 0.3 } }, deep),
  when({ animationKey: PL("swim_deep_legs"), time: { variable: "ticks", scale: 0.4625 }, damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.4, rightForeLeg: 0.4 } }, deep),
  drv("head", "X", "headPitch", { space: "OVERRIDE" }), drv("head", "Y", "headYaw"), drv("head", "X", "deep", { scale: -80, ease: "ease_in_out", power: 3, min: 0, max: 1 }),
  drv("renderRotation", "X", "deep", { scale: 80, ease: "ease_in_out", power: 3, min: 0, max: 1, space: "OVERRIDE" }),
  { driver: "core:vector", bone: "root", y: { ...deepT, scale: 14 }, z: { ...deepT, scale: -20 }, damping: { root: [null, 0.7, 0.7] }, vectorModes: { root: "SLIDE" } },
] };

const nodes: Record<string, Obj> = {
  stand: pStand, walk: pWalk, sprint: pSprint, jump: pJump, sprint_jump_right: sprintJumpNode("right"),
  sprint_jump_left: sprintJumpNode("left"), falling: pFalling, flying: pFlying, swimming: pSwimming,
  ladder: pLadder, elytra: pElytra, riding: pRiding, sitting: pSitting, sleeping: pSleeping,
};
for (const [name, node] of Object.entries(nodes)) {
  const key = name.startsWith("sprint_jump") ? "sprint_jump" : name;
  // sprint jump picks the leg variant; the biped jump keeps its restart; sprint jump restarts likewise
  const conns = playerConnections(key).filter((c) => c.target !== "sprint_jump");
  const sj = playerConnections(null).filter((c) => c.target === "sprint_jump").map((c) => c.triggerCondition)[0];
  const targets = conns.map((c) => c.target);
  let at = targets.includes("jump") ? targets.indexOf("jump")
    : targets.includes("falling") ? targets.indexOf("falling") + 1 : targets.indexOf("flying") + 1;
  if (name !== "sprint_jump_right") {
    conns.splice(at, 0, { target: "sprint_jump_right", triggerCondition: AND(sj, state("SPRINT_JUMP_LEG")) });
    at += 1;
  }
  if (name !== "sprint_jump_left") {
    conns.splice(at, 0, { target: "sprint_jump_left", triggerCondition: AND(sj, NOT(state("SPRINT_JUMP_LEG"))) });
  }
  if (name === "jump") {
    conns.push(...jumpRestart);
  }
  if (name.startsWith("sprint_jump")) {
    conns.push({ target: name, triggerCondition: AND(cmp("prevMotionY", "<", 0), cmp("motionY", ">", 0)) });
  }
  node.connections = conns;
}

// ---- player: the action layer (BipedActionController and its item actions) ---------------------------
// Right-handed only (the bits read the primary hand); use actions get a clip per active hand.
function mcSin(v: number): number {
  const i = Math.trunc(v * 10430.378) & 65535;
  return Math.sin(i * Math.PI * 2 / 65536);
}

/** A non-looping clip with explicit keyframe times: boneFn(t) -> {bone: quaternion}. */
function curveClip(path: string, boneFn: (t: number) => Record<string, Quaternion>, samples: number[], duration: number, loop = false): void {
  const data: Obj = { bones: {}, duration, loop, times: samples.map((t) => round7(t)) };
  const frames = samples.map((t) => boneFn(t));
  for (const bone of Object.keys(frames[0])) {
    data.bones[bone] = { keyframes: frames.map((f) => ({ position: [0, 0, 0], rotation: f[bone], scale: [1, 1, 1] })) };
  }
  writeClip(path, data);
}

function prop(name: string, value: unknown = null, unset = false): Obj {
  const c: Obj = { type: "core:property", property: name };
  if (unset) c.unset = true;
  else c.value = value;
  return c;
}

function conn(target: string, cond: Obj, sets: Obj | null = null): Obj {
  const c: Obj = { target, triggerCondition: cond };
  if (sets) c.set = sets;
  return c;
}

// An action bit's slideY() over a base layer that re-slides the same vector restarts every
// frame; the core detects the conflicting write and restarts the slide, so SLIDE is exact.
const tAA = "ticksAfterAttack";
const dec = { type: "core:decreased", variable: tAA };
const stillNotRiding = AND(state("STANDING_STILL"), NOT(state("RIDING")));
const stanceWindow = AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60), state("ON_GROUND"));
const stanceSprintCond = AND(stanceWindow, state("SPRINTING"));
const stanceStillCond = AND(stanceWindow, NOT(state("SPRINTING")), state("STANDING_STILL"));
const comboReset = { driver: "core:set", variable: "combo", value: 0, when: cmp(tAA, ">", 20) };
const localOffsetZero = { animationKey: PL("localoffset_zero"), damping: { localOffset: 0.3 }, vectorModes: { localOffset: "SLIDE" } };
poseClip(join(CLIPS, "player", "localoffset_zero.json"), {}, { localOffset: [0, 0, 0] });

const mirrored = (item: Obj): Obj => ({ ...item, mirror: true });
const swapped = (item: Obj): Obj => ({ ...item, swapSides: true });

// --- sword slashes: baked clips, the look direction wrapped around the baked head part -------------------
// Everything the bit computes with its hand multiplier is marked "mirror"; the still-standing legs
// are not (the bit poses them the same way for both hands).
function slashNode(name: string, clipname: string, byAttack: boolean, mainDamp: number, mainSnap: boolean, headDamp: number | null,
                   stillCond: Obj, itemDamp: number | null, itemSnap: boolean, stillExtra: Obj[] | null = null): Obj {
  const time = byAttack ? { variable: tAA } : null;
  const item = (bones: string[], kw: Obj): Obj => {
    const it: Obj = { animationKey: PL(clipname), bones };
    if (time) it.time = time;
    return Object.assign(it, kw);
  };
  const pose = [
    mirrored(item(["body", "leftArm", "leftForeArm", "rightForeArm", "localOffset"],
                  { damping: { body: 0.9, leftArm: 0.3, leftForeArm: 0.3, rightForeArm: 0.3, localOffset: 0.3 }, vectorModes: { localOffset: "SLIDE" } })),
    mirrored(item(["rightArm"], { damping: { rightArm: mainDamp }, snap: mainSnap })),
    mirrored(item(["renderRightItemRotation"], { damping: itemDamp ? { renderRightItemRotation: itemDamp } : {}, snap: itemSnap })),
    mirrored(headDamp ? withDamping(drv("head", "X", "headPitch", { space: "OVERRIDE" }), { head: headDamp }) : drv("head", "X", "headPitch", { space: "OVERRIDE" })),
    mirrored(item(["head"], { space: "PRE" })),
    mirrored(drv("head", "Y", "headYaw")),
    when({ animationKey: PL(clipname + "_still"), bones: ["leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "root"],
           damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.3, rightForeLeg: 0.3, root: [null, 0.6, null] },
           vectorModes: { root: "SLIDE" } }, stillCond),
    when(mirrored({ animationKey: PL(clipname + "_still"), bones: ["renderRotation"], damping: { renderRotation: 0.3 } }), stillCond),
  ];
  if (stillExtra) pose.push(...stillExtra.map((x) => when(mirrored(x), stillCond)));
  return { type: "core:pose", tags: [name], pose };
}

// up / inward: legs, fore leg and render rotation are not damped by the bit (keep their rate)
function slashNodeKeepLegs(node: Obj): Obj {
  node.pose.at(-2).damping = { root: [null, 0.6, null] };
  return node;
}

const swordTrail = when({ driver: "mobends:sword_trail", resetOnEnter: true }, AND(cmp(tAA, "<", 4), prop("attackActionType", "SWORD")));
const slashes: Record<string, Obj> = {
  slash_up: slashNodeKeepLegs(slashNode("attack_slash_up", "slash_up", true, 0.9, false, 0.9, stillNotRiding, 0.9, true)),
  slash_inward: slashNodeKeepLegs(slashNode("attack_slash_inward", "slash_inward", true, 0.9, false, 0.9, stillNotRiding, 0.9, true)),
  slash_down: slashNode("attack_slash_down", "slash_down", false, 0.3, true, 0.9, stillNotRiding, null, true, [drv("head", "Y", null, { const: -30 })]),
  slash_outward: slashNode("attack_slash_outward", "slash_outward", false, 0.3, true, 0.9, stillNotRiding, null, true, [drv("head", "Y", null, { const: -30 })]),
};
// the whirl: head undamped, render rotation snapped, the global offset always dips
const whirl = slashNode("attack_whirl_slash", "slash_whirl", true, 0.3, true, null, state("STANDING_STILL"), 0.9, false);
whirl.pose[whirl.pose.length - 1] = when({ animationKey: PL("slash_whirl_still"), damping: { leftLeg: 0.3, rightLeg: 0.3, leftForeLeg: 0.3, rightForeLeg: 0.3 } }, state("STANDING_STILL"));
whirl.pose.splice(3, 0, { animationKey: PL("slash_whirl"), bones: ["root"], time: { variable: tAA }, damping: { root: [null, 0.6, null] }, vectorModes: { root: "SLIDE" } });
whirl.pose.splice(4, 0, { animationKey: PL("slash_whirl"), bones: ["renderRotation"], time: { variable: tAA }, snap: true });
slashes.slash_whirl = whirl;
for (const node of Object.values(slashes)) {
  node.pose.unshift(swordTrail);
}
// the whirl feeds the trail for its whole duration and clears it while its first half tick lasts
whirl.pose[0] = { driver: "mobends:sword_trail" };
whirl.pose.unshift(when({ driver: "mobends:sword_trail", add: false, resetEachFrame: true }, cmp(tAA, "<", 0.5)));

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
  renderRightItemRotation: rotations(["X", 65]), renderRotation: rotations(["Y", -30]) }, { root: [0, -2, 0] });
const b0time = { variable: "ticks", scale: 1 / 5.0 };
const b1time = { variable: "ticks", scale: 1 / 5.7 };
const stance: Obj = { type: "core:pose", tags: ["attack_stance"], pose: [
  mirrored({ animationKey: PL("stance_arms"), damping: { rightArm: 0.3, leftArm: 0.3 } }),
  { animationKey: PL("stance_breath0"), time: b0time, bones: ["body"], damping: { body: 0.3 } },
  swapped({ animationKey: PL("stance_breath0"), time: b0time, bones: ["rightArm"], space: "POST" }),
  mirrored({ animationKey: PL("stance_breath0"), time: b0time, bones: ["head"], space: "PRE" }),
  swapped({ animationKey: PL("stance_breath1"), time: b1time, bones: ["leftArm"], space: "POST" }),
  swapped({ animationKey: PL("stance_breath1"), time: b1time, bones: ["rightArm"], space: "PRE" }),
  mirrored({ animationKey: PL("stance_const"), damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, rightForeArm: 0.3, leftForeArm: 0.3,
                                                          renderRightItemRotation: 0.3, renderRotation: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "SLIDE" } }),
  { animationKey: PL("stance_kneel"), time: { variable: "ticksAfterTouchdown" }, when: cmp("ticksAfterTouchdown", "<", 1 / 0.15), damping: { body: 1 }, vectorModes: { root: "SNAP" } },
  comboReset,
] };
poseClip(join(CLIPS, "player", "stance_sprint_abs.json"), { rightArm: rotations(["Z", 60], ["Y", 60]), renderRightItemRotation: rotations(["X", 45]) });
poseClip(join(CLIPS, "player", "stance_sprint_pre.json"), { body: rotations(["Y", 20]), head: rotations(["Y", -20]), leftArm: rotations(["Z", -30]) });
const stanceSprint: Obj = { type: "core:pose", tags: ["attack_stance_sprint"], pose: [
  when({ driver: "mobends:sword_trail", velocity: [0, 0, -10] }, prop("attackActionType", "SWORD")),
  localOffsetZero,
  mirrored({ animationKey: PL("stance_sprint_abs"), damping: { renderRightItemRotation: 0.3 } }),
  mirrored({ animationKey: PL("stance_sprint_pre"), space: "PRE" }),
  comboReset,
] };
const swordIdle: Obj = { type: "core:pose", pose: [comboReset] };

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
  return { type: "core:pose", tags: ["punch"], pose: [
    { animationKey: PL(`punch_${side}_abs`), damping: { [arm]: 0.9, [other]: 0.3, [fore]: 0.9, [otherFore]: 0.3, body: 0.6 } },
    drv(arm, "X", "headPitch", { offset: -90 }),
    { animationKey: PL(`punch_${side}_pre`), space: "PRE" },
    when({ animationKey: PL("punch_still"), damping: { rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, root: [null, 0.6, null] }, vectorModes: { root: "SLIDE" } }, state("STANDING_STILL")),
    when({ animationKey: PL(`punch_${side}_still`), damping: { body: 0.6 } }, state("STANDING_STILL")),
  ] };
}
poseClip(join(CLIPS, "player", "fist_guard_abs.json"), {
  ...legsStill,
  renderRotation: rotations(["Y", -20]),
  rightArm: rotations(["X", -90], ["Z", 20]), leftArm: rotations(["X", -90], ["Z", -20]),
  rightForeArm: rotations(["X", -80]), leftForeArm: rotations(["X", -80]) }, { root: [0, -2, 0] });
poseClip(join(CLIPS, "player", "fist_guard_pre.json"), { body: rotations(["X", 10]), head: rotations(["X", -10], ["Y", -20]) });
const fistGuardBones = ["rightArm", "leftArm", "rightForeArm", "leftForeArm", "rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg", "root"];
const fistGuard: Obj = { type: "core:pose", tags: ["fist_guard"], pose: [
  when({ animationKey: PL("fist_guard_abs"), bones: fistGuardBones, damping: { rightArm: 0.3, leftArm: 0.3, rightForeArm: 0.3, leftForeArm: 0.3,
                                                                              rightLeg: 0.3, leftLeg: 0.3, rightForeLeg: 0.3, leftForeLeg: 0.3, root: [null, 0.6, null] },
         vectorModes: { root: "SLIDE" } }, state("STANDING_STILL")),
  when(mirrored({ animationKey: PL("fist_guard_abs"), bones: ["renderRotation"], damping: { renderRotation: 0.3 } }), state("STANDING_STILL")),
  when({ animationKey: PL("fist_guard_pre"), bones: ["body"], space: "PRE" }, state("STANDING_STILL")),
  when(mirrored({ animationKey: PL("fist_guard_pre"), bones: ["head"], space: "PRE" }), state("STANDING_STILL")),
] };
const fistsIdle: Obj = { type: "core:pose", pose: [] };

// --- tool swing: curves over the swing progress (sin(sqrt(p) * 2pi) is steep near 0: dense samples there) -
const swingSamples = range(129).map((k) => (k / 128) ** 2);
const swingPhase = (p: number) => Math.sqrt(p) * 6.2831855;
curveClip(join(CLIPS, "player", "tool_body.json"), (p) => ({ body: rotations(["Y", mcSin(swingPhase(p)) * 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_head.json"), (p) => ({ head: rotations(["Y", -mcSin(swingPhase(p)) * 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_arm.json"), (p) => ({ rightArm: rotations(["X", mcSin(swingPhase(p)) * 50 - 30]) }), swingSamples, 1);
curveClip(join(CLIPS, "player", "tool_arm_post.json"), (p) => ({ rightArm: rotations(["Z", mcCos(swingPhase(p)) * -20 + 10]) }), swingSamples, 1);
poseClip(join(CLIPS, "player", "tool_rest.json"), { centerRotation: rotations() }, { localOffset: [0, 0, 0] });
poseClip(join(CLIPS, "player", "tool_sneak_body.json"), { body: rotations(["X", 20]) });
const swingTime = { variable: "swingProgress" };
function alsoWhen(item: Obj, cond: Obj): Obj {
  return when(item, "when" in item ? AND(item.when, cond) : cond);
}
const tool: Obj = { type: "core:pose", tags: ["tool"], pose: [
  { animationKey: PL("tool_rest"), damping: { centerRotation: 0.3, localOffset: 0.3 }, vectorModes: { localOffset: "SLIDE" } },
  { animationKey: PL("tool_body"), time: swingTime, damping: { body: 0.8 } },
  when({ animationKey: PL("tool_sneak_body"), space: "PRE" }, state("SNEAKING")),
  when(withDamping(drv("head", "X", "headPitch", { space: "OVERRIDE" }), { head: 0.8 }), NOT(state("SNEAKING"))),
  when(withDamping(drv("head", "X", "headPitch", { offset: -20, space: "OVERRIDE" }), { head: 0.8 }), state("SNEAKING")),
  drv("head", "Y", "headYaw"),
  { animationKey: PL("tool_head"), time: swingTime, space: "PRE" },
  { animationKey: PL("tool_arm"), time: swingTime, snap: true },
  { animationKey: PL("tool_arm_post"), time: swingTime, space: "POST" },
].map((x) => (JSON.stringify(x).includes("tool_arm") ? swapped : mirrored)(alsoWhen(x, state("SWINGING")))) };

// --- item use: eating, bow, shield; one node per active hand ---------------------------------------------
function useNodes(): Record<string, Obj> {
  const nodes: Record<string, Obj> = {};
  for (const [side, h] of [["right", 1], ["left", -1]] as const) {
    const [arm, other] = side === "right" ? ["rightArm", "leftArm"] : ["leftArm", "rightArm"];
    const [fore, otherFore] = [arm.replace("Arm", "ForeArm"), other.replace("Arm", "ForeArm")];
    const eatSamples = range(65).map((k) => k / 64);
    curveClip(join(CLIPS, "player", `eat_arm_${side}.json`), (b) => ({ [arm]: rotations(["X", b * -80], ["Z", 45 * b * h]) }), eatSamples, 1);
    cycleClip(join(CLIPS, "player", `eat_head_${side}.json`), (p) => ({ head: rotations(["X", mcCos(p) * 5], ["Y", 15 * h]) }));
    nodes[`eat_${side}`] = { type: "core:pose", tags: ["eating"], pose: [
      { driver: "core:ramp", name: "bringUp", speed: 0.15, downSpeed: 0 },
      { driver: "core:ramp", name: "bringUpPrev", speed: 0.15, downSpeed: 0, readBeforeAdvance: true },
      { animationKey: PL(`eat_arm_${side}`), time: { variable: "bringUp" } },
      drv(fore, "X", "bringUp", { scale: -45, space: "OVERRIDE" }),
      when({ animationKey: PL(`eat_head_${side}`), time: { variable: "ticks" } }, cmp("bringUpPrev", ">=", 1)),
    ] };
    // bow: the off arm's Z part is a curve over the head pitch
    const pitchSamples = range(65).map((k) => -90 + 180 * k / 64);
    // keyframe times run 0..180 for a pitch of -90..90
    curveClip(join(CLIPS, "player", `bow_offarm_${side}.json`),
              (t) => ({ [other]: rotations(["Z", (-mcCos((t - 90) / 180 * 3.1415927) * 40 + 40) * h]) }), pitchSamples.map((p) => p + 90), 180);
    nodes[`bow_${side}`] = { type: "core:pose", tags: ["bow"], pose: [
      localOffsetZero,
      withDamping(drv("head", "X", "headPitch", { space: "OVERRIDE" }), { head: 0.5 }),
      // on a ladder the body faces the wall and the head only pitches
      when(drv("head", "Y", "aimedBowTicks", { scale: -5 * h, offset: 50 * h }), NOT(state("CLIMBING"))),
      when(withDamping(drv("body", "Y", "aimedBowTicks", { scale: 5 * h, offset: -50 * h, space: "OVERRIDE" }), { body: 0.8 }), NOT(state("CLIMBING"))),
      when(drv("body", "Y", "headYaw"), NOT(state("CLIMBING"))),
      when(withDamping(drv("body", "Y", "climbingBodyYaw", { space: "OVERRIDE" }), { body: 0.8 }), state("CLIMBING")),
      withDamping(drv(arm, "X", "headPitch", { offset: -90, space: "OVERRIDE" }), { [arm]: 0.8 }),
      drv(arm, "Y", "aimedBowTicks", { scale: -5 * h, offset: 50 * h }),
      withDamping(drv(other, "Y", null, { const: 80 * h, space: "OVERRIDE" }), { [other]: 1 }),
      { animationKey: PL(`bow_offarm_${side}`), time: { variable: "headPitch", offset: 90 }, space: "PRE" },
      drv(other, "X", "headPitch", { offset: -90, min: -160 }),
      withDamping(drv(fore, "X", null, { const: 0, space: "OVERRIDE" }), { [fore]: 1 }),
      drv(otherFore, "X", "aimedBowTicks", { scale: -3, space: "OVERRIDE" }),
    ] };
    nodes[`shield_${side}`] = { type: "core:pose", tags: ["shield"], pose: [
      { driver: "core:ramp", name: "bringUp", speed: 0.7, downSpeed: 0 },
      drv(arm, "Y", "bringUp", { scale: -45 * h, space: "OVERRIDE" }),
      drv(fore, "X", "bringUp", { scale: -45, space: "OVERRIDE" }),
    ] };
  }
  return nodes;
}

// --- the node graph ---------------------------------------------------------------------------------------
const useTypes: [string, string][] = [["eat", "FOOD"], ["bow", "BOW"], ["shield", "SHIELD"]];
function useConns(excludeType: string | null = null): Obj[] {
  const out: Obj[] = [];
  for (const [base, typ] of useTypes) {
    if (typ === excludeType) continue;
    for (const side of ["right", "left"]) {
      out.push(conn(`${base}_${side}`, AND(prop("useActionType", typ), prop("activeHandSide", side.toUpperCase()))));
    }
  }
  return out;
}
const noUse = prop("useActionType", null, true);
const familyEntry: Record<string, [string, Obj | null, Obj | null][]> = {
  // a fresh SwordAction: no move plays until the next attack; the stance if in its window
  sword: [["stance_sprint", stanceSprintCond, { combo: 0 }], ["stance", stanceStillCond, { combo: 0 }], ["sword_idle", null, { combo: 0 }]],
  // a fresh PunchingAction starts with the left fist
  fists: [["punch_left", cmp(tAA, "<", 10), { fist: 0 }], ["fist_guard", AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60)), { fist: 0 }], ["fists_idle", null, { fist: 0 }]],
  tool: [["tool", null, null]],
};
const familyType: Record<string, string> = { sword: "SWORD", fists: "FISTS", tool: "TOOL" };
function attackConns(excludeFamily: string | null = null): Obj[] {
  const out: Obj[] = [];
  for (const [family, entries] of Object.entries(familyEntry)) {
    if (family === excludeFamily) continue;
    for (const [target, cond, sets] of entries) {
      const parts = [noUse, prop("attackActionType", familyType[family]), ...(cond ? [cond] : [])];
      out.push(conn(target, AND(...parts), sets));
    }
  }
  return out;
}
const slashOrder = ["slash_up", "slash_down", "slash_inward", "slash_outward", "slash_whirl"];
const slashByCombo = slashOrder.map((n, k) => conn(n, AND(dec, cmp("combo", "==", k)), { combo: (k + 1) % 5 }));
const afterSlash = [conn("stance_sprint", stanceSprintCond), conn("stance", stanceStillCond),
                    conn("sword_idle", AND(cmp(tAA, ">=", 10), NOT(stanceSprintCond), NOT(stanceStillCond)))];
const punchConns = [conn("punch_right", AND(dec, cmp("fist", "==", 0)), { fist: 1 }), conn("punch_left", AND(dec, cmp("fist", "==", 1)), { fist: 0 })];
const guardWindow = AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60));

const actionNodes: Record<string, Obj> = {
  idle: { type: "core:pose", pose: [] }, sword_idle: swordIdle, stance, stance_sprint: stanceSprint,
  fists_idle: fistsIdle, punch_right: punchNode("right"), punch_left: punchNode("left"), fist_guard: fistGuard, tool,
  ...slashes,
  ...useNodes(),
};
for (const [name, node] of Object.entries(actionNodes)) {
  let c: Obj[];
  if (name === "idle") {
    c = [...useConns(), ...attackConns()];
  } else if (name === "sword_idle") {
    c = [...useConns(), ...attackConns("sword"), ...slashByCombo, conn("stance_sprint", stanceSprintCond), conn("stance", stanceStillCond)];
  } else if (name.startsWith("slash_")) {
    c = [...useConns(), ...attackConns("sword"), ...slashByCombo, ...afterSlash];
  } else if (name === "stance") {
    c = [...useConns(), ...attackConns("sword"), ...slashByCombo, conn("stance_sprint", stanceSprintCond), conn("sword_idle", NOT(OR(stanceSprintCond, stanceStillCond)))];
  } else if (name === "stance_sprint") {
    c = [...useConns(), ...attackConns("sword"), ...slashByCombo, conn("stance", stanceStillCond), conn("sword_idle", NOT(OR(stanceSprintCond, stanceStillCond)))];
  } else if (name === "fists_idle") {
    c = [...useConns(), ...attackConns("fists"), ...punchConns, conn("fist_guard", guardWindow)];
  } else if (name.startsWith("punch_")) {
    c = [...useConns(), ...attackConns("fists"), ...punchConns, conn("fist_guard", guardWindow), conn("fists_idle", cmp(tAA, ">=", 60))];
  } else if (name === "fist_guard") {
    c = [...useConns(), ...attackConns("fists"), ...punchConns, conn("fists_idle", cmp(tAA, ">=", 60))];
  } else if (name === "tool") {
    c = [...useConns(), ...attackConns("tool")];
  } else { // use nodes
    const typ = useTypes.filter(([b]) => name.startsWith(b)).map(([, t]) => t)[0];
    c = [...useConns(typ), ...attackConns()];
  }
  node.connections = structuredClone(c);
}

const actionLayer: Obj = { type: "KEYFRAME", when: NOT(state("SLEEPING")), entryNode: "idle", variables: { combo: 0, fist: 0 }, nodes: actionNodes,
                           // a left-handed player plays the hand-dependent items as their mirror image
                           mirror: { when: state("LEFT_HANDED"), negate: ["headYaw"],
                                     pairs: [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                                             ["renderLeftItemRotation", "renderRightItemRotation"]] } };

const groundAction = OR(action("stand"), action("walk"), action("sprint"));
function torchArm(side: string): Obj[] {
  const arm = side + "Arm";
  return [drv(arm, "X", "headPitch", { scale: 0.5, offset: -90, space: "OVERRIDE" }), drv(arm, "Y", "headYaw", { scale: 0.7 }),
          { animationKey: PL(`torch_forearm_${side}`) }];
}
const torchMain = { type: "core:property", property: "mainHandItem", value: "minecraft:torch" };
const torchOff = { type: "core:property", property: "offHandItem", value: "minecraft:torch" };
const player: Obj = {
  formatVersion: 2,
  layers: [
    // item rotations are reset every frame before the layers run (keeps their damping)
    { type: "KEYFRAME", entryNode: "reset", nodes: { reset: { type: "core:pose", pose: [{ animationKey: PL("reset_items") }] } } },
    { type: "KEYFRAME", entryNode: "stand", nodes },
    // sneaking overlay on the ground states
    { type: "KEYFRAME", when: AND(state("SNEAKING"), groundAction), entryNode: "sneak", nodes: { sneak: {
        type: "core:pose", tags: ["sneak"], pose: [
          { animationKey: PL("sneak_base"), time: limbTime,
            damping: { rightLeg: 1, leftLeg: 1, rightArm: 0.8, leftArm: 0.8, root: [null, 0.6, null], localOffset: 0.3 },
            vectorModes: { root: "RETARGET", localOffset: "SLIDE" } },
          { animationKey: PL("sneak_forelegs"), time: limbTime, damping: { leftForeLeg: 0.3, rightForeLeg: 0.3 } },
          { animationKey: PL("sneak_swing"), time: limbTime, weight: { variable: "limbSwingAmount" }, space: "POST" },
          { animationKey: PL("sneak_body"), time: limbTime, space: "POST" },
          { animationKey: PL("sneak_head"), time: limbTime, space: "PRE" },
        ] } } },
    // torch holding while standing or walking (not sprinting)
    { type: "KEYFRAME", when: AND(OR(action("stand"), action("walk")), OR(torchMain, torchOff)), entryNode: "torch", nodes: { torch: {
        type: "core:pose", tags: ["torch_holding"], pose: [
          // the main hand holds the torch if it has one, else the off hand; the arm follows the primary hand
          ...torchArm("right").map((x) => when(x, AND(torchMain, NOT(state("LEFT_HANDED"))))),
          ...torchArm("left").map((x) => when(x, AND(torchMain, state("LEFT_HANDED")))),
          ...torchArm("left").map((x) => when(x, AND(NOT(torchMain), torchOff, NOT(state("LEFT_HANDED"))))),
          ...torchArm("right").map((x) => when(x, AND(NOT(torchMain), torchOff, state("LEFT_HANDED")))),
        ] } } },
    // items and attacks (the BipedActionController)
    actionLayer,
    // the cape is physics, kept as a driver
    { type: "KEYFRAME", entryNode: "cape", nodes: { cape: { type: "core:pose", pose: [{ driver: "mobends:cape", bone: "cape" }] } } },
  ] };


// ---- squid: the vanilla tentacle wave over the interpolated squid rotation --------------------------
const SQ = (n: string) => clip("squid", n);
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
const squidTime = { variable: "squidRotation" };
const squid: Obj = { formatVersion: 2, layers: [{ type: "KEYFRAME", entryNode: "swim", nodes: { swim: { type: "core:pose", tags: ["swim"], pose: [
  when({ animationKey: SQ("swim_base"), time: squidTime, damping: squidBaseDamp }, state("SQUID_PREV_ROTATION_LOW")),
  when({ animationKey: SQ("swim_base_rest"), damping: squidBaseDamp }, NOT(state("SQUID_PREV_ROTATION_LOW"))),
  when({ animationKey: SQ("swim_sections"), time: squidTime, damping: squidSectionDamp }, state("SQUID_ROTATION_LOW")),
  when({ animationKey: SQ("swim_sections_rest"), damping: squidSectionDamp }, NOT(state("SQUID_ROTATION_LOW"))),
] } } }] };


// ---- spider: IK leg drivers for the ground gaits, data for the jump and the death ----------------------
const SP = (n: string) => clip("spider", n);
const withSnap = (item: Obj): Obj => ({ ...item, snap: true });
const spiderHead = [withSnap(drv("head", "X", "headPitch", { space: "OVERRIDE" })), drv("head", "Y", "headYaw")];
poseClip(join(CLIPS, "spider", "rest.json"), { centerRotation: rotations(), renderRotation: rotations() }, { localOffset: [0, 0, 0] });
const spiderRest = { animationKey: SP("rest"), damping: { localOffset: 1 }, vectorModes: { localOffset: "SLIDE" } };
const spiderLimbs = [
  { phase: 0, minDist: 20, maxDist: 10, minRot: -80, maxRot: -50 }, { phase: 0.3, minDist: 20, maxDist: 10, minRot: -80, maxRot: -50 },
  { phase: 0.3, minDist: 15, maxDist: 15, minRot: -30, maxRot: 10 }, { phase: 0, minDist: 15, maxDist: 15, minRot: -30, maxRot: 10 },
  { phase: 0.4, minDist: 7, maxDist: 15, minRot: 20, maxRot: 50 }, { phase: 0.7, minDist: 7, maxDist: 15, minRot: 20, maxRot: 50 },
  { phase: 0.7, minDist: 10, maxDist: 20, minRot: 60, maxRot: 80 }, { phase: 0.4, minDist: 10, maxDist: 20, minRot: 60, maxRot: 80 },
];
const bodyBob = (fn: string, sign: number) => ({ variable: "ticks", scale: 0.2, fn, mul: 0.4 * sign });
const spIdle: Obj = { type: "core:pose", tags: ["idle"], pose: [
  { driver: "mobends:spider_idle_legs", groundLevel: { variable: "ticks", scale: 0.1, fn: "sin", mul: 0.5 },
    bodyX: bodyBob("sin", 1), bodyZ: bodyBob("cos", 1), kneelDuration: 10, kneelAmplitude: 4, kneelLead: 0 },
  withSnap({ driver: "core:vector", bone: "root", x: bodyBob("sin", 1), y: { variable: "groundLevel", scale: -1 }, z: bodyBob("cos", -1) }),
  ...spiderHead, spiderRest,
] };
const spMove: Obj = { type: "core:pose", tags: ["move"], pose: [
  { driver: "mobends:spider_moving_legs", swing: { variable: "limbSwing", scale: 0.6662 },
    groundLevel: { variable: "ticks", scale: 0.6, fn: "mcsin", mul: 1.2 }, kneelDuration: 10, kneelAmplitude: 3, kneelLead: 0.2, limbs: spiderLimbs },
  withSnap({ driver: "core:vector", bone: "root", x: bodyBob("mcsin", 1), y: { variable: "groundLevel", scale: -1 }, z: bodyBob("mccos", -1) }),
  ...spiderHead, spiderRest,
] };
poseClip(join(CLIPS, "spider", "crawl_rest.json"), { centerRotation: rotations() }, { localOffset: [0, -10, 0] });
const spCrawl: Obj = { type: "core:pose", tags: ["crawl"], pose: [
  { driver: "mobends:spider_moving_legs", swing: { variable: "crawlProgress", scale: 5 },
    groundLevel: { variable: "crawlProgress", scale: 3, fn: "mcsin", mul: 1.2 }, limbs: spiderLimbs },
  ...spiderHead,
  withDamping(drv("renderRotation", "X", null, { const: -90, space: "OVERRIDE" }), { renderRotation: 0.6 }), drv("renderRotation", "Y", "crawlRenderYaw"),
  { animationKey: SP("crawl_rest"), damping: { localOffset: 0.5 }, vectorModes: { localOffset: "SLIDE" } },
] };
// jump: legs fan out to their natural yaw and bend with the vertical motion
function naturalYaw(i: number): number {
  const ny = -((i / 7) * 2 - 1);
  return degrees(i % 2 === 1 ? -ny * 1.3 : ny * 1.3);
}
poseClip(join(CLIPS, "spider", "jump.json"), Object.fromEntries(range(8).map((i) => [`leg${i + 1}`, rotations(["Y", naturalYaw(i)])])), { root: [0, 0, 0] });
const jumpMotion = (mul: number, add: number) => ({ variable: "interpolatedMotionY", scale: -5, min: -1, max: 1, mul, add });
const alternate = (i: number) => (i % 2 ? -1 : 1);
const spJump: Obj = { type: "core:pose", tags: ["jump"], pose: [
  withSnap({ animationKey: SP("jump"), bones: ["root"] }),
  { animationKey: SP("jump"), bones: range(8).map((i) => `leg${i + 1}`), damping: Object.fromEntries(range(8).map((i) => [`leg${i + 1}`, 1])) },
  ...range(8).map((i) => ({ ...drv(`leg${i + 1}`, "Z", null, { space: "POST" }), angle: jumpMotion(25 * alternate(i), -20 * alternate(i)) })),
  ...range(8).map((i) => withDamping({ ...drv(`foreLeg${i + 1}`, "Z", null, { space: "OVERRIDE" }), angle: jumpMotion(-40 * alternate(i), -70 * alternate(i)) },
                                     { [`foreLeg${i + 1}`]: 1 })),
  spiderRest,
] };
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
const amountDeg = { variable: "limbSwingAmount", scale: 180 / Math.PI };
const spDeath: Obj = { type: "core:pose", tags: ["death"], pose: [
  { driver: "core:accumulate", name: "wiggleSpeed", rate: -0.1, initial: 1, min: 0 },
  { driver: "core:accumulate", name: "wigglePhase", rate: { variable: "wiggleSpeed", scale: 2, offset: 0.3 } },
  { driver: "core:vector", bone: "root", y: 10, damping: { root: [null, 0.3, null] }, vectorModes: { root: "SLIDE" } },
  ...spiderHead,
  withSnap({ animationKey: SP("death") }),
  { animationKey: SP("death_sway_y"), time: { variable: "limbSwing", scale: 0.6662 }, weight: amountDeg, space: "PRE" },
  { animationKey: SP("death_sway_z"), time: { variable: "limbSwing", scale: 0.6662 }, weight: amountDeg, space: "PRE" },
  { animationKey: SP("death_wiggle"), time: { variable: "wigglePhase" }, weight: { variable: "wiggleSpeed", scale: 10, offset: 10 }, space: "PRE" },
] };
// the controller's decision chain, guarded like the player's
const spiderChain: [string, Obj | null][] = [
  ["death", cmp("health", "<=", 0)],
  ["crawl", state("BESIDE_CLIMBABLE")],
  ["jump", jumping],
  ["idle", state("STANDING_STILL")],
  ["move", null],
];
const spNodes: Record<string, Obj> = { idle: spIdle, move: spMove, jump: spJump, crawl: spCrawl, death: spDeath };
for (const [name, node] of Object.entries(spNodes)) {
  const conns: Obj[] = [];
  const prior: Obj[] = [];
  for (const [target, cond] of spiderChain) {
    const parts = [...prior.map((c) => NOT(c)), ...(cond !== null ? [cond] : [])];
    if (target !== name) {
      const c: Obj = { target, triggerCondition: parts.length === 1 ? parts[0] : AND(...parts) };
      if (name === "jump" && ["idle", "move"].includes(target)) {
        c.set = { resetLimbs: 1 }; // feet re-planted under the body after a jump
      }
      conns.push(c);
    }
    if (cond !== null) {
      prior.push(cond);
    }
  }
  node.connections = conns;
}
const spider: Obj = { formatVersion: 2, layers: [{ type: "KEYFRAME", entryNode: "idle", variables: { resetLimbs: 1 }, nodes: spNodes }] };

// the skeleton's controller runs the biped action controller as well: bow, sword, tool, fists
const skeletonActions = structuredClone(actionLayer);
delete skeletonActions.when; // only players sleep
skeleton.layers.push(skeletonActions);

// the zombie villager's controller is the zombie's
const zombieVillager: Obj = { formatVersion: 2, extends: "mobends:bends/animators/zombie.json" };


// ---- mobs described by model definitions (bends/models/*.json): generic walkers ---------------------
// Their legs are split at the knee by the definition; the gait swings the upper segment like the
// vanilla model did and bends the lower one when the leg trails.
type Leg = [upper: string, lower: string | null, phase: number];
const W = (folder: string, n: string) => clip(folder, n);
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
  return { animationKey: W(folder, "walk"), time: limbTime, weight: { variable: "limbSwingAmount" },
           damping: Object.fromEntries(legBones(legs).map((b) => [b, 0.8])) };
}

/** A slow breathing sway of the listed bones on the tick clock. */
function walkerIdle(folder: string, bones: [string, number][]): Obj {
  cycleClip(join(CLIPS, folder, "idle.json"), (p) => Object.fromEntries(bones.map(([b, amp]) => [b, rotations(["X", mcCos(p) * amp])])));
  return { animationKey: W(folder, "idle"), time: { variable: "ticks", scale: 0.09 }, damping: Object.fromEntries(bones.map(([b]) => [b, 0.5])) };
}

function walkerJump(folder: string, legs: Leg[], upperAngle = -20, lowerAngle = 35): Obj {
  poseClip(join(CLIPS, folder, "jump.json"), {
    ...Object.fromEntries(legs.map(([upper]) => [upper, rotations(["X", upperAngle])])),
    ...Object.fromEntries(legs.filter(([, lower]) => lower).map(([, lower]) => [lower!, rotations(["X", lowerAngle])])) });
  return { animationKey: W(folder, "jump"), damping: Object.fromEntries(legBones(legs).map((b) => [b, 0.3])) };
}

function walkerAnimator(folder: string, legs: Leg[], idleBones: [string, number][], extra: Obj[] = [], headBone = "head"): Obj {
  const look = [withDamping(drv(headBone, "Y", "headYaw"), { [headBone]: 0.5 }), drv(headBone, "X", "headPitch", { space: "POST" })];
  const stand = { type: "core:pose", tags: ["stand"], pose: [walkerIdle(folder, idleBones), ...look, ...extra],
                  connections: [{ target: "jump", triggerCondition: jumping }, { target: "walk", triggerCondition: state("MOVING_HORIZONTALLY") }] };
  const walk = { type: "core:pose", tags: ["walk"], pose: [walkerGait(folder, legs), ...look, ...extra],
                 connections: [{ target: "jump", triggerCondition: jumping }, { target: "stand", triggerCondition: state("STANDING_STILL") }] };
  const jump = { type: "core:pose", tags: ["jump"], pose: [walkerJump(folder, legs), ...look, ...extra],
                 connections: [{ target: "stand", triggerCondition: AND(grounded, state("STANDING_STILL")) }, { target: "walk", triggerCondition: AND(grounded, state("MOVING_HORIZONTALLY")) }] };
  return { formatVersion: 2, layers: [{ type: "KEYFRAME", entryNode: "stand", nodes: { stand, walk, jump } }] };
}

// vanilla ModelQuadruped: legs 1 and 4 swing together, 2 and 3 opposite
const quadLegs: Leg[] = [["leg1", "foreLeg1", 0.0], ["leg2", "foreLeg2", Math.PI], ["leg3", "foreLeg3", Math.PI], ["leg4", "foreLeg4", 0.0]];
const quadruped = walkerAnimator("quadruped", quadLegs, [["head", 2.0], ["body", 1.0]]);
const creeper = walkerAnimator("creeper", quadLegs, [["head", 1.5]]);
const villager = walkerAnimator("villager", [["rightLeg", "foreRightLeg", 0.0], ["leftLeg", "foreLeftLeg", Math.PI]], [["head", 1.5], ["arms", 2.0]]);
const chickenWings = [withDamping(drv("rightWing", "Z", "wingAngle", { space: "OVERRIDE" }), { rightWing: 1 }),
                      withDamping(drv("leftWing", "Z", "wingAngle", { scale: -1, space: "OVERRIDE" }), { leftWing: 1 })];
const chicken = walkerAnimator("chicken", [["rightLeg", "foreRightLeg", 0.0], ["leftLeg", "foreLeftLeg", Math.PI]], [["head", 2.0]], chickenWings);


// iron golem: the vanilla gait is a triangle wave of period 13 on the limb swing; arms and legs get
// middle joints, and the attack swing comes from the entity's attack timer.
function triangleWave(f: number, period: number): number {
  return (Math.abs(mod(f, period) - period * 0.5) - period * 0.25) / (period * 0.25);
}
const golemArmDamp = { rightArm: 0.8, leftArm: 0.8, rightForeArm: 0.6, leftForeArm: 0.6 };
const golemLegDamp = { rightLeg: 0.8, leftLeg: 0.8, rightForeLeg: 0.6, leftForeLeg: 0.6 };
function golemWalk(t: number): Record<string, Quaternion> {
  const w = triangleWave(t, 13);
  return {
    leftLeg: rotations(["X", -60 * w]), rightLeg: rotations(["X", 60 * w]),
    leftForeLeg: rotations(["X", (1 + w) * 0.5 * 30]), rightForeLeg: rotations(["X", (1 - w) * 0.5 * 30]),
    rightArm: rotations(["X", -11.5 + 60 * w]), leftArm: rotations(["X", -11.5 - 60 * w]),
    rightForeArm: rotations(["X", -(1 + w) * 0.5 * 25]), leftForeArm: rotations(["X", -(1 - w) * 0.5 * 25]),
  };
}
curveClip(join(CLIPS, "iron_golem", "walk.json"), golemWalk, range(105).map((k) => 13 * k / 104), 13, true);
curveClip(join(CLIPS, "iron_golem", "attack.json"), (t) => ({
  rightArm: rotations(["X", degrees(-2 + 1.5 * triangleWave(t, 10))]), leftArm: rotations(["X", degrees(-2 + 1.5 * triangleWave(t, 10))]),
  rightForeArm: rotations(["X", -20]), leftForeArm: rotations(["X", -20]) }), range(81).map((k) => 10 * k / 80), 10);
cycleClip(join(CLIPS, "iron_golem", "idle.json"), (p) => ({ head: rotations(["X", mcCos(p) * 1.5]), body: rotations(["X", mcCos(p + 1) * 0.7]) }));
poseClip(join(CLIPS, "iron_golem", "jump.json"), { leftLeg: rotations(["X", -15]), rightLeg: rotations(["X", -15]), leftForeLeg: rotations(["X", 25]), rightForeLeg: rotations(["X", 25]) });
const golemLook = [withDamping(drv("head", "Y", "headYaw"), { head: 0.5 }), drv("head", "X", "headPitch", { space: "POST" })];
const golemAttack = when({ animationKey: W("iron_golem", "attack"), time: { variable: "attackTimer" }, damping: golemArmDamp }, cmp("attackTimer", ">", 0));
const golemNodes = {
  stand: { type: "core:pose", tags: ["stand"], pose: [{ animationKey: W("iron_golem", "idle"), time: { variable: "ticks", scale: 0.07 }, damping: { head: 0.5, body: 0.5 } }, ...golemLook, golemAttack],
           connections: [{ target: "jump", triggerCondition: jumping }, { target: "walk", triggerCondition: state("MOVING_HORIZONTALLY") }] },
  walk: { type: "core:pose", tags: ["walk"], pose: [{ animationKey: W("iron_golem", "walk"), time: { variable: "limbSwing" }, weight: { variable: "limbSwingAmount" }, damping: { ...golemArmDamp, ...golemLegDamp } }, ...golemLook, golemAttack],
          connections: [{ target: "jump", triggerCondition: jumping }, { target: "stand", triggerCondition: state("STANDING_STILL") }] },
  jump: { type: "core:pose", tags: ["jump"], pose: [{ animationKey: W("iron_golem", "jump"), damping: golemLegDamp }, ...golemLook, golemAttack],
          connections: [{ target: "stand", triggerCondition: AND(grounded, state("STANDING_STILL")) }, { target: "walk", triggerCondition: AND(grounded, state("MOVING_HORIZONTALLY")) }] },
};
const ironGolem: Obj = { formatVersion: 2, layers: [{ type: "KEYFRAME", entryNode: "stand", nodes: golemNodes }] };

const animators: [string, Obj][] = [
  ["biped", biped], ["zombie", zombie], ["skeleton", skeleton], ["pig_zombie", pigZombie], ["player", player], ["squid", squid], ["spider", spider], ["zombie_villager", zombieVillager],
  ["quadruped", quadruped], ["creeper", creeper], ["villager", villager], ["chicken", chicken], ["iron_golem", ironGolem],
];
for (const [name, data] of animators) {
  writeFileSync(join(ANIM, name + ".json"), JSON.stringify(data, null, 2));
  console.log("wrote", name + ".json");
}
