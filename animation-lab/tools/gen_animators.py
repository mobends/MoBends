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
        {"type": "KEYFRAME", "when": cmp("entitySwingProgress", ">", 0), "entryNode": "slash",
         "mirror": {"when": state("LEFT_HANDED"), "negate": ["headYaw"],
                    "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                              ["renderLeftItemRotation", "renderRightItemRotation"]]},
         "nodes": {"slash": {
            "type": "core:pose", "tags": ["attack", "attack_slash_inward"],
            "pose": [
                {"animationKey": P("slash"), "time": {"variable": "ticksAfterAttack"}, "mirror": True,
                 "damping": {"body": 0.9, "head": 0.9, "rightArm": 0.9, "leftArm": 0.3, "rightForeArm": 0.3, "leftForeArm": 0.3, "localOffset": 0.3},
                 "vectorModes": {"localOffset": "SLIDE"}},
                *[dict(x, mirror=True) for x in headLook],
                # the still-standing legs are the same for both hands; the render rotation is not
                {"animationKey": P("slash_still"), "bones": ["leftLeg", "rightLeg", "rightForeLeg", "root"], "when": AND(state("STANDING_STILL"), NOT(state("RIDING"))),
                 "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "RETARGET"}},
                {"animationKey": P("slash_still"), "bones": ["renderRotation"], "mirror": True, "when": AND(state("STANDING_STILL"), NOT(state("RIDING"))), "damping": {"renderRotation": 0.3}},
                {"driver": "core:axis_rotate", "bone": "renderRightItemRotation", "axis": "X", "angle": 50, "space": "OVERRIDE", "snap": True, "mirror": True, "damping": {"renderRightItemRotation": 0.9}},
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

# ---- player: the action layer (BipedActionController and its item actions) ---------------------------
# Right-handed only (the bits read the primary hand); use actions get a clip per active hand.
def mc_sin(v):
    i = int(v * 10430.378) & 65535
    return math.sin(i * math.pi * 2 / 65536)

def curve_clip(path, bone_fn, samples, duration, vectors_fn=None):
    """A non-looping clip with explicit keyframe times: bone_fn(t) -> {bone: quaternion}."""
    data = {"bones": {}, "duration": duration, "loop": False, "times": [round(t, 7) for t in samples]}
    frames = [bone_fn(t) for t in samples]
    for bone in frames[0]:
        data["bones"][bone] = {"keyframes": [{"position": [0, 0, 0], "rotation": f[bone], "scale": [1, 1, 1]} for f in frames]}
    os.makedirs(os.path.dirname(path), exist_ok=True)
    json.dump(data, open(path, 'w'))

def prop(name, value=None, unset=False):
    c = {"type": "core:property", "property": name}
    if unset: c["unset"] = True
    else: c["value"] = value
    return c

def conn(target, cond, sets=None):
    c = {"target": target, "triggerCondition": cond}
    if sets: c["set"] = sets
    return c

# An action bit's slideY() over a base layer that re-slides the same vector restarts every
# frame; the core detects the conflicting write and restarts the slide, so SLIDE is exact.
tAA = "ticksAfterAttack"
dec = {"type": "core:decreased", "variable": tAA}
stillNotRiding = AND(state("STANDING_STILL"), NOT(state("RIDING")))
stanceWindow = AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60), state("ON_GROUND"))
stanceSprintCond = AND(stanceWindow, state("SPRINTING"))
stanceStillCond = AND(stanceWindow, NOT(state("SPRINTING")), state("STANDING_STILL"))
comboReset = {"driver": "core:set", "variable": "combo", "value": 0, "when": cmp(tAA, ">", 20)}
localOffsetZero = {"animationKey": PL("localoffset_zero"), "damping": {"localOffset": 0.3}, "vectorModes": {"localOffset": "SLIDE"}}
pose_clip(os.path.join(CLIPS, 'player', 'localoffset_zero.json'), {}, {"localOffset": [0, 0, 0]})

def mirrored(item):
    item = dict(item); item["mirror"] = True; return item
def swapped(item):
    item = dict(item); item["swapSides"] = True; return item

# --- sword slashes: baked clips, the look direction wrapped around the baked head part -------------------
# Everything the bit computes with its hand multiplier is marked "mirror"; the still-standing legs
# are not (the bit poses them the same way for both hands).
def slash_node(name, clipname, by_attack, main_damp, main_snap, head_damp, still_cond, item_damp, item_snap, still_extra=None):
    time = {"variable": tAA} if by_attack else None
    def item(bones, **kw):
        it = {"animationKey": PL(clipname), "bones": bones}
        if time: it["time"] = time
        it.update(kw)
        return it
    pose = [
        mirrored(item(["body", "leftArm", "leftForeArm", "rightForeArm", "localOffset"],
             damping={"body": 0.9, "leftArm": 0.3, "leftForeArm": 0.3, "rightForeArm": 0.3, "localOffset": 0.3}, vectorModes={"localOffset": "SLIDE"})),
        mirrored(item(["rightArm"], damping={"rightArm": main_damp}, snap=main_snap)),
        mirrored(item(["renderRightItemRotation"], damping={"renderRightItemRotation": item_damp} if item_damp else {}, snap=item_snap)),
        mirrored(with_damping(drv("head", "X", "headPitch", space="OVERRIDE"), head=head_damp) if head_damp else drv("head", "X", "headPitch", space="OVERRIDE")),
        mirrored(item(["head"], space="PRE")),
        mirrored(drv("head", "Y", "headYaw")),
        when({"animationKey": PL(clipname + "_still"), "bones": ["leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "root"],
              "damping": {"leftLeg": 0.3, "rightLeg": 0.3, "leftForeLeg": 0.3, "rightForeLeg": 0.3, "root": [None, 0.6, None]},
              "vectorModes": {"root": "SLIDE"}}, still_cond),
        when(mirrored({"animationKey": PL(clipname + "_still"), "bones": ["renderRotation"], "damping": {"renderRotation": 0.3}}), still_cond),
    ]
    if still_extra: pose += [when(mirrored(x), still_cond) for x in still_extra]
    return {"type": "core:pose", "tags": [name], "pose": pose}

# up / inward: legs, fore leg and render rotation are not damped by the bit (keep their rate)
def slash_node_keep_legs(node):
    node["pose"][-2]["damping"] = {"root": [None, 0.6, None]}
    return node

swordTrail = when({"driver": "mobends:sword_trail", "resetOnEnter": True}, AND(cmp(tAA, "<", 4), prop("attackActionType", "SWORD")))
slashes = {
    "slash_up": slash_node_keep_legs(slash_node("attack_slash_up", "slash_up", True, 0.9, False, 0.9, stillNotRiding, 0.9, True)),
    "slash_inward": slash_node_keep_legs(slash_node("attack_slash_inward", "slash_inward", True, 0.9, False, 0.9, stillNotRiding, 0.9, True)),
    "slash_down": slash_node("attack_slash_down", "slash_down", False, 0.3, True, 0.9, stillNotRiding, None, True, [drv("head", "Y", const=-30)]),
    "slash_outward": slash_node("attack_slash_outward", "slash_outward", False, 0.3, True, 0.9, stillNotRiding, None, True, [drv("head", "Y", const=-30)]),
}
# the whirl: head undamped, render rotation snapped, the global offset always dips
whirl = slash_node("attack_whirl_slash", "slash_whirl", True, 0.3, True, None, state("STANDING_STILL"), 0.9, False)
whirl["pose"][-1] = when({"animationKey": PL("slash_whirl_still"), "damping": {"leftLeg": 0.3, "rightLeg": 0.3, "leftForeLeg": 0.3, "rightForeLeg": 0.3}}, state("STANDING_STILL"))
whirl["pose"].insert(3, {"animationKey": PL("slash_whirl"), "bones": ["root"], "time": {"variable": tAA}, "damping": {"root": [None, 0.6, None]}, "vectorModes": {"root": "SLIDE"}})
whirl["pose"].insert(4, {"animationKey": PL("slash_whirl"), "bones": ["renderRotation"], "time": {"variable": tAA}, "snap": True})
slashes["slash_whirl"] = whirl
for n, node in slashes.items():
    node["pose"].insert(0, swordTrail)
# the whirl feeds the trail for its whole duration and clears it while its first half tick lasts
whirl["pose"][0] = {"driver": "mobends:sword_trail"}
whirl["pose"].insert(0, when({"driver": "mobends:sword_trail", "add": False, "resetEachFrame": True}, cmp(tAA, "<", 0.5)))

# --- attack stance: two breathing clocks (sin(t/5), cos(t/5.7)) split into two clips ----------------------
# The bit's arm angles are 60h + 5 sin(t/5) etc.: the constant carries the hand multiplier, the
# breathing does not, so the constants mirror while the sways only change arm.
cycle_clip(os.path.join(CLIPS, 'player', 'stance_breath0.json'), lambda p: {
    "body": rotations(('X', 20 + math.sin(p) * 2)),
    "rightArm": rotations(('Z', math.sin(p) * 5)),
    "head": rotations(('Y', -30), ('X', -(20 + math.sin(p) * 2)))})
cycle_clip(os.path.join(CLIPS, 'player', 'stance_breath1.json'), lambda p: {
    "leftArm": rotations(('Z', math.cos(p) * 5)),
    "rightArm": rotations(('Y', math.cos(p) * 5))})
pose_clip(os.path.join(CLIPS, 'player', 'stance_arms.json'), {"rightArm": rotations(('Z', 60)), "leftArm": rotations(('Z', -60))})
pose_clip(os.path.join(CLIPS, 'player', 'stance_const.json'), {
    "rightLeg": rotations(('X', -30), ('Z', 10), ('Y', 25)), "leftLeg": rotations(('X', -30), ('Z', -10), ('Y', -25)),
    "rightForeLeg": rotations(('X', 30)), "leftForeLeg": rotations(('X', 30)),
    "rightForeArm": rotations(('X', -20)), "leftForeArm": rotations(('X', -60)),
    "renderRightItemRotation": rotations(('X', 65)), "renderRotation": rotations(('Y', -30))}, {"root": [0, -2, 0]})
b0time = {"variable": "ticks", "scale": 1 / 5.0}
b1time = {"variable": "ticks", "scale": 1 / 5.7}
stance = {"type": "core:pose", "tags": ["attack_stance"], "pose": [
    mirrored({"animationKey": PL("stance_arms"), "damping": {"rightArm": 0.3, "leftArm": 0.3}}),
    {"animationKey": PL("stance_breath0"), "time": b0time, "bones": ["body"], "damping": {"body": 0.3}},
    swapped({"animationKey": PL("stance_breath0"), "time": b0time, "bones": ["rightArm"], "space": "POST"}),
    mirrored({"animationKey": PL("stance_breath0"), "time": b0time, "bones": ["head"], "space": "PRE"}),
    swapped({"animationKey": PL("stance_breath1"), "time": b1time, "bones": ["leftArm"], "space": "POST"}),
    swapped({"animationKey": PL("stance_breath1"), "time": b1time, "bones": ["rightArm"], "space": "PRE"}),
    mirrored({"animationKey": PL("stance_const"), "damping": {"rightLeg": 0.3, "leftLeg": 0.3, "rightForeLeg": 0.3, "leftForeLeg": 0.3, "rightForeArm": 0.3, "leftForeArm": 0.3,
                                                     "renderRightItemRotation": 0.3, "renderRotation": 0.3, "root": [None, 0.6, None]}, "vectorModes": {"root": "SLIDE"}}),
    {"animationKey": PL("stance_kneel"), "time": {"variable": "ticksAfterTouchdown"}, "when": cmp("ticksAfterTouchdown", "<", 1 / 0.15), "damping": {"body": 1.0}, "vectorModes": {"root": "SNAP"}},
    comboReset,
]}
pose_clip(os.path.join(CLIPS, 'player', 'stance_sprint_abs.json'), {"rightArm": rotations(('Z', 60), ('Y', 60)), "renderRightItemRotation": rotations(('X', 45))})
pose_clip(os.path.join(CLIPS, 'player', 'stance_sprint_pre.json'), {"body": rotations(('Y', 20)), "head": rotations(('Y', -20)), "leftArm": rotations(('Z', -30))})
stance_sprint = {"type": "core:pose", "tags": ["attack_stance_sprint"], "pose": [
    when({"driver": "mobends:sword_trail", "velocity": [0, 0, -10]}, prop("attackActionType", "SWORD")),
    localOffsetZero,
    mirrored({"animationKey": PL("stance_sprint_abs"), "damping": {"renderRightItemRotation": 0.3}}),
    mirrored({"animationKey": PL("stance_sprint_pre"), "space": "PRE"}),
    comboReset,
]}
sword_idle = {"type": "core:pose", "pose": [comboReset]}

# --- fists: punches (alternating arm) and the guard -----------------------------------------------------
legsStill = {"rightLeg": rotations(('X', -30), ('Z', 10)), "leftLeg": rotations(('X', -30), ('Y', -25), ('Z', -10)),
             "rightForeLeg": rotations(('X', 30)), "leftForeLeg": rotations(('X', 30))}
pose_clip(os.path.join(CLIPS, 'player', 'punch_still.json'), legsStill, {"root": [0, -2, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'punch_right_abs.json'), {
    "rightArm": rotations(('Y', -90)), "leftArm": rotations(('Z', -20), ('X', -90)),
    "rightForeArm": rotations(), "leftForeArm": rotations(('X', -80)), "body": rotations(('Y', -20)), "renderRotation": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'punch_right_pre.json'), {"rightArm": rotations(('Y', 10)), "head": rotations(('Y', 20))})
pose_clip(os.path.join(CLIPS, 'player', 'punch_right_still.json'), {"body": rotations(('Y', -40)), "renderRotation": rotations(('Y', -20))})
pose_clip(os.path.join(CLIPS, 'player', 'punch_left_abs.json'), {
    "leftArm": rotations(('Y', 100)), "rightArm": rotations(('X', -90), ('Z', 20)),
    "leftForeArm": rotations(), "rightForeArm": rotations(('X', -80)), "body": rotations(('Y', 20)), "renderRotation": rotations()})
pose_clip(os.path.join(CLIPS, 'player', 'punch_left_pre.json'), {"leftArm": rotations(('Y', -16)), "head": rotations(('Y', -20))})
pose_clip(os.path.join(CLIPS, 'player', 'punch_left_still.json'), {"body": rotations(), "renderRotation": rotations(('Y', -20))})
def punch_node(side):
    arm, other = (("rightArm", "leftArm") if side == "right" else ("leftArm", "rightArm"))
    fore, otherFore = arm.replace("Arm", "ForeArm"), other.replace("Arm", "ForeArm")
    return {"type": "core:pose", "tags": ["punch"], "pose": [
        {"animationKey": PL(f"punch_{side}_abs"), "damping": {arm: 0.9, other: 0.3, fore: 0.9, otherFore: 0.3, "body": 0.6}},
        drv(arm, "X", "headPitch", offset=-90),
        {"animationKey": PL(f"punch_{side}_pre"), "space": "PRE"},
        when({"animationKey": PL("punch_still"), "damping": {"rightLeg": 0.3, "leftLeg": 0.3, "rightForeLeg": 0.3, "leftForeLeg": 0.3, "root": [None, 0.6, None]}, "vectorModes": {"root": "SLIDE"}}, state("STANDING_STILL")),
        when({"animationKey": PL(f"punch_{side}_still"), "damping": {"body": 0.6}}, state("STANDING_STILL")),
    ]}
pose_clip(os.path.join(CLIPS, 'player', 'fist_guard_abs.json'), dict(legsStill, **{
    "renderRotation": rotations(('Y', -20)),
    "rightArm": rotations(('X', -90), ('Z', 20)), "leftArm": rotations(('X', -90), ('Z', -20)),
    "rightForeArm": rotations(('X', -80)), "leftForeArm": rotations(('X', -80))}), {"root": [0, -2, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'fist_guard_pre.json'), {"body": rotations(('X', 10)), "head": rotations(('X', -10), ('Y', -20))})
fistGuardBones = ["rightArm", "leftArm", "rightForeArm", "leftForeArm", "rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg", "root"]
fist_guard = {"type": "core:pose", "tags": ["fist_guard"], "pose": [
    when({"animationKey": PL("fist_guard_abs"), "bones": fistGuardBones, "damping": {"rightArm": 0.3, "leftArm": 0.3, "rightForeArm": 0.3, "leftForeArm": 0.3,
                                                          "rightLeg": 0.3, "leftLeg": 0.3, "rightForeLeg": 0.3, "leftForeLeg": 0.3, "root": [None, 0.6, None]},
          "vectorModes": {"root": "SLIDE"}}, state("STANDING_STILL")),
    when(mirrored({"animationKey": PL("fist_guard_abs"), "bones": ["renderRotation"], "damping": {"renderRotation": 0.3}}), state("STANDING_STILL")),
    when({"animationKey": PL("fist_guard_pre"), "bones": ["body"], "space": "PRE"}, state("STANDING_STILL")),
    when(mirrored({"animationKey": PL("fist_guard_pre"), "bones": ["head"], "space": "PRE"}), state("STANDING_STILL")),
]}
fists_idle = {"type": "core:pose", "pose": []}

# --- tool swing: curves over the swing progress (sin(sqrt(p) * 2pi) is steep near 0: dense samples there) -
swingSamples = [(k / 128) ** 2 for k in range(129)]
swingPhase = lambda p: math.sqrt(p) * 6.2831855
curve_clip(os.path.join(CLIPS, 'player', 'tool_body.json'), lambda p: {"body": rotations(('Y', mc_sin(swingPhase(p)) * 30))}, swingSamples, 1)
curve_clip(os.path.join(CLIPS, 'player', 'tool_head.json'), lambda p: {"head": rotations(('Y', -mc_sin(swingPhase(p)) * 30))}, swingSamples, 1)
curve_clip(os.path.join(CLIPS, 'player', 'tool_arm.json'), lambda p: {"rightArm": rotations(('X', mc_sin(swingPhase(p)) * 50 - 30))}, swingSamples, 1)
curve_clip(os.path.join(CLIPS, 'player', 'tool_arm_post.json'), lambda p: {"rightArm": rotations(('Z', mc_cos(swingPhase(p)) * -20 + 10))}, swingSamples, 1)
pose_clip(os.path.join(CLIPS, 'player', 'tool_rest.json'), {"centerRotation": rotations()}, {"localOffset": [0, 0, 0]})
pose_clip(os.path.join(CLIPS, 'player', 'tool_sneak_body.json'), {"body": rotations(('X', 20))})
swingTime = {"variable": "swingProgress"}
def also_when(item, cond):
    return when(item, AND(item["when"], cond) if "when" in item else cond)
tool = {"type": "core:pose", "tags": ["tool"], "pose": [(swapped if "tool_arm" in json.dumps(x) else mirrored)(also_when(x, state("SWINGING"))) for x in [
    {"animationKey": PL("tool_rest"), "damping": {"centerRotation": 0.3, "localOffset": 0.3}, "vectorModes": {"localOffset": "SLIDE"}},
    {"animationKey": PL("tool_body"), "time": swingTime, "damping": {"body": 0.8}},
    when({"animationKey": PL("tool_sneak_body"), "space": "PRE"}, state("SNEAKING")),
    when(with_damping(drv("head", "X", "headPitch", space="OVERRIDE"), head=0.8), NOT(state("SNEAKING"))),
    when(with_damping(drv("head", "X", "headPitch", offset=-20, space="OVERRIDE"), head=0.8), state("SNEAKING")),
    drv("head", "Y", "headYaw"),
    {"animationKey": PL("tool_head"), "time": swingTime, "space": "PRE"},
    {"animationKey": PL("tool_arm"), "time": swingTime, "snap": True},
    {"animationKey": PL("tool_arm_post"), "time": swingTime, "space": "POST"},
]]}

# --- item use: eating, bow, shield; one node per active hand ---------------------------------------------
def use_nodes():
    nodes = {}
    for side, h in (("right", 1), ("left", -1)):
        arm, other = (("rightArm", "leftArm") if side == "right" else ("leftArm", "rightArm"))
        fore, otherFore = arm.replace("Arm", "ForeArm"), other.replace("Arm", "ForeArm")
        eatSamples = [k / 64 for k in range(65)]
        curve_clip(os.path.join(CLIPS, 'player', f'eat_arm_{side}.json'), lambda b, arm=arm, h=h: {arm: rotations(('X', b * -80), ('Z', 45 * b * h))}, eatSamples, 1)
        cycle_clip(os.path.join(CLIPS, 'player', f'eat_head_{side}.json'), lambda p, h=h: {"head": rotations(('X', mc_cos(p) * 5), ('Y', 15 * h))})
        nodes[f"eat_{side}"] = {"type": "core:pose", "tags": ["eating"], "pose": [
            {"driver": "core:ramp", "name": "bringUp", "speed": 0.15, "downSpeed": 0},
            {"driver": "core:ramp", "name": "bringUpPrev", "speed": 0.15, "downSpeed": 0, "readBeforeAdvance": True},
            {"animationKey": PL(f"eat_arm_{side}"), "time": {"variable": "bringUp"}},
            drv(fore, "X", "bringUp", scale=-45, space="OVERRIDE"),
            when({"animationKey": PL(f"eat_head_{side}"), "time": {"variable": "ticks"}}, cmp("bringUpPrev", ">=", 1)),
        ]}
        # bow: the off arm's Z part is a curve over the head pitch
        pitchSamples = [-90 + 180 * k / 64 for k in range(65)]
        # keyframe times run 0..180 for a pitch of -90..90
        curve_clip(os.path.join(CLIPS, 'player', f'bow_offarm_{side}.json'),
                   lambda t, other=other, h=h: {other: rotations(('Z', (-mc_cos((t - 90) / 180 * 3.1415927) * 40 + 40) * h))}, [p + 90 for p in pitchSamples], 180)
        nodes[f"bow_{side}"] = {"type": "core:pose", "tags": ["bow"], "pose": [
            localOffsetZero,
            with_damping(drv("head", "X", "headPitch", space="OVERRIDE"), head=0.5),
            # on a ladder the body faces the wall and the head only pitches
            when(drv("head", "Y", "aimedBowTicks", scale=-5 * h, offset=50 * h), NOT(state("CLIMBING"))),
            when(with_damping(drv("body", "Y", "aimedBowTicks", scale=5 * h, offset=-50 * h, space="OVERRIDE"), body=0.8), NOT(state("CLIMBING"))),
            when(drv("body", "Y", "headYaw"), NOT(state("CLIMBING"))),
            when(with_damping(drv("body", "Y", "climbingBodyYaw", space="OVERRIDE"), body=0.8), state("CLIMBING")),
            with_damping(drv(arm, "X", "headPitch", offset=-90, space="OVERRIDE"), **{arm: 0.8}),
            drv(arm, "Y", "aimedBowTicks", scale=-5 * h, offset=50 * h),
            with_damping(drv(other, "Y", const=80 * h, space="OVERRIDE"), **{other: 1.0}),
            {"animationKey": PL(f"bow_offarm_{side}"), "time": {"variable": "headPitch", "offset": 90}, "space": "PRE"},
            drv(other, "X", "headPitch", offset=-90, min=-160),
            with_damping(drv(fore, "X", const=0, space="OVERRIDE"), **{fore: 1.0}),
            drv(otherFore, "X", "aimedBowTicks", scale=-3, space="OVERRIDE"),
        ]}
        nodes[f"shield_{side}"] = {"type": "core:pose", "tags": ["shield"], "pose": [
            {"driver": "core:ramp", "name": "bringUp", "speed": 0.7, "downSpeed": 0},
            drv(arm, "Y", "bringUp", scale=-45 * h, space="OVERRIDE"),
            drv(fore, "X", "bringUp", scale=-45, space="OVERRIDE"),
        ]}
    return nodes

# --- the node graph ---------------------------------------------------------------------------------------
useTypes = [("eat", "FOOD"), ("bow", "BOW"), ("shield", "SHIELD")]
def use_conns(exclude_type=None):
    out = []
    for base, typ in useTypes:
        if typ == exclude_type: continue
        for side in ("right", "left"):
            out.append(conn(f"{base}_{side}", AND(prop("useActionType", typ), prop("activeHandSide", side.upper()))))
    return out
noUse = prop("useActionType", unset=True)
familyEntry = {
    # a fresh SwordAction: no move plays until the next attack; the stance if in its window
    "sword": [("stance_sprint", stanceSprintCond, {"combo": 0}), ("stance", stanceStillCond, {"combo": 0}), ("sword_idle", None, {"combo": 0})],
    # a fresh PunchingAction starts with the left fist
    "fists": [("punch_left", cmp(tAA, "<", 10), {"fist": 0}), ("fist_guard", AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60)), {"fist": 0}), ("fists_idle", None, {"fist": 0})],
    "tool": [("tool", None, None)],
}
familyType = {"sword": "SWORD", "fists": "FISTS", "tool": "TOOL"}
def attack_conns(exclude_family=None):
    out = []
    for family, entries in familyEntry.items():
        if family == exclude_family: continue
        for target, cond, sets in entries:
            parts = [noUse, prop("attackActionType", familyType[family])] + ([cond] if cond else [])
            out.append(conn(target, AND(*parts), sets))
    return out
slashOrder = ["slash_up", "slash_down", "slash_inward", "slash_outward", "slash_whirl"]
slashByCombo = [conn(n, AND(dec, cmp("combo", "==", k)), {"combo": (k + 1) % 5}) for k, n in enumerate(slashOrder)]
afterSlash = [conn("stance_sprint", stanceSprintCond), conn("stance", stanceStillCond),
              conn("sword_idle", AND(cmp(tAA, ">=", 10), NOT(stanceSprintCond), NOT(stanceStillCond)))]
punchConns = [conn("punch_right", AND(dec, cmp("fist", "==", 0)), {"fist": 1}), conn("punch_left", AND(dec, cmp("fist", "==", 1)), {"fist": 0})]
guardWindow = AND(cmp(tAA, ">=", 10), cmp(tAA, "<", 60))

action_nodes = {"idle": {"type": "core:pose", "pose": []}, "sword_idle": sword_idle, "stance": stance, "stance_sprint": stance_sprint,
                "fists_idle": fists_idle, "punch_right": punch_node("right"), "punch_left": punch_node("left"), "fist_guard": fist_guard, "tool": tool}
action_nodes.update(slashes)
action_nodes.update(use_nodes())
for name, node in action_nodes.items():
    if name == "idle":
        c = use_conns() + attack_conns()
    elif name == "sword_idle":
        c = use_conns() + attack_conns("sword") + slashByCombo + [conn("stance_sprint", stanceSprintCond), conn("stance", stanceStillCond)]
    elif name.startswith("slash_"):
        c = use_conns() + attack_conns("sword") + slashByCombo + afterSlash
    elif name == "stance":
        c = use_conns() + attack_conns("sword") + slashByCombo + [conn("stance_sprint", stanceSprintCond), conn("sword_idle", NOT(OR(stanceSprintCond, stanceStillCond)))]
    elif name == "stance_sprint":
        c = use_conns() + attack_conns("sword") + slashByCombo + [conn("stance", stanceStillCond), conn("sword_idle", NOT(OR(stanceSprintCond, stanceStillCond)))]
    elif name == "fists_idle":
        c = use_conns() + attack_conns("fists") + punchConns + [conn("fist_guard", guardWindow)]
    elif name.startswith("punch_"):
        c = use_conns() + attack_conns("fists") + punchConns + [conn("fist_guard", guardWindow), conn("fists_idle", cmp(tAA, ">=", 60))]
    elif name == "fist_guard":
        c = use_conns() + attack_conns("fists") + punchConns + [conn("fists_idle", cmp(tAA, ">=", 60))]
    elif name == "tool":
        c = use_conns() + attack_conns("tool")
    else:  # use nodes
        typ = [t for b, t in useTypes if name.startswith(b)][0]
        c = use_conns(typ) + attack_conns()
    node["connections"] = copy.deepcopy(c)

action_layer = {"type": "KEYFRAME", "when": NOT(state("SLEEPING")), "entryNode": "idle", "variables": {"combo": 0, "fist": 0}, "nodes": action_nodes,
                # a left-handed player plays the hand-dependent items as their mirror image
                "mirror": {"when": state("LEFT_HANDED"), "negate": ["headYaw"],
                           "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"], ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
                                     ["renderLeftItemRotation", "renderRightItemRotation"]]}}

groundAction = OR(action("stand"), action("walk"), action("sprint"))
def torchArm(side):
    arm = side + "Arm"
    return [drv(arm, "X", "headPitch", scale=0.5, offset=-90, space="OVERRIDE"), drv(arm, "Y", "headYaw", scale=0.7),
            {"animationKey": PL(f"torch_forearm_{side}")}]
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
                # the main hand holds the torch if it has one, else the off hand; the arm follows the primary hand
                *[when(x, AND(torchMain, NOT(state("LEFT_HANDED")))) for x in torchArm("right")],
                *[when(x, AND(torchMain, state("LEFT_HANDED"))) for x in torchArm("left")],
                *[when(x, AND(NOT(torchMain), torchOff, NOT(state("LEFT_HANDED")))) for x in torchArm("left")],
                *[when(x, AND(NOT(torchMain), torchOff, state("LEFT_HANDED"))) for x in torchArm("right")],
            ]}}},
        # items and attacks (the BipedActionController)
        action_layer,
        # the cape is physics, kept as a driver
        {"type": "KEYFRAME", "entryNode": "cape", "nodes": {"cape": {"type": "core:pose", "pose": [{"driver": "mobends:cape", "bone": "cape"}]}}},
    ]}


