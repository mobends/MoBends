#!/usr/bin/env python3
"""Generates the biped animator JSON files (biped, zombie, skeleton, pig zombie).

The animators are plain data; this script only spares us from writing the shared structure by
hand and from computing the quaternions of hand-authored constant poses. Run it through
`gradle generateAnimators` (or directly with the mod's resources dir as the argument).
"""
import json, math, os, sys

RES = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources')
ANIM = os.path.join(RES, 'assets', 'mobends', 'bends', 'animators')
CLIPS = os.path.join(RES, 'assets', 'mobends', 'bends', 'animations')
os.makedirs(ANIM, exist_ok=True)

def clip(folder, name):
    return f"mobends:bends/animations/{folder}/{name}.json"

# ---- condition helpers ----------------------------------------------------------------------
def cmp(var, op, val): return {"type": "core:compare", "variable": var, "op": op, "value": val}
def state(s): return {"type": "core:state", "state": s}
def action(tag): return {"type": "core:action", "tag": tag}
def AND(*c): return {"type": "core:and", "conditions": list(c)}
def OR(*c): return {"type": "core:or", "conditions": list(c)}
def NOT(c): return {"type": "core:not", "condition": c}

# ---- quaternion helpers for hand-authored constant poses -------------------------------------
def axis(a, deg):
    h = math.radians(deg) / 2
    x, y, z = {'X': (1, 0, 0), 'Y': (0, 1, 0), 'Z': (0, 0, 1)}[a]
    s = math.sin(h)
    return [x * s, y * s, z * s, math.cos(h)]

def mul(a, b):
    ax, ay, az, aw = a; bx, by, bz, bw = b
    return [ax*bw + aw*bx + ay*bz - az*by,
            ay*bw + aw*by + az*bx - ax*bz,
            az*bw + aw*bz + ax*by - ay*bx,
            aw*bw - ax*bx - ay*by - az*bz]

def rotations(*calls):
    """rotate*(a).rotate*(b)... in the procedural order: each call pre-multiplies (q = R * q)."""
    q = [0, 0, 0, 1]
    for a, deg in calls:
        q = mul(axis(a, deg), q)
    return [round(v, 7) for v in q]

def pose_clip(path, bones, vectors=None):
    data = {"bones": {}, "duration": 0, "loop": False}
    for bone, q in bones.items():
        data["bones"][bone] = {"keyframes": [{"position": [0, 0, 0], "rotation": q, "scale": [1, 1, 1]}]}
    for bone, v in (vectors or {}).items():
        data["bones"][bone] = {"keyframes": [{"position": v, "rotation": [0, 0, 0, 1], "scale": [1, 1, 1]}]}
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, 'w'))

# ---- shared biped locomotion ------------------------------------------------------------------
B = lambda n: clip('biped', n)
jumping = OR(state("AIRBORNE"), cmp("ticksAfterTouchdown", "<", 1))
grounded = AND(state("ON_GROUND"), cmp("ticksAfterTouchdown", ">=", 1))
limbTime = {"variable": "limbSwing", "scale": 0.6662}
headLook = [
    {"driver": "core:axis_rotate", "bone": "head", "axis": "Y", "angle": {"variable": "headYaw"}, "space": "PRE"},
    {"driver": "core:axis_rotate", "bone": "head", "axis": "X", "angle": {"variable": "headPitch"}, "space": "POST"},
]
kneel = {"animationKey": B("kneel"), "time": {"variable": "ticksAfterTouchdown"},
         "when": cmp("ticksAfterTouchdown", "<", 1 / 0.15), "damping": {"body": 1.0}, "vectorModes": {"root": "SNAP"}}
resetDamping = {"root": 0.3, "localOffset": 0.3, "renderRotation": 0.3, "centerRotation": 0.3,
                "renderRightItemRotation": 0.3, "renderLeftItemRotation": 0.3}

stand = {
    "type": "core:pose", "tags": ["stand"],
    "enterPose": [{"animationKey": B("stand_enter"), "when": cmp("ticksAfterTouchdown", "<", 0.5 / 0.15)}],
    "pose": [
        {"animationKey": B("stand"), "time": {"variable": "ticks", "scale": 0.1},
         "damping": dict(resetDamping, body=1.0, rightArm=0.4, leftArm=0.4),
         "vectorModes": {"root": "SLIDE", "localOffset": "SLIDE"}},
        *headLook,
        kneel,
    ],
    "connections": [
        {"target": "jump", "triggerCondition": jumping},
        {"target": "walk", "triggerCondition": state("MOVING_HORIZONTALLY")},
    ]}
