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

for name, data in [("biped", biped), ("zombie", zombie), ("skeleton", skeleton), ("pig_zombie", pig_zombie)]:
    with open(os.path.join(ANIM, name + ".json"), 'w') as f:
        json.dump(data, f, indent=2)
    print("wrote", name + ".json")