# ---- squid: the vanilla tentacle wave over the interpolated squid rotation --------------------------
SQ = lambda n: clip('squid', n)
def squid_base(t, active):
    out = {}
    sr = t + 1.1
    f = max(0.0, sr / math.pi)
    angle = mc_sin(f * f * math.pi) * 60 if active else 0
    for i in range(8):
        out[f"tentacle_{i}_0"] = rotations(('X', angle), ('Y', i * -360.0 / 8 + 90.0))
    return out
def squid_sections(t, active):
    out = {}
    for i in range(8):
        for j in range(1, 9):
            out[f"tentacle_{i}_{j}"] = rotations(('X', -(mc_sin((t + 1.1) + j * 0.1) * 10) if active else 0))
    return out
squidSamples = [2 * math.pi * k / 512 for k in range(513)]
curve_clip(os.path.join(CLIPS, 'squid', 'swim_base.json'), lambda t: squid_base(t, True), squidSamples, 2 * math.pi)
pose_clip(os.path.join(CLIPS, 'squid', 'swim_base_rest.json'), squid_base(0, False))
curve_clip(os.path.join(CLIPS, 'squid', 'swim_sections.json'), lambda t: squid_sections(t, True), squidSamples, 2 * math.pi)
pose_clip(os.path.join(CLIPS, 'squid', 'swim_sections_rest.json'), squid_sections(0, False))
squidBaseDamp = {f"tentacle_{i}_0": 0.1 for i in range(8)}
squidSectionDamp = {f"tentacle_{i}_{j}": 0.1 for i in range(8) for j in range(1, 9)}
squidTime = {"variable": "squidRotation"}
squid = {"formatVersion": 2, "layers": [{"type": "KEYFRAME", "entryNode": "swim", "nodes": {"swim": {"type": "core:pose", "tags": ["swim"], "pose": [
    when({"animationKey": SQ("swim_base"), "time": squidTime, "damping": squidBaseDamp}, state("SQUID_PREV_ROTATION_LOW")),
    when({"animationKey": SQ("swim_base_rest"), "damping": squidBaseDamp}, NOT(state("SQUID_PREV_ROTATION_LOW"))),
    when({"animationKey": SQ("swim_sections"), "time": squidTime, "damping": squidSectionDamp}, state("SQUID_ROTATION_LOW")),
    when({"animationKey": SQ("swim_sections_rest"), "damping": squidSectionDamp}, NOT(state("SQUID_ROTATION_LOW"))),
]}}}]}


