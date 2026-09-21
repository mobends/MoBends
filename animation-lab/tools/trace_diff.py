#!/usr/bin/env python3
"""Prints, frame by frame, the bones where a KUMO trace deviates from its golden.

usage: trace_diff.py <entity>/<scenario> [bone ...] [--from N] [--to N] [--key r|rt] [--thresh deg] [--vec v|vt]

  --key r     compare the smoothed rotations (default); rt: the targets the animation wrote
  --vec v     compare the smoothed offset vectors instead (vt: their targets)

Reads golden/<scenario>.json.gz and build/kumo-traces/<scenario>.json.gz (written by `gradle compare`).
Angles print as "<angle>@[axis]".
"""
import gzip, json, math, os, sys
root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
args = sys.argv[1:]
scen = args.pop(0)
opts = {'from': 0, 'to': 10**9, 'key': 'r', 'thresh': 0.1, 'vec': None}
bones = []
while args:
    a = args.pop(0)
    if a.startswith('--'):
        k = a[2:]; v = args.pop(0)
        opts[k] = float(v) if k in ('thresh',) else (int(v) if k in ('from', 'to') else v)
    else:
        bones.append(a)
g = json.load(gzip.open(f'{root}/golden/{scen}.json.gz'))
k = json.load(gzip.open(f'{root}/build/kumo-traces/{scen}.json.gz'))
def ang(a, b):
    d = math.sqrt(sum((x - y) ** 2 for x, y in zip(a, b))); s = math.sqrt(sum((x + y) ** 2 for x, y in zip(a, b)))
    return math.degrees(2 * math.atan2(min(d, s), max(d, s)))
def euler(q):
    x, y, z, w = q
    # ZYX-ish readout: just print quaternion + axis-angle for readability
    n = math.sqrt(x*x+y*y+z*z)
    if n < 1e-9: return "id"
    return f"{math.degrees(2*math.atan2(n, w)):6.1f}@[{x/n:+.2f},{y/n:+.2f},{z/n:+.2f}]"
key = opts['key']
for i, (fg, fk) in enumerate(zip(g['frames'], k['frames'])):
    if i < opts['from'] or i > opts['to']: continue
    out = []
    names = bones or sorted(fg['bones'])
    for b in names:
        bg, bk = fg['bones'].get(b), fk['bones'].get(b)
        if bg is None or bk is None: continue
        if opts['vec']:
            v = opts['vec']
            if v in bg and v in bk and bg[v] != bk[v]:
                dv = max(abs(x - y) for x, y in zip(bg[v], bk[v]))
                if dv > 0.01: out.append(f"{b}.{v} ref={bg[v]} kumo={bk[v]}")
            continue
        if key not in bg: continue
        e = ang(bg[key], bk[key])
        if e > opts['thresh']:
            out.append(f"{b}: {e:5.2f} ref={euler(bg[key])} kumo={euler(bk[key])}")
    if out:
        try:
            print(f"-- frame {i} tick {fg['tick']:.2f}")
            for o in out: print("   ", o)
        except BrokenPipeError:
            sys.exit(0)