walk = {
    "type": "core:pose", "tags": ["walk"],
    "pose": [
        {"animationKey": B("walk_base"), "time": limbTime,
         "damping": dict(resetDamping, body=0.5, head=0.5, rightArm=0.8, leftArm=0.8, rightForeArm=0.8, leftForeArm=0.8,
                         rightLeg=1.0, leftLeg=1.0, root=[0.3, 0.6, 0.3]),
         "vectorModes": {"root": "RETARGET", "localOffset": "SLIDE"}},
        {"animationKey": B("walk_forelegs"), "time": limbTime, "damping": {"leftForeLeg": 0.5, "rightForeLeg": 0.5}},
        {"animationKey": B("walk_swing"), "time": limbTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST"},
        {"driver": "core:axis_rotate", "bone": "body", "axis": "Z", "angle": {"variable": "headYaw", "scale": -0.1, "min": -10, "max": 10}, "space": "PRE"},
        *headLook,
        kneel,
    ],
    "connections": [
        {"target": "jump", "triggerCondition": jumping},
        {"target": "stand", "triggerCondition": state("STANDING_STILL")},
    ]}
jump = {
    "type": "core:pose", "tags": ["jump"],
    "enterPose": [{"animationKey": B("jump_enter")}],
    "pose": [
        {"animationKey": B("jump"), "time": {"variable": "ticksInAir"},
         "damping": dict(resetDamping, centerRotation=0.7, body=0.2, rightArm=0.05, leftArm=0.05, rightForeArm=0.3, leftForeArm=0.3),
         "vectorModes": {"root": "SLIDE"}},
        *headLook,
        {"animationKey": B("jump_moving_base"), "time": limbTime, "when": state("MOVING_HORIZONTALLY"),
         "damping": {"rightLeg": 1.0, "leftLeg": 1.0, "leftForeArm": 0.3, "rightForeArm": 0.3}},
        {"animationKey": B("jump_moving_forelegs"), "time": limbTime, "when": state("MOVING_HORIZONTALLY"),
         "damping": {"leftForeLeg": 0.3, "rightForeLeg": 0.3}},
        {"animationKey": B("jump_moving_swing"), "time": limbTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST", "when": state("MOVING_HORIZONTALLY")},
        {"animationKey": B("jump_still"), "when": state("STANDING_STILL"),
         "damping": {"rightLeg": 0.3, "leftLeg": 0.3, "rightForeLeg": 0.3, "leftForeLeg": 0.3}},
    ],
    "connections": [
        {"target": "stand", "triggerCondition": AND(grounded, state("STANDING_STILL"))},
        {"target": "walk", "triggerCondition": AND(grounded, state("MOVING_HORIZONTALLY"))},
        {"target": "jump", "triggerCondition": AND(cmp("prevMotionY", "<", 0), cmp("motionY", ">", 0))},
    ]}

biped = {
    "formatVersion": 2,
    "layers": [
        {"type": "KEYFRAME", "entryNode": "stand", "nodes": {"stand": stand, "walk": walk, "jump": jump}},
    ]}

# ---- zombie: animation sets -----------------------------------------------------------------------
Z = lambda n: clip('zombie', n)
zombie = {
    "formatVersion": 2,
    "extends": "mobends:bends/animators/biped.json",
    "layers": [
        {"type": "KEYFRAME", "mode": "ADDITIVE", "additiveSpace": {"default": "PRE", "body": "POST", "root": "OVERRIDE"},
         "when": cmp("animationSet", "==", 0), "entryNode": "lean", "nodes": {"lean": {
            "type": "core:pose", "tags": ["lean"],
            "pose": [
                {"animationKey": Z("lean"), "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
                {"animationKey": Z("lean_arms_up"), "space": "OVERRIDE", "when": AND(state("MOVING_HORIZONTALLY"), cmp("currentWalkingState", "==", 1))},
            ]}}},
        {"type": "KEYFRAME", "when": cmp("animationSet", "==", 1), "entryNode": "stumble", "nodes": {"stumble": {
            "type": "core:pose", "tags": ["stumbling"],
            "pose": [
                {"animationKey": Z("stumble_base"), "time": limbTime,
                 "damping": {"rightLeg": 1.0, "leftLeg": 1.0, "rightArm": 1.0, "leftArm": 1.0, "body": 0.5}},
                {"animationKey": Z("stumble_swing"), "time": limbTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST"},
                {"animationKey": Z("stumble_head"), "time": limbTime, "space": "PRE"},
            ]}}},
    ]}

# ---- skeleton: strafing legs ------------------------------------------------------------------------
S = lambda n: clip('skeleton', n)
skeleton = {
    "formatVersion": 2,
    "extends": "mobends:bends/animators/biped.json",
    "layers": [
        {"type": "KEYFRAME", "when": AND(action("walk"), state("STRAFING")), "entryNode": "strafe", "nodes": {"strafe": {
            "type": "core:pose", "tags": ["strafe"],
            "pose": [
                {"animationKey": S("strafe_base"), "time": limbTime, "damping": {"rightLeg": 1.0, "leftLeg": 1.0}},
                {"animationKey": S("strafe_swing"), "time": limbTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST"},
            ]}}},
    ]}

# ---- pig zombie: hunched pose + slash attack -------------------------------------------------------
P = lambda n: clip('pigzombie', n)
# The constant pose is the rotate* / localRotate* calls of the pig zombie stand/walk bits.
pose_clip(os.path.join(CLIPS, 'pigzombie', 'pose_post.json'), {"body": rotations(('X', 20))})
pose_clip(os.path.join(CLIPS, 'pigzombie', 'pose_pre.json'), {
    "body": rotations(('Z', -10)),
    "head": rotations(('X', -20)),
    "rightArm": rotations(('X', -20), ('Z', 10)),
    "leftArm": rotations(('X', -20), ('Z', 10)),
    "rightLeg": rotations(('Z', 10), ('X', -30)),
    "leftLeg": rotations(('Z', -10), ('X', -10), ('Y', -10)),
    "rightForeLeg": rotations(('X', 25)),
    "leftForeLeg": rotations(('X', 25)),
})
pose_clip(os.path.join(CLIPS, 'pigzombie', 'stand_offset.json'), {}, {"root": [0, -3, 0]})
pig_zombie = {
    "formatVersion": 2,
    "extends": "mobends:bends/animators/biped.json",
    "layers": [
        {"type": "KEYFRAME", "mode": "ADDITIVE", "additiveSpace": {"default": "PRE", "root": "OVERRIDE"},
         "when": OR(action("stand"), action("walk")), "entryNode": "hunch", "nodes": {"hunch": {
            "type": "core:pose", "tags": ["hunch"],
            "pose": [
                {"animationKey": P("pose_post"), "space": "POST"},
                {"animationKey": P("pose_pre"), "space": "PRE"},
                {"animationKey": P("stand_offset"), "when": action("stand"), "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
                {"animationKey": P("walk_bob"), "time": limbTime, "when": action("walk"), "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
            ]}}},
        {"type": "KEYFRAME", "when": cmp("entitySwingProgress", ">", 0), "entryNode": "slash", "nodes": {"slash": {
            "type": "core:pose", "tags": ["attack", "attack_slash_inward"],
            "pose": [
                {"animationKey": P("slash"), "time": {"variable": "ticksAfterAttack"},
                 "damping": {"body": 0.9, "head": 0.9, "rightArm": 0.9, "leftArm": 0.3, "rightForeArm": 0.3, "leftForeArm": 0.3, "localOffset": 0.3},
                 "vectorModes": {"localOffset": "SLIDE"}},
                *headLook,
                {"animationKey": P("slash_still"), "when": AND(state("STANDING_STILL"), NOT(state("RIDING"))),
                 "damping": {"renderRotation": 0.3, "root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
                {"driver": "core:axis_rotate", "bone": "renderRightItemRotation", "axis": "X", "angle": 50, "space": "OVERRIDE", "snap": True, "damping": {"renderRightItemRotation": 0.9}},
            ]}}},
    ]}


# ---- player ----------------------------------------------------------------------------------------
PL = lambda n: clip('player', n)

def mc_cos(v):
    """Minecraft's table-based cosine, so analytic clips match the procedural output exactly."""
    i = (int(v * 10430.378) + 16384) & 65535
    return math.sin(i * math.pi * 2 / 65536)

def cycle_clip(path, bone_fn, count=64, period=2 * math.pi):
    """Generates a looping clip from a function phase -> {bone: quaternion}."""
    frames = [bone_fn(period * i / count if i < count else 0.0) for i in range(count + 1)]
    data = {"bones": {}, "duration": period, "loop": True}
    for bone in frames[0]:
        data["bones"][bone] = {"keyframes": [{"position": [0, 0, 0], "rotation": f[bone], "scale": [1, 1, 1]} for f in frames]}
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, 'w'))

# The underwater arm pose is Rx(c) * Ry(-90 t) * Rx(a) with a, c on the tick clock and t a ramp;
# the two clock-driven parts are generated here so the ramp can sit between them.
arm_sway = lambda phase: (mc_cos(phase) + 1) / 2
cycle_clip(os.path.join(CLIPS, 'player', 'swim_deep_arm_inner.json'),
           lambda p: {"leftArm": rotations(('X', arm_sway(p) * -120)), "rightArm": rotations(('X', arm_sway(p) * -120))})
cycle_clip(os.path.join(CLIPS, 'player', 'swim_deep_arm_outer.json'),
           lambda p: {"leftArm": rotations(('X', arm_sway(p) * 20)), "rightArm": rotations(('X', arm_sway(p) * 20))})

def drv(bone, ax, var=None, scale=1, offset=0, space="PRE", const=None, **kw):
    angle = const if const is not None else dict({"variable": var, "scale": scale, "offset": offset}, **kw)
    return {"driver": "core:axis_rotate", "bone": bone, "axis": ax, "angle": angle, "space": space}

def with_damping(item, **damping):
    item = dict(item)
    item["damping"] = {k: v for k, v in damping.items()}
    return item

def when(item, cond):
    item = dict(item); item["when"] = cond; return item

resetItems = {"renderRightItemRotation": rotations(), "renderLeftItemRotation": rotations()}
pose_clip(os.path.join(CLIPS, 'player', 'reset_items.json'), resetItems)
pose_clip(os.path.join(CLIPS, 'player', 'torch_forearm_right.json'), {"rightForeArm": rotations(('X', -5))})
pose_clip(os.path.join(CLIPS, 'player', 'torch_forearm_left.json'), {"leftForeArm": rotations(('X', -5))})
pose_clip(os.path.join(CLIPS, 'player', 'elytra.json'), {
    "body": rotations(), "leftForeArm": rotations(), "rightForeArm": rotations(),
    "leftLeg": rotations(('Z', -5)), "rightLeg": rotations(('Z', 5)),
    "leftForeLeg": rotations(), "rightForeLeg": rotations(),
    "centerRotation": rotations(), "renderRotation": rotations(), "head": rotations(),
    "leftArm": rotations(), "rightArm": rotations()}, {"root": [0, 0, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'fly_common.json'), {"renderRotation": rotations()}, {"root": [0, 0, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'fly_sprint.json'), {
    "leftForeArm": rotations(), "rightForeArm": rotations(), "leftLeg": rotations(('Z', -5)), "rightLeg": rotations(('Z', 5)),
    "leftForeLeg": rotations(), "rightForeLeg": rotations(), "body": rotations(), "head": rotations(), "leftArm": rotations(), "rightArm": rotations(), "centerRotation": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'fly_hover_rest.json'), {"centerRotation": rotations(), "head": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'fly_moving.json'), {
    "centerRotation": rotations(), "body": rotations(), "leftArm": rotations(), "rightArm": rotations(),
    "leftForeArm": rotations(), "rightForeArm": rotations(),
    "leftLeg": rotations(('X', -45)), "rightLeg": rotations(('X', -6)), "leftForeLeg": rotations(('X', 30)), "rightForeLeg": rotations(('X', 10)), "head": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'falling_head.json'), {"head": rotations(('X', -20))})
pose_clip(os.path.join(CLIPS, 'player', 'falling_rest.json'), {"body": rotations(), "centerRotation": rotations(), "head": rotations()})
for leg in ("right", "left"):
    m = 1 if leg == "right" else -1
    main, off = ("right", "left") if leg == "right" else ("left", "right")
    pose_clip(os.path.join(CLIPS, 'player', f'sprint_jump_{leg}.json'), {
        "body": rotations(('Y', 20 * m)),
        "rightLeg": rotations(('Z', 5), ('X', -45 * m)), "leftLeg": rotations(('Z', -5), ('X', 45 * m)),
        "rightArm": rotations(('Z', 10), ('X', 50 * m)), "leftArm": rotations(('Z', -10), ('X', -50 * m)),
        "centerRotation": rotations(), "head": rotations()}, {"root": [0, 0, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'ladder_rest.json'), {"centerRotation": rotations(), "head": rotations(), "renderRotation": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'ladder_ledge_forearms.json'), {"leftForeArm": rotations(('X', -10)), "rightForeArm": rotations(('X', -10))})
pose_clip(os.path.join(CLIPS, 'player', 'swim_common.json'), {"head": rotations(), "renderRotation": rotations()}, {"localOffset": [0, 0, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'riding_head.json'), {"head": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'riding_moving_head.json'), {"head": rotations(('X', -25))})

# --- locomotion nodes shared with bipeds, plus the player's head override while attacking -----------
attackHead = [
    when({"animationKey": PL("riding_head"), "damping": {"head": 0.5}}, cmp("ticksAfterAttack", "<", 10)),
    when(drv("head", "Y", "headYaw", space="PRE"), cmp("ticksAfterAttack", "<", 10)),
    when(drv("head", "X", "headPitch", space="POST"), cmp("ticksAfterAttack", "<", 10)),
]
import copy
p_walk = copy.deepcopy(walk); p_walk["pose"] += attackHead
sprintTime = {"variable": "limbSwing", "scale": 0.6662 * 0.8}
p_sprint = {
    "type": "core:pose", "tags": ["sprint"],
    "pose": [
        {"animationKey": PL("sprint_base"), "time": sprintTime,
         "damping": dict(resetDamping, body=0.8, head=0.5, rightArm=0.8, leftArm=0.8, rightLeg=1.0, leftLeg=1.0,
                         rightForeLeg=0.7, leftForeLeg=0.7, root=[0.1, 0.9, 0.1]),
         "vectorModes": {"root": "RETARGET", "localOffset": "SLIDE"}},
        {"animationKey": PL("sprint_forearms"), "time": sprintTime, "damping": {"leftForeArm": 0.3, "rightForeArm": 0.3}},
        {"animationKey": PL("sprint_swing"), "time": sprintTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST"},
        drv("body", "Z", "headYaw", scale=-0.3, min=-10, max=10),
        *headLook,
        *attackHead,
    ]}
groundCond = AND(grounded, NOT(state("CLIMBING")), NOT(state("IN_WATER")), NOT(state("ELYTRA_FLYING")), NOT(state("RIDING")), NOT(state("SLEEPING")))
airCond = AND(jumping, NOT(state("CLIMBING")), NOT(state("IN_WATER")), NOT(state("ELYTRA_FLYING")), NOT(state("RIDING")), NOT(state("SLEEPING")))
def player_connections(exclude):
    """The PlayerController decision tree, in priority order, as connections. Each branch is
    guarded by the negation of the branches above it, so a node whose own condition still holds
    never falls through to a lower-priority target."""
    sleeping = state("SLEEPING")
    riding = state("RIDING")
    elytra = state("ELYTRA_FLYING")
    climbing = state("CLIMBING")
    inWater = state("IN_WATER")
    groups = [
        (sleeping, [("sleeping", None)]),
        (riding, [("riding", state("RIDING_LIVING")), ("sitting", NOT(state("RIDING_LIVING")))]),
        (elytra, [("elytra", None)]),
        (climbing, [("ladder", None)]),
        (inWater, [("swimming", None)]),
        (jumping, [
            ("flying", state("FLYING")),
            ("falling", AND(NOT(state("FLYING")), cmp("ticksFalling", ">", 10))),
            ("sprint_jump", AND(NOT(state("FLYING")), cmp("ticksFalling", "<=", 10), state("SPRINTING"))),
            ("jump", AND(NOT(state("FLYING")), cmp("ticksFalling", "<=", 10), NOT(state("SPRINTING")))),
        ]),
        (None, [
            ("stand", state("STANDING_STILL")),
            ("sprint", AND(state("MOVING_HORIZONTALLY"), state("SPRINTING"))),
            ("walk", AND(state("MOVING_HORIZONTALLY"), NOT(state("SPRINTING")))),
        ]),
    ]
    conns = []
    prior = []
    for group, branches in groups:
        for target, branch in branches:
            parts = [NOT(p) for p in prior]
            if group is not None: parts.append(group)
            if branch is not None: parts.append(branch)
            cond = parts[0] if len(parts) == 1 else AND(*parts)
            if target != exclude:
                conns.append({"target": target, "triggerCondition": cond})
        if group is not None:
            prior.append(group)
    return conns

p_stand = copy.deepcopy(stand)
p_jump = copy.deepcopy(jump)
# the biped jump's self-restart stays last
jumpRestart = [c for c in p_jump["connections"] if c["target"] == "jump"]

p_sleeping = {"type": "core:pose", "tags": ["sleeping"], "pose": [
    {"animationKey": PL("sleeping"), "time": {"variable": "ticks", "scale": 0.1},
     "damping": dict(resetDamping, head=1.0, rightArm=0.4, leftArm=0.4), "vectorModes": {"root": "SLIDE", "localOffset": "SLIDE"}}]}
p_sitting = {"type": "core:pose", "tags": ["sitting"], "pose": [
    {"animationKey": PL("sitting"), "damping": {"centerRotation": 0.3, "body": 0.5}}, *headLook]}
p_riding = {"type": "core:pose", "tags": ["riding"], "pose": [
    {"animationKey": PL("riding"), "damping": {"centerRotation": 0.3, "body": 0.5, "localOffset": 0.3}, "vectorModes": {"localOffset": "SLIDE"}},
    *headLook,
    drv("body", "Z", "ridingRelativeHeadYaw", scale=-0.25, min=-20, max=20, space="OVERRIDE"),
    {"animationKey": PL("riding_legs"), "time": {"variable": "ridingRelativeYaw", "offset": 180}},
    # the moving clip carries the head as Rx(-25) sampled at zero look; that part is applied in PRE space below
    when({"animationKey": PL("riding_moving"), "bones": ["body", "leftArm", "rightArm", "leftForeArm", "rightForeArm"]}, state("MOVING_HORIZONTALLY")),
    when({"animationKey": PL("riding_moving_head"), "space": "PRE"}, AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", "<=", 0.01))),
    when({"animationKey": PL("riding_fast"), "time": {"variable": "ticks", "scale": 0.5}, "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
         AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01))),
]}
# riding_fast carries head as an absolute value (Rx(-body) sampled at zero look); it must compose
# after the look drivers, so it is split: body/arms/root absolute, head PRE.
p_riding["pose"][-1] = when({"animationKey": PL("riding_fast"), "bones": ["body", "leftArm", "rightArm", "root"], "time": {"variable": "ticks", "scale": 0.5},
                             "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}}, AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01)))
p_riding["pose"].append(when({"animationKey": PL("riding_fast"), "bones": ["head"], "time": {"variable": "ticks", "scale": 0.5}, "space": "PRE"},
                             AND(state("MOVING_HORIZONTALLY"), cmp("entityXZSpeed", ">", 0.01))))

p_elytra = {"type": "core:pose", "tags": ["elytra"], "pose": [
    {"animationKey": PL("elytra"), "damping": {"head": 1.0, "body": 0.7, "leftArm": 0.7, "rightArm": 0.7, "leftForeArm": 0.7, "rightForeArm": 0.7,
                                               "leftLeg": 0.7, "rightLeg": 0.7, "leftForeLeg": 0.7, "rightForeLeg": 0.7, "centerRotation": 1.0, "renderRotation": 0.7, "root": 0.7},
     "vectorModes": {"root": "SLIDE"}},
    drv("head", "Y", "headYaw", space="OVERRIDE"), drv("head", "X", const=-90),
    drv("leftArm", "Z", const=-60), drv("leftArm", "Z", "flightSpeedFactor", scale=55), drv("leftArm", "Z", "headYawAbs", scale=-0.5),
    drv("rightArm", "Z", const=60), drv("rightArm", "Z", "flightSpeedFactor", scale=-55), drv("rightArm", "Z", "headYawAbs", scale=0.5),
]}

flySprint = AND(state("SPRINTING"), NOT(state("DRAWING_BOW")), cmp("ticksAfterAttack", ">=", 10))
flyHover = AND(NOT(flySprint), cmp("motionMagnitude", "<", 0.1))
flyMoving = AND(NOT(flySprint), cmp("motionMagnitude", ">=", 0.1))
bodyRotXDrv = lambda bone, sign, space: drv(bone, "X", "headPitch", scale=0.8 * sign, min=min(0, -60 * sign), max=max(0, -60 * sign), space=space)
p_flying = {"type": "core:pose", "tags": ["flying"], "pose": [
    {"animationKey": PL("fly_common"), "damping": {"renderRotation": 0.7, "root": 0.7}, "vectorModes": {"root": "SLIDE"}},
    # sprint-flying
    when({"animationKey": PL("fly_sprint"), "damping": {"centerRotation": 1.0, "head": 1.0, "body": 0.7, "leftArm": 0.7, "rightArm": 0.7, "leftForeArm": 0.7, "rightForeArm": 0.7,
                                                        "leftLeg": 0.7, "rightLeg": 0.7, "leftForeLeg": 0.7, "rightForeLeg": 0.7}}, flySprint),
    when(drv("centerRotation", "X", "flightPitch", space="OVERRIDE"), flySprint), when(drv("centerRotation", "Z", "headYaw"), flySprint),
    when(bodyRotXDrv("body", 1, "OVERRIDE"), flySprint),
    when(drv("head", "Y", "headYaw", space="OVERRIDE"), flySprint), when(drv("head", "X", "headPitch"), flySprint),
    when(bodyRotXDrv("head", -1, "PRE"), flySprint), when(drv("head", "X", "flightPitch", scale=-1), flySprint),
    when(bodyRotXDrv("leftArm", -1, "OVERRIDE"), flySprint), when(drv("leftArm", "Z", const=-60), flySprint), when(drv("leftArm", "Z", "flightSpeedFactor", scale=55), flySprint), when(drv("leftArm", "Z", "headYawAbs", scale=-0.5), flySprint),
    when(bodyRotXDrv("rightArm", -1, "OVERRIDE"), flySprint), when(drv("rightArm", "Z", const=60), flySprint), when(drv("rightArm", "Z", "flightSpeedFactor", scale=-55), flySprint), when(drv("rightArm", "Z", "headYawAbs", scale=0.5), flySprint),
    # hovering
    when({"animationKey": PL("fly_hover_arms"), "time": {"variable": "ticks", "scale": 0.0825}, "damping": {"leftArm": 0.3, "rightArm": 0.3, "leftForeArm": 0.3, "rightForeArm": 0.3}}, flyHover),
    when({"animationKey": PL("fly_hover_legs"), "time": {"variable": "ticks", "scale": 0.125}, "damping": {"leftLeg": 0.3, "rightLeg": 0.3, "leftForeLeg": 0.4, "rightForeLeg": 0.4}}, flyHover),
    when({"animationKey": PL("fly_hover_rest"), "damping": {"head": 1.0}}, flyHover),
    when(drv("head", "X", "headPitch", space="OVERRIDE"), flyHover), when(drv("head", "Y", "headYaw"), flyHover),
    # moving
    when({"animationKey": PL("fly_moving"), "damping": {"head": 1.0}}, flyMoving),
    when(drv("centerRotation", "X", "forwardMomentum", scale=50), flyMoving),
    when(drv("leftArm", "X", "forwardMomentum", scale=90, space="OVERRIDE"), flyMoving), when(drv("leftArm", "Z", "sidewaysMomentum", scale=-80, offset=-20, space="POST"), flyMoving),
    when(drv("rightArm", "X", "forwardMomentum", scale=90, space="OVERRIDE"), flyMoving), when(drv("rightArm", "Z", "sidewaysMomentum", scale=-80, offset=20, space="POST"), flyMoving),
    when(drv("leftLeg", "Z", "sidewaysMomentum", scale=-40, offset=-5, space="POST"), flyMoving),
    when(drv("rightLeg", "Z", "sidewaysMomentum", scale=-40, offset=5, space="POST"), flyMoving),
    when(drv("head", "X", "headPitch", space="OVERRIDE"), flyMoving), when(drv("head", "X", "forwardMomentum", scale=-50), flyMoving),
    when(drv("centerRotation", "Y", "headYaw", scale=-1, space="POST"), AND(flyMoving, NOT(state("DRAWING_BOW")))),
]}
# clamps for forward/sideways momentum: the bit clamps them to [-1, 1]
for item in p_flying["pose"]:
    a = item.get("angle")
    if isinstance(a, dict) and a.get("variable") in ("forwardMomentum", "sidewaysMomentum"):
        a["min"], a["max"], a["clampFirst"] = -1, 1, True

fallDamp = {"variable": "ticksFalling", "scale": 0.9 / 80, "offset": -0.9 * 10 / 80, "min": 0, "max": 0.9}
fallBones = ["leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "renderRotation"]
p_falling = {"type": "core:pose", "tags": ["falling"], "pose": [
    {"animationKey": PL("falling_rest"), "damping": {"centerRotation": 0.3, "body": 0.5}},
    drv("head", "X", "headPitch", space="OVERRIDE"), drv("head", "Y", "headYaw"),
    {"animationKey": PL("falling"), "time": {"variable": "ticks", "scale": 0.5}, "damping": {b: fallDamp for b in fallBones}},
    {"animationKey": PL("falling_head"), "space": "PRE", "damping": {"head": fallDamp}},
]}

def sprint_jump_node(leg):
    m = 1 if leg == "right" else -1
    main_fl, off_fl = ("rightForeLeg", "leftForeLeg") if leg == "right" else ("leftForeLeg", "rightForeLeg")
    return {"type": "core:pose", "tags": ["sprint_jump"], "pose": [
        {"driver": "core:ramp", "name": "relax", "speed": 0.1, "downSpeed": 0},
        {"animationKey": PL(f"sprint_jump_{leg}"), "damping": {"centerRotation": 0.3, "root": 0.5, "body": 0.3, "rightLeg": 0.8, "leftLeg": 0.8, "rightArm": 0.3, "leftArm": 0.3}, "vectorModes": {"root": "SLIDE"}},
        # body lean from the vertical motion, applied *inside* the Y twist (orientX then rotateY)
        drv("body", "X", "motionY", scale=-100, offset=20, min=-0.2, max=0.2, clampFirst=True, space="POST"),
        drv(main_fl, "X", "relax", scale=-80, offset=80, ease="pow", power=0.25, min=0, max=1, space="OVERRIDE"),
        drv(off_fl, "X", "relax", scale=70, ease="pow", power=0.25, min=0, max=1, space="OVERRIDE"),
        drv("head", "X", "headPitch", offset=-20, space="OVERRIDE"), drv("head", "Y", "headYaw", offset=-20 * m),
    ]}
# the sprint-jump body: orientX(lean).rotateY(20m) = Ry(20m) * Rx(lean): the clip holds Ry, the driver adds Rx in POST space

p_ladder = {"type": "core:pose", "tags": ["ladder_climb"], "pose": [
    {"animationKey": PL("ladder"), "time": {"variable": "climbingCycle"},
     "damping": {"body": 0.5, "leftArm": 0.5, "rightArm": 0.5, "leftForeArm": 0.5, "rightForeArm": 0.5, "leftLeg": 0.5, "rightLeg": 0.5, "leftForeLeg": 0.5, "rightForeLeg": 0.5, "localOffset": [None, None, 0.6]},
     "vectorModes": {"localOffset": "SLIDE"}},
    {"animationKey": PL("ladder_rest"), "damping": {"centerRotation": 0.3, "renderRotation": 0.6}},
    drv("renderRotation", "Y", "climbingRenderYaw", space="OVERRIDE"),
    drv("head", "X", "headPitch", space="OVERRIDE"), drv("head", "Y", "climbingHeadYaw"),
    when(drv("body", "X", "ledgeHeight", scale=50, offset=-30, space="OVERRIDE"), cmp("ledgeHeight", ">=", 0.6)),
    when(drv("leftArm", "X", "ledgeHeight", scale=40, offset=-124, space="OVERRIDE"), cmp("ledgeHeight", ">=", 0.6)),
    when(drv("rightArm", "X", "ledgeHeight", scale=40, offset=-124, space="OVERRIDE"), cmp("ledgeHeight", ">=", 0.6)),
    when({"animationKey": PL("ladder_ledge_forearms"), "damping": {"leftForeArm": 0.5, "rightForeArm": 0.5}}, cmp("ledgeHeight", ">=", 0.6)),
]}
for item in p_ladder["pose"]:
    if item.get("driver") == "core:axis_rotate" and item["bone"] in ("body", "leftArm", "rightArm"):
        item["damping"] = {item["bone"]: 0.5}

surface = OR(state("STANDING_STILL"), state("DRAWING_BOW"), cmp("ticksAfterAttack", "<", 10), NOT(state("UNDERWATER")))
deep = NOT(surface)
deepT = {"variable": "deep", "ease": "ease_in_out", "power": 3, "min": 0, "max": 1}
p_swimming = {"type": "core:pose", "tags": ["swimming"], "pose": [
    {"driver": "core:ramp", "name": "deep", "speed": 0.1, "when": deep, "readBeforeAdvance": True},
    {"animationKey": PL("swim_common"), "damping": {"head": 1.0, "renderRotation": 0.7, "localOffset": 0.3}, "vectorModes": {"localOffset": "SLIDE"}},
    when({"animationKey": PL("swim_surface_arms"), "time": {"variable": "ticks", "scale": 0.0825}, "damping": {"leftArm": 0.3, "rightArm": 0.3, "leftForeArm": 0.3, "rightForeArm": 0.3}}, surface),
    when({"animationKey": PL("swim_surface_legs"), "time": {"variable": "ticks", "scale": 0.2625}, "damping": {"leftLeg": 0.3, "rightLeg": 0.3, "leftForeLeg": 0.4, "rightForeLeg": 0.4}}, surface),
    when({"animationKey": PL("swim_deep_arm_inner"), "time": {"variable": "ticks", "scale": 0.1625}, "damping": {"leftArm": 0.3, "rightArm": 0.3}}, deep),
    when(drv("leftArm", "Y", "deep", scale=-90, ease="ease_in_out", power=3, min=0, max=1), deep),
    when(drv("rightArm", "Y", "deep", scale=90, ease="ease_in_out", power=3, min=0, max=1), deep),
    when({"animationKey": PL("swim_deep_arm_outer"), "time": {"variable": "ticks", "scale": 0.1625}, "space": "PRE"}, deep),
    when({"animationKey": PL("swim_deep_arms"), "time": {"variable": "ticks", "scale": 0.1625}, "damping": {"leftForeArm": 0.3, "rightForeArm": 0.3, "body": 0.5, "renderRightItemRotation": 0.3}}, deep),
    when({"animationKey": PL("swim_deep_legs"), "time": {"variable": "ticks", "scale": 0.4625}, "damping": {"leftLeg": 0.3, "rightLeg": 0.3, "leftForeLeg": 0.4, "rightForeLeg": 0.4}}, deep),
    drv("head", "X", "headPitch", space="OVERRIDE"), drv("head", "Y", "headYaw"), drv("head", "X", "deep", scale=-80, ease="ease_in_out", power=3, min=0, max=1),
    drv("renderRotation", "X", "deep", scale=80, ease="ease_in_out", power=3, min=0, max=1, space="OVERRIDE"),
    {"driver": "core:vector", "bone": "root", "y": dict(deepT, scale=14), "z": dict(deepT, scale=-20), "damping": {"root": [None, 0.7, 0.7]}, "vectorModes": {"root": "SLIDE"}},
]}

nodes = {
    "stand": p_stand, "walk": p_walk, "sprint": p_sprint, "jump": p_jump, "sprint_jump_right": sprint_jump_node("right"),
    "sprint_jump_left": sprint_jump_node("left"), "falling": p_falling, "flying": p_flying, "swimming": p_swimming,
    "ladder": p_ladder, "elytra": p_elytra, "riding": p_riding, "sitting": p_sitting, "sleeping": p_sleeping,
}
for name, node in nodes.items():
    key = "sprint_jump" if name.startswith("sprint_jump") else name
    conns = player_connections(key)
    # sprint jump picks the leg variant; the biped jump keeps its restart; sprint jump restarts likewise
    conns = [c for c in conns if c["target"] != "sprint_jump"]
    sj = [c["triggerCondition"] for c in player_connections(None) if c["target"] == "sprint_jump"][0]
    targets = [c["target"] for c in conns]
    at = targets.index("jump") if "jump" in targets else (targets.index("falling") + 1 if "falling" in targets else targets.index("flying") + 1)
    if name != "sprint_jump_right":
        conns.insert(at, {"target": "sprint_jump_right", "triggerCondition": AND(sj, state("SPRINT_JUMP_LEG"))}); at += 1
    if name != "sprint_jump_left":
        conns.insert(at, {"target": "sprint_jump_left", "triggerCondition": AND(sj, NOT(state("SPRINT_JUMP_LEG")))})
    if name == "jump":
        conns += jumpRestart
    if name.startswith("sprint_jump"):
        conns.append({"target": name, "triggerCondition": AND(cmp("prevMotionY", "<", 0), cmp("motionY", ">", 0))})
    node["connections"] = conns

groundAction = OR(action("stand"), action("walk"), action("sprint"))
torchMain = {"type": "core:property", "property": "mainHandItem", "value": "minecraft:torch"}
torchOff = {"type": "core:property", "property": "offHandItem", "value": "minecraft:torch"}
player = {
    "formatVersion": 2,
    "layers": [
        # item rotations are reset every frame before the layers run (keeps their damping)
        {"type": "KEYFRAME", "entryNode": "reset", "nodes": {"reset": {"type": "core:pose", "pose": [{"animationKey": PL("reset_items")}]}}},
        {"type": "KEYFRAME", "entryNode": "stand", "nodes": nodes},
        # sneaking overlay on the ground states
        {"type": "KEYFRAME", "when": AND(state("SNEAKING"), groundAction), "entryNode": "sneak", "nodes": {"sneak": {
            "type": "core:pose", "tags": ["sneak"], "pose": [
                {"animationKey": PL("sneak_base"), "time": limbTime,
                 "damping": {"rightLeg": 1.0, "leftLeg": 1.0, "rightArm": 0.8, "leftArm": 0.8, "root": [None, 0.6, None], "localOffset": 0.3},
                 "vectorModes": {"root": "RETARGET", "localOffset": "SLIDE"}},
                {"animationKey": PL("sneak_forelegs"), "time": limbTime, "damping": {"leftForeLeg": 0.3, "rightForeLeg": 0.3}},
                {"animationKey": PL("sneak_swing"), "time": limbTime, "weight": {"variable": "limbSwingAmount"}, "space": "POST"},
                {"animationKey": PL("sneak_body"), "time": limbTime, "space": "POST"},
                {"animationKey": PL("sneak_head"), "time": limbTime, "space": "PRE"},
            ]}}},
        # torch holding while standing or walking (not sprinting)
        {"type": "KEYFRAME", "when": AND(OR(action("stand"), action("walk")), OR(torchMain, torchOff)), "entryNode": "torch", "nodes": {"torch": {
            "type": "core:pose", "tags": ["torch_holding"], "pose": [
                when(drv("rightArm", "X", "headPitch", scale=0.5, offset=-90, space="OVERRIDE"), torchMain), when(drv("rightArm", "Y", "headYaw", scale=0.7), torchMain),
                when({"animationKey": PL("torch_forearm_right")}, torchMain),
                when(drv("leftArm", "X", "headPitch", scale=0.5, offset=-90, space="OVERRIDE"), AND(NOT(torchMain), torchOff)), when(drv("leftArm", "Y", "headYaw", scale=0.7), AND(NOT(torchMain), torchOff)),
                when({"animationKey": PL("torch_forearm_left")}, AND(NOT(torchMain), torchOff)),
            ]}}},
        # the cape is physics, kept as a driver
        {"type": "KEYFRAME", "entryNode": "cape", "nodes": {"cape": {"type": "core:pose", "pose": [{"driver": "mobends:cape", "bone": "cape"}]}}},
    ]}

for name, data in [("biped", biped), ("zombie", zombie), ("skeleton", skeleton), ("pig_zombie", pig_zombie), ("player", player)]:
    with open(os.path.join(ANIM, name + ".json"), 'w') as f:
        json.dump(data, f, indent=2)
    print("wrote", name + ".json")