# ---- spider: IK leg drivers for the ground gaits, data for the jump and the death ----------------------
SP = lambda n: clip('spider', n)
def with_snap(item):
    item = dict(item); item["snap"] = True; return item
spiderHead = [with_snap(drv("head", "X", "headPitch", space="OVERRIDE")), drv("head", "Y", "headYaw")]
pose_clip(os.path.join(CLIPS, 'spider', 'rest.json'), {"centerRotation": rotations(), "renderRotation": rotations()}, {"localOffset": [0, 0, 0]})
spiderRest = {"animationKey": SP("rest"), "damping": {"localOffset": 1.0}, "vectorModes": {"localOffset": "SLIDE"}}
spiderLimbs = [
    {"phase": 0.0, "minDist": 20, "maxDist": 10, "minRot": -80, "maxRot": -50}, {"phase": 0.3, "minDist": 20, "maxDist": 10, "minRot": -80, "maxRot": -50},
    {"phase": 0.3, "minDist": 15, "maxDist": 15, "minRot": -30, "maxRot": 10}, {"phase": 0.0, "minDist": 15, "maxDist": 15, "minRot": -30, "maxRot": 10},
    {"phase": 0.4, "minDist": 7, "maxDist": 15, "minRot": 20, "maxRot": 50}, {"phase": 0.7, "minDist": 7, "maxDist": 15, "minRot": 20, "maxRot": 50},
    {"phase": 0.7, "minDist": 10, "maxDist": 20, "minRot": 60, "maxRot": 80}, {"phase": 0.4, "minDist": 10, "maxDist": 20, "minRot": 60, "maxRot": 80},
]
bodyBob = lambda fn, sign: {"variable": "ticks", "scale": 0.2, "fn": fn, "mul": 0.4 * sign}
sp_idle = {"type": "core:pose", "tags": ["idle"], "pose": [
    {"driver": "mobends:spider_idle_legs", "groundLevel": {"variable": "ticks", "scale": 0.1, "fn": "sin", "mul": 0.5},
     "bodyX": bodyBob("sin", 1), "bodyZ": bodyBob("cos", 1), "kneelDuration": 10, "kneelAmplitude": 4, "kneelLead": 0},
    with_snap({"driver": "core:vector", "bone": "root", "x": bodyBob("sin", 1), "y": {"variable": "groundLevel", "scale": -1}, "z": bodyBob("cos", -1)}),
    *spiderHead, spiderRest,
]}
sp_move = {"type": "core:pose", "tags": ["move"], "pose": [
    {"driver": "mobends:spider_moving_legs", "swing": {"variable": "limbSwing", "scale": 0.6662},
     "groundLevel": {"variable": "ticks", "scale": 0.6, "fn": "mcsin", "mul": 1.2}, "kneelDuration": 10, "kneelAmplitude": 3, "kneelLead": 0.2, "limbs": spiderLimbs},
    with_snap({"driver": "core:vector", "bone": "root", "x": bodyBob("mcsin", 1), "y": {"variable": "groundLevel", "scale": -1}, "z": bodyBob("mccos", -1)}),
    *spiderHead, spiderRest,
]}
pose_clip(os.path.join(CLIPS, 'spider', 'crawl_rest.json'), {"centerRotation": rotations()}, {"localOffset": [0, -10, 0]})
sp_crawl = {"type": "core:pose", "tags": ["crawl"], "pose": [
    {"driver": "mobends:spider_moving_legs", "swing": {"variable": "crawlProgress", "scale": 5},
     "groundLevel": {"variable": "crawlProgress", "scale": 3.0, "fn": "mcsin", "mul": 1.2}, "limbs": spiderLimbs},
    *spiderHead,
    with_damping(drv("renderRotation", "X", const=-90, space="OVERRIDE"), renderRotation=0.6), drv("renderRotation", "Y", "crawlRenderYaw"),
    {"animationKey": SP("crawl_rest"), "damping": {"localOffset": 0.5}, "vectorModes": {"localOffset": "SLIDE"}},
]}
# jump: legs fan out to their natural yaw and bend with the vertical motion
def natural_yaw(i):
    ny = -((i / 7) * 2 - 1)
    return math.degrees(-ny * 1.3 if i % 2 == 1 else ny * 1.3)
pose_clip(os.path.join(CLIPS, 'spider', 'jump.json'), {f"leg{i + 1}": rotations(('Y', natural_yaw(i))) for i in range(8)}, {"root": [0, 0, 0]})
jumpMotion = lambda mul, add: {"variable": "interpolatedMotionY", "scale": -5, "min": -1, "max": 1, "mul": mul, "add": add}
sp_jump = {"type": "core:pose", "tags": ["jump"], "pose": [
    with_snap({"animationKey": SP("jump"), "bones": ["root"]}),
    {"animationKey": SP("jump"), "bones": [f"leg{i + 1}" for i in range(8)], "damping": {f"leg{i + 1}": 1.0 for i in range(8)}},
    *[drv(f"leg{i + 1}", "Z", None, space="POST", **{}) | {"angle": jumpMotion(25 * (-1 if i % 2 else 1), -20 * (-1 if i % 2 else 1))} for i in range(8)],
    *[with_damping(drv(f"foreLeg{i + 1}", "Z", None, space="OVERRIDE") | {"angle": jumpMotion(-40 * (-1 if i % 2 else 1), -70 * (-1 if i % 2 else 1))}, **{f"foreLeg{i + 1}": 1.0}) for i in range(8)],
    spiderRest,
]}
# death: legs splay (instant), sway with the last limb swing, and wiggle with a decaying speed
deathZ = [-45, 45, -33.3, 33.3, -33.3, 33.3, -45, 45]
deathY = [45, -45, 22.5, -22.5, -22.5, 22.5, -45, 45]
pose_clip(os.path.join(CLIPS, 'spider', 'death.json'),
          dict({f"leg{i + 1}": rotations(('Z', deathZ[i]), ('Y', deathY[i])) for i in range(8)},
               **{f"foreLeg{i + 1}": rotations(('Z', 89 if i % 2 else -89)) for i in range(8)}))
swingPhases = [0.0, 0.0, math.pi, math.pi, math.pi / 2, math.pi / 2, math.pi * 3 / 2, math.pi * 3 / 2]
cycle_clip(os.path.join(CLIPS, 'spider', 'death_sway_y.json'), lambda ls: {
    f"leg{i + 1}": rotations(('Y', -(mc_cos(ls * 2.0 + swingPhases[i]) * 0.4) * (-1 if i % 2 else 1))) for i in range(8)}, 128)
cycle_clip(os.path.join(CLIPS, 'spider', 'death_sway_z.json'), lambda ls: {
    f"leg{i + 1}": rotations(('Z', abs(mc_sin(ls + swingPhases[i]) * 0.4) * (-1 if i % 2 else 1))) for i in range(8)}, 128)
wigglePhases = [0, math.pi / 4, math.pi / 2, math.pi / 4 * 3]
cycle_clip(os.path.join(CLIPS, 'spider', 'death_wiggle.json'), lambda ph: {
    f"leg{i + 1}": rotations(('Z', mc_cos(ph + wigglePhases[i % 4]))) for i in range(8)}, 128)
amountDeg = {"variable": "limbSwingAmount", "scale": 180 / math.pi}
sp_death = {"type": "core:pose", "tags": ["death"], "pose": [
    {"driver": "core:accumulate", "name": "wiggleSpeed", "rate": -0.1, "initial": 1, "min": 0},
    {"driver": "core:accumulate", "name": "wigglePhase", "rate": {"variable": "wiggleSpeed", "scale": 2, "offset": 0.3}},
    {"driver": "core:vector", "bone": "root", "y": 10, "damping": {"root": [None, 0.3, None]}, "vectorModes": {"root": "SLIDE"}},
    *spiderHead,
    with_snap({"animationKey": SP("death")}),
    {"animationKey": SP("death_sway_y"), "time": {"variable": "limbSwing", "scale": 0.6662}, "weight": amountDeg, "space": "PRE"},
    {"animationKey": SP("death_sway_z"), "time": {"variable": "limbSwing", "scale": 0.6662}, "weight": amountDeg, "space": "PRE"},
    {"animationKey": SP("death_wiggle"), "time": {"variable": "wigglePhase"}, "weight": {"variable": "wiggleSpeed", "scale": 10, "offset": 10}, "space": "PRE"},
]}
# the controller's decision chain, guarded like the player's
spiderChain = [
    ("death", cmp("health", "<=", 0)),
    ("crawl", state("BESIDE_CLIMBABLE")),
    ("jump", jumping),
    ("idle", state("STANDING_STILL")),
    ("move", None),
]
sp_nodes = {"idle": sp_idle, "move": sp_move, "jump": sp_jump, "crawl": sp_crawl, "death": sp_death}
for name, node in sp_nodes.items():
    conns = []
    prior = []
    for target, cond in spiderChain:
        parts = [NOT(c) for c in prior] + ([cond] if cond is not None else [])
        if target != name:
            c = {"target": target, "triggerCondition": parts[0] if len(parts) == 1 else AND(*parts)}
            if name == "jump" and target in ("idle", "move"):
                c["set"] = {"resetLimbs": 1}  # feet re-planted under the body after a jump
            conns.append(c)
        if cond is not None:
            prior.append(cond)
    node["connections"] = conns
spider = {"formatVersion": 2, "layers": [{"type": "KEYFRAME", "entryNode": "idle", "variables": {"resetLimbs": 1}, "nodes": sp_nodes}]}

# the skeleton's controller runs the biped action controller as well: bow, sword, tool, fists
skeleton_actions = copy.deepcopy(action_layer)
del skeleton_actions["when"]  # only players sleep
skeleton["layers"].append(skeleton_actions)

# the zombie villager's controller is the zombie's
zombie_villager = {"formatVersion": 2, "extends": "mobends:bends/animators/zombie.json"}


# ---- mobs described by model definitions (bends/models/*.json): generic walkers ---------------------
# Their legs are split at the knee by the definition; the gait swings the upper segment like the
# vanilla model did and bends the lower one when the leg trails.
W = lambda folder, n: clip(folder, n)
def walker_gait(folder, legs, upperAmp=55, lowerAmp=35):
    """legs: list of (upper, lower, phase). A looping cycle over the limb swing at unit amplitude."""
    def frame(p):
        out = {}
        for upper, lower, phase in legs:
            swing = mc_cos(p + phase)
            out[upper] = rotations(('X', swing * upperAmp))
            if lower:
                out[lower] = rotations(('X', (1 - swing) * 0.5 * lowerAmp))
        return out
    cycle_clip(os.path.join(CLIPS, folder, 'walk.json'), frame)
    return {"animationKey": W(folder, "walk"), "time": limbTime, "weight": {"variable": "limbSwingAmount"},
            "damping": {b: 0.8 for upper, lower, _ in legs for b in ([upper] + ([lower] if lower else []))}}

def walker_idle(folder, bones):
    """A slow breathing sway of the listed bones on the tick clock."""
    cycle_clip(os.path.join(CLIPS, folder, 'idle.json'), lambda p: {b: rotations(('X', mc_cos(p) * amp)) for b, amp in bones})
    return {"animationKey": W(folder, "idle"), "time": {"variable": "ticks", "scale": 0.09}, "damping": {b: 0.5 for b, _ in bones}}

def walker_jump(folder, legs, upperAngle=-20, lowerAngle=35):
    pose_clip(os.path.join(CLIPS, folder, 'jump.json'), dict(
        {upper: rotations(('X', upperAngle)) for upper, lower, _ in legs},
        **{lower: rotations(('X', lowerAngle)) for upper, lower, _ in legs if lower}))
    return {"animationKey": W(folder, "jump"), "damping": {b: 0.3 for upper, lower, _ in legs for b in ([upper] + ([lower] if lower else []))}}

def walker_animator(folder, legs, idleBones, extra=None, headBone="head"):
    look = [with_damping(drv(headBone, "Y", "headYaw"), **{headBone: 0.5}), drv(headBone, "X", "headPitch", space="POST")]
    extra = extra or []
    stand = {"type": "core:pose", "tags": ["stand"], "pose": [walker_idle(folder, idleBones), *look, *extra],
             "connections": [{"target": "jump", "triggerCondition": jumping}, {"target": "walk", "triggerCondition": state("MOVING_HORIZONTALLY")}]}
    walk = {"type": "core:pose", "tags": ["walk"], "pose": [walker_gait(folder, legs), *look, *extra],
            "connections": [{"target": "jump", "triggerCondition": jumping}, {"target": "stand", "triggerCondition": state("STANDING_STILL")}]}
    jump = {"type": "core:pose", "tags": ["jump"], "pose": [walker_jump(folder, legs), *look, *extra],
            "connections": [{"target": "stand", "triggerCondition": AND(grounded, state("STANDING_STILL"))}, {"target": "walk", "triggerCondition": AND(grounded, state("MOVING_HORIZONTALLY"))}]}
    return {"formatVersion": 2, "layers": [{"type": "KEYFRAME", "entryNode": "stand", "nodes": {"stand": stand, "walk": walk, "jump": jump}}]}

# vanilla ModelQuadruped: legs 1 and 4 swing together, 2 and 3 opposite
quadLegs = [("leg1", "foreLeg1", 0.0), ("leg2", "foreLeg2", math.pi), ("leg3", "foreLeg3", math.pi), ("leg4", "foreLeg4", 0.0)]
quadruped = walker_animator("quadruped", quadLegs, [("head", 2.0), ("body", 1.0)])
creeper = walker_animator("creeper", quadLegs, [("head", 1.5)])
villager = walker_animator("villager", [("rightLeg", "foreRightLeg", 0.0), ("leftLeg", "foreLeftLeg", math.pi)], [("head", 1.5), ("arms", 2.0)])
chickenWings = [with_damping(drv("rightWing", "Z", "wingAngle", space="OVERRIDE"), rightWing=1.0),
                with_damping(drv("leftWing", "Z", "wingAngle", scale=-1, space="OVERRIDE"), leftWing=1.0)]
chicken = walker_animator("chicken", [("rightLeg", "foreRightLeg", 0.0), ("leftLeg", "foreLeftLeg", math.pi)], [("head", 2.0)], extra=chickenWings)

for name, data in [("biped", biped), ("zombie", zombie), ("skeleton", skeleton), ("pig_zombie", pig_zombie), ("player", player), ("squid", squid), ("spider", spider), ("zombie_villager", zombie_villager),
                   ("quadruped", quadruped), ("creeper", creeper), ("villager", villager), ("chicken", chicken)]:
    with open(os.path.join(ANIM, name + ".json"), 'w') as f:
        json.dump(data, f, indent=2)
    print("wrote", name + ".json")
