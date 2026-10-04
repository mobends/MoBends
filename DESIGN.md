# KUMO format rework

Working notes for the KUMO animator format and the files around it (type files, model
definitions), before v2 ships. Two parts:

1. **The new design**: how the system works, on its own terms.
2. **Migrating from the old design**: why it changes, how each old construct maps onto the new
   one, measurements, and the work to get there.

Parts of the format this rework doesn't touch (layers, machines, selectors, connections, pose
composition, masks, mirroring, `extends`, extensions, trust and resource-pack limits) are as
specified in `misc/kumo-format.md`, with conditions written as expressions, nodes and connections
written as in *Structured objects*, and mirroring as in *Mirroring*. When a decision here
is implemented, the spec is updated and the entry can leave this document.

---

# Part 1: The new design

## Files

- **Type files** say which model and animator an entity gets while a selector holds. The
  selector is an expression over operations that need no entity data (`core:entity_type`,
  `core:player_name`, `core:player_uuid`, `mobends:skin_variant`).
- **Model definitions** describe a mob's bones over its vanilla model, and the values the mob
  exposes to animators: they are the only files that declare `entity.` definitions.
- **Animators** are layers of state machines whose nodes pose the bones.
- **Extensions** add layers on top of a type's animator.

**Type selector precedence** counts conditions as today, over expressions: an operation that
isn't `and`, `or`, `not` or `if` counts 1 (comparisons included); `and` counts the sum of its
operands, `or` the fewest of any operand (it holds only as narrowly as its broadest branch),
`not` 1, and `if` its condition plus the fewer of its two branches.

**Every mob is described by files.** Mo' Bends' own mobs, the player included, are type files
plus model definitions like any mod's or resource pack's: `mobends:player` is a type file Mo'
Bends ships. A mod or pack needs Java only to bring new operations or drivers.

## How constructs are written

Operations, statements and pose items are all written the same way: an object with **exactly one
key that doesn't start with `@`**. That key names the construct; its value is the construct's own
content, as the construct declares it: an array of positional arguments (operations, `set`) or
an object of named fields (clips, drivers).

Every other key is a **modifier**: it starts with `@` and says how the engine treats the
construct, not what the construct is.

```json
{"add": ["layer.combo", 1]}
{"mobends:is_sitting": [], "@fallback": false}
{"@when": "nodeIsActive", "set": ["layer.combo", 0]}
{"@when": "entityIsRiding", "core:axis_rotate": {"bone": "head", "axis": "X", "angle": {"neg": ["entityHeadPitch"]}},
 "@space": "PRE"}
{"core:clip": {"animationKey": ".../walk.json", "frame": "entityLimbSwing"}, "@damping": {"body": 0.5}}
```

- **Modifiers are a closed set Mo' Bends owns**, and each kind of construct accepts its own:
  pose items `@when`, `@space`, `@damping`, `@snap`, `@mirror`, `@swapSides`, `@vectorModes`;
  statements `@when`; operations `@fallback` (where the operation declares it). Any other key is
  a load error. A modifier never collides with a construct's own field, and adding a modifier
  never breaks an addon's driver.
- **`@comment`** is accepted on every object of every file (constructs, structured objects,
  definitions, a file's root). Its value is a string, and the engine ignores it. A misspelt
  `@coment` is a load error like any unknown `@` key.
- Fields only some constructs have stay their own fields (`weight` on clips and
  `core:step_turn`, `inout` on accumulators and springs).
- Operations and drivers share one registry namespace: `core:spring` names one thing.
- **Style:** put `@when` first when there is one, so a reader knows something applies
  conditionally before reading what. (JSON objects are unordered; this is a convention for
  files, not something the loader checks.)

### Structured objects

Structured objects (layers, machines, selector branches, connections, the mirror rule) keep their
fixed keys: their place in the file says what they are. Every key they don't declare is a load
error, as on a construct.

**Nodes follow the one-key rule.** A node's one key without `@` is its type, and its value is
what the type declares: `core:pose` takes the pose list and the posing fields (`pose`,
`enterPose`, `snapOnEnter`, `damping`), which only mean something for a node that poses.
`core:fallthrough` and `core:vanilla` take `{}`. Addons register node types, and the runtime owns
every `@` key beside the type, so neither can add a key that collides with the other's.

What a scope has whatever else it is, is written under **`@` keys**:

| key | on | holds |
|---|---|---|
| `@define` | every scope | the scope's definitions (*Definitions and scopes*) |
| `@on` | every scope | the scope's statement lists (*Statement lists*) |
| `@connections` | nodes, machines, layers | the connections out of the node, or out of any node inside the machine or layer |
| `@when` | layers, the mirror rule | the condition under which the layer runs, or under which mirrored items mirror |

```json
"walk": {
  "core:pose": {"pose": [ ...items... ], "damping": {"body": 0.5}},
  "@define": {"stride": {"live": {"mul": ["entitySpeed", 2]}}},
  "@on": {"enter": [{"set": ["animator.onFeet", true]}]},
  "@connections": [{"when": "animator.jumping", "then": "jump", "transitionDuration": 2}]
},
"animated": {"core:fallthrough": {}}
```

**A condition is `@when`**, on constructs and scopes alike, with one exception: **selector
branches and connections** are `{"when": ..., "then": ...}` objects. Choosing where to go when a
condition holds is all they are, so their condition and their target are their content, not a
modifier. A connection's other keys (`transitionDuration`, the easing) sit beside them.

Keys inside the `@` keys need no `@`: they are the format's own (`enter`, `then`) or names the
file declares (`stride`).

## Values and expressions

### Types

Every expression is checked when it is loaded and is either a **number** or a **boolean**.
`entityLimbSwing` where a condition goes (a number where a boolean goes) or
`{"add": ["entityIsOnGround", 1]}` is a load error. At runtime values are floats. Strings never are values: they appear only as
constant arguments of operations that declare them.

### Forms

| form | example | type |
|---|---|---|
| number | `0.5` | number |
| boolean | `true` | boolean |
| string | `"minecraft:torch"` | only as an argument an operation declares as a string |
| scoped name | `"layer.combo"` | the definition's |
| built-in value | `"nodeTicksElapsed"`, `"nodeIsFadingOut"` | the built-in's |
| operation | `{"add": [...]}`, `{"mobends:is_sitting": [], "@fallback": false}` | the operation's |

Every place that takes a condition takes a boolean expression: `@when` on pose items,
statements, layers and the mirror rule; `when` on selector branches and connections.

### Names

**Built-in values are bare names** (`nodeTicksElapsed`, `partialTicks`, `entityIsOnGround`).
They are what Mo' Bends provides: always available, no arguments, independent of the entity's
class. Bare names belong to Mo' Bends; a bare name that isn't a built-in is a load error, and
adding a built-in never collides with anything a file declares.

**Everything a file declares carries its scope**: `entity.foo`, `animator.foo`, `layer.foo`,
`machine.foo`, `node.foo`.

- There is no lookup through enclosing scopes and no shadowing: the prefix says where the value
  lives.
- `machine.` is the innermost machine around the place the name is written, the way `node.` is
  the node being evaluated. If that machine doesn't declare the name, it is a load error, even
  when an outer machine does: a value machines nested in each other share is declared on the
  layer.
- **A name is one prefix and one name**: a definition's own name can't contain a dot, and there
  are no deeper paths (`machine.sword.foo`, another layer's `layer.arms.foo`). What two scopes
  share is declared on the smallest scope around both: the layer for its machines, the animator
  for its layers (*Following another layer's choice*), the entity for extensions. Every name then
  reads a scope that exists wherever it is read; a node's or machine's scope exists only while
  the layer is in it. A path naming an outer machine is safe and stays possible, if nested
  machines ever need it.
- The separator is a dot, so a scoped name never looks like a registry id (`core:holds_item`).
- Each name resolves, at compile time, to one slot of the program or of the entity's state.
- An unknown name, or a name in a scope that doesn't declare it, is a load error.

### Operations

An operation is `{"<name>": [arguments...]}`, plus the modifiers it accepts (see *How constructs
are written*). Arguments are positional only.

- **Bare operation names are the language** (`add`, `if`, `rose`): a fixed set, part of the
  format, Mo' Bends' only.
- **`namespace:id` names are registered** Java logic, and the namespace says who registered it:
  `core:` is what Mo' Bends registers directly (operations on vanilla classes, node types,
  drivers), `mobends:` is Mo' Bends' own content registered through the addon API, and an
  addon's operations carry its mod id.
- **Spelling:** language names are short and camelCase (`lt`, `if`, `wrapDegrees`,
  `easeInOut`); registered ids are snake_case, as Minecraft's registry ids are
  (`core:holds_item`).

```json
{"mymod:smoothstep": ["node.t"]}
{"mobends:is_sitting": [], "@fallback": false}
{"core:holds_item": ["main_hand", "minecraft:torch"]}
{"core:equipment_name": ["head", "^Notch.*"]}
{"decreased": ["entityTicksAfterAttack"]}
{"mymod:distance_to_nearest": ["minecraft:zombie"], "@fallback": 100}
```

Each parameter has a kind, checked at load:

| kind | accepts | example |
|---|---|---|
| number | any number expression, evaluated every frame | `"node.t"`, `{"mul": [...]}` |
| boolean | any boolean expression, evaluated every frame | `"nodeIsActive"`, `{"not": [...]}` |
| constant number | a number literal only | a window size |
| string | a string literal only | `"minecraft:torch"`, `"^Notch.*"` |
| choice | one of a fixed set of strings | `"main_hand"` / `"off_hand"`, `"X"` |

- A wrong argument fails with the operation's own words: `'core:holds_item' argument 2 (item)
  must be a string, got an expression`.
- An operation takes a few arguments (about three at most). Anything with more configuration is
  a typed object instead: a driver or a statement.

**`@fallback`** is the modifier of operations that may not apply to the entity's class: when the
operation can't bind to the class, the fallback (any expression of the same type) is compiled
instead. Without one, that is a load error.

### Reading the entity: `field`

`{"field": ["ridingEntity", "motionX"]}` reads `entity.ridingEntity.motionX`: the array is a
**path**, one field per step, starting at the entity.

**`field` is written only in entity-level definitions** (a model definition's `@define` and
`@on`); anywhere else it is a load error. Animators read `entity.` names and stay portable: how a
mob provides a value stays in its own files. Model definitions are trusted-only, so a resource
pack can't read arbitrary fields. The restriction can be lifted later without breaking a file.

- Resolved at load: each step is looked up on the declared type of the step before it, starting
  at the entity's class and walking up superclasses. The last field must be a number or a
  boolean, which is the expression's type.
- Fields are named by their development (MCP) names. Vanilla classes go through generated
  accessors (`GenVanillaFields.kt`), anything else through reflection.
- `@fallback` takes any expression of the same type: another field, a name, a constant, an
  operation. If the path can't be resolved on the entity's class, the fallback is compiled
  instead; if a step is `null` at runtime (not riding anything), the fallback is evaluated for
  that frame. Without a fallback, a path that can't be resolved is a load error.

  ```json
  {"field": ["destPos"], "@fallback": {"field": ["flapTarget"]}}
  {"field": ["ridingEntity", "renderYawOffset"], "@fallback": 0}
  {"field": ["isCharging"], "@fallback": false}
  ```
- Interpolating between ticks is written out:
  `{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}`.

### Stateful operations

A few language operations remember something between frames:

- `decreased` [x]: holds on the frame `x` is lower than on the previous frame (an edge trigger);
- `rose` [b] / `fell` [b]: hold on the frame boolean `b` turns true / false.

Each place one is written gets its own memory (two uses keep two memories), a hidden slot rather
than a declared state, which starts over
when the scope holding that place is created. A place inside a definition belongs to the scope
that declares the definition: one memory however many places read it, reset with that scope
(*Three kinds of definitions*).

**Nothing short-circuits**: `and`, `or` and `if` evaluate every operand, both branches of an `if`
included, so an edge trigger never misses a frame. The compiler may skip an operand that contains
no stateful operation, since the result is the same.

### Functions (after v2)

Functions are not in v2, but the format keeps room for them. A function is a definition with
parameters, for an expression repeated with different inputs:

```json
"@functions": {
  "wobble": {"params": {"t": "number", "amount": "number"},
             "body": {"mul": [{"sin": [{"mul": ["arg.t", 6.28]}]}, "arg.amount"]}}
},
...
{"core:axis_rotate": {"bone": "head", "axis": "Z",
                      "angle": {"animator.wobble": [{"div": ["nodeTicksElapsed", 20]}, 5]}}}
```

The intended design:

- **A call is inlined**: it behaves exactly as if the body were written out at the call site
  with the arguments substituted. The body is type-checked once, where it is declared. So every
  call site gets its own slots for the body's stateful operations, a body has no `set` (it is an
  expression), and recursion is a load error.
- **Called by scoped name** as the operation key (`{"animator.wobble": [...]}`). The scope says
  where it is declared, and the key can't collide with language (bare) or registered (`ns:id`)
  names.
- **Parameters** are read as `arg.t`, declared with the kinds operations have (number, boolean,
  constant number, string, choice), and passed positionally.
- **Declared** under `@functions` on the animator, layers and machines. A body reads its
  parameters, built-ins and the definitions of its declaring scope. Files share functions only
  through `extends`.

**Reserved until then:** the `@functions` key, and operation keys of the form `<scope>.<name>`.
Both are load errors in v2, so adding functions breaks no file.

## Definitions and scopes

### Scopes

The scopes are the entity, the animator, a layer, a machine and a node. A scope is **created
when it is entered** and **disposed when it is left**, once no transition needs it any more (a
node fading out keeps its scope until the crossfade ends):

- the entity's when its data is created; an animator's when it is created;
- a layer's when the layer starts; a machine's on every entry into it, so its definitions
  start over each time, and it is disposed once the last of its nodes has faded out;
- a node's on every entry, so its definitions start over each time.

Machines have no clock and no phases of their own: the layer crossfades between nodes, never
between machines. A machine that needs a clock keeps one as `machine.` state advanced in its
`update` list. If that becomes common, a built-in is added, named for its scope the way
`nodeTicksElapsed` and `layerTicksElapsed` are (`machineTicksElapsed`), not an operation taking
the scope as an argument.

### Three kinds of definitions

Every scope can declare definitions of three kinds:

| kind | value | changed by |
|---|---|---|
| **constant** | computed once, when its scope is created; may snapshot other definitions' values at that moment (a node constant can capture the body yaw a turn started from) | nothing: read-only |
| **state** | an initial value computed when its scope is created | `set` statements |
| **live** | evaluated once per frame, every frame its scope exists, whether or not anything reads it (so a stateful operation inside it steps once per frame and never misses one; a live definition without one may be evaluated lazily) | nothing: derived |

A scope declares them in `@define`, keyed by name. Each definition is written like a construct:
an object with exactly one key, its kind, whose value is the expression:

```json
"@define": {
  "startYaw": {"constant": "entityBodyYaw"},
  "combo":    {"state": 0},
  "stride":   {"live": {"mul": ["entitySpeed", 2]}}
}
```

- A name is declared once per scope, whatever its kind; a definition with two kinds breaks the
  one-key rule and is a load error.
- A definition's other keys are `@` modifiers, as on constructs. Only `@comment` is defined;
  they are where per-definition rules go if any are needed (an `@override` for `extends`, what a
  type publishes to its extensions).
- Related definitions sit together whatever their kind, and a reader finds one by its name.

Definitions may refer to each other in any order; cycles are a load error.

### Entity-level definitions

The entity scope holds what a mob exposes and remembers. **Only the mob's model definition
declares it**, in its `@define` (and `@on`):

- It names the entity class, which `field` reads and operations bind against.
- It is the one file every type, animator and extension for that model shares. Several types can
  choose one model (a pack's type for one player name and the default type); if types declared
  entity values, which `entity.` names exist would depend on which type won, and a file reading
  `entity.foo` would load for one entity and fail for another of the same class. Type files stay
  selectors: they run before entity data exists.
- Extensions don't declare entity definitions: what an extension needs for itself is in its own
  animator's `@define`. Two extensions can't collide on an `entity.` name, and a duplicate name is
  a load error within one file.
- Model definitions have no `extends` yet. Most of what every biped has is a built-in; sharing
  between definitions is added when a mob needs it.

What it declares:

- **live** definitions over `field`, e.g. the chicken's wings:

  ```json
  "@define": {
    "flapWave":  {"live": {"add": [{"mcsin": [{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}]}, 1]}},
    "flapSpeed": {"live": {"lerp": [{"field": ["oFlapSpeed"]}, {"field": ["destPos"]}, "partialTicks"]}},
    "wingAngle": {"live": {"mul": ["entity.flapWave", "entity.flapSpeed", 57.29578]}}
  }
  ```
- **state** a mob keeps across frames, updated with `set` (see *Entity values*).

With program and state split (see *Runtime*), entity state lives in the entity's state array next
to the animator's.

## Statements

### State changes only through statements

A statement is `{"set": ["layer.combo", <expression>]}`, optionally with `@when`. Statements
appear only in statement lists; an expression never changes state, so the order and number of
times expressions are evaluated never matter.

### Statement lists

Every scope can have three, in its `@on` object:

- `enter`: run when the scope is created, right after its definitions are initialised;
- `update`: run every frame the scope exists, **including while a node is fading out**;
- `exit`: run when the scope is disposed (for a node fading out, when the crossfade ends).

```json
"@on": {
  "enter": [{"set": ["layer.combo", 0]}],
  "update": [{"set": ["node.phase", {"add": ["node.phase", "ticksPerFrame"]}]}]
}
```

Selector branches and connections have a list of their own, run when they are taken.

**Lists run per frame.** There are no per-tick lists: a statement that counts or integrates
scales by `ticksPerFrame`. Sensing that must happen once per game tick is Mo' Bends' job, behind
the `entity…` built-ins. A `tick` list can be added if something needs one. An entity that isn't
rendered isn't animated, so its frame-counted state doesn't advance and can miss an edge while
it is off screen; for animation that doesn't matter.

**Order in a frame.** The entity's `update` list runs first, then the animator's, then each
extension animator's in extension order. Then, layer by layer, after the layer has chosen its node
and before it poses: the layer's list, its machines' (outermost first), the node fading out (if
any), then the current node.

**Order on a transition.** The `exit` lists of the scopes disposed at once run first (the node
being left during a crossfade, machines left), then the taken branch's or connection's own list,
then the `enter` lists of the scopes it enters, outermost first.

### Who may set what

A scope may `set` its own state and the state of the scopes around it: a node may set its
layer's, its animator's or the entity's state, and so may an extension's layers. A value meant to
be private is declared in a smaller scope. During a crossfade both nodes may set the same layer or
entity state; `nodeIsFadingOut` lets a statement opt out.

- **Trust.** An untrusted file may only set state declared by an untrusted file. Otherwise an
  untrusted extension could steer the trusted layers through a value they read, and the
  resource-pack limits, which clamp untrusted layers against the trusted pose, would no longer
  hold. Model definitions are trusted-only, so in practice an untrusted extension sets only its
  own animator's, layers' and nodes' state.
- **Order of writes.** When several layers set one value in a frame, the last write wins: later
  layers see it this frame, earlier ones next frame. Extension order is the user's ranking, so
  re-ranking extensions can change what they see.

### Following another layer's choice

A layer that depends on what another layer chose reads a definition, never the other layer's
node:

- **A choice without memory** (a function of current inputs) is a live definition, used by the
  choosing layer's selector and by every layer that follows it:

  ```json
  "@define": {"walking": {"live": {"and": [{"not": ["animator.jumping"]}, {"not": ["entityIsStandingStill"]}]}}},
  "select": [{"when": "animator.jumping", "then": "jump"}, {"when": "animator.walking", "then": "walk"}, {"then": "stand"}]
  ```
- **A choice with memory** (a selector that holds its node in a dead band, connections, timers)
  is state the choosing layer publishes, set when the node is entered (or in the taken branch's
  own list). Not in the `exit` list: that runs only when the crossfade ends.

  ```json
  "@define": {"onFeet": {"state": true}},
  "nodes": {
    "jump":  {"core:pose": {...}, "@on": {"enter": [{"set": ["animator.onFeet", false]}]}},
    "walk":  {"core:pose": {...}, "@on": {"enter": [{"set": ["animator.onFeet", true]}]}},
    "stand": {"core:pose": {...}, "@on": {"enter": [{"set": ["animator.onFeet", true]}]}}
  }
  ```

### `extends`

An animator that `extends` another shares one `animator.` scope with it: `extends` is one animator
split across files, and the child names the file it depends on.

- The parent's `@define` and `@on` merge into the child's, the parent's first, as its layers are.
  The child reads the parent's names directly (`cow.json` reads `quadruped.json`'s
  `animator.walking`).
- Redeclaring a parent's name is a load error (no shadowing). If a child ever needs to replace a
  value, that is a definition modifier (`@override`), added when needed.
- The parent's statement lists run before the child's, for `enter`, `update` and `exit` alike.
- A renamed parent definition fails the child at load, as a renamed node does.

### Extensions

An extension's animator is a scope of its own: its `animator.` names are its own, so extensions
never see or pollute each other's or the extended animator's. What an extension may follow is what
the type publishes at entity level (`entity.walking`, `entity.onFeet`), an explicit contract between
a mob's files and the extensions written for it.

An `entity.` name the model doesn't declare is a load error for the extension that reads it: that
extension is skipped, and the type and the other extensions still animate. There is no fallback
for a name (a name is a string and carries no modifiers); an extension is written for a mob's
files.

## Nodes, transitions and time

### Node phases

Built-in booleans for the node being evaluated; exactly one holds at a time:

| | the node is | ends when |
|---|---|---|
| `nodeIsFadingIn` | the layer's current node, while the crossfade into it runs | the crossfade ends |
| `nodeIsActive` | the layer's current node, fully in | the layer leaves it |
| `nodeIsFadingOut` | the node the layer left, still posed while the crossfade runs | the crossfade ends; the scope is disposed |

The current node is `nodeIsFadingIn or nodeIsActive`.

**A node never runs twice at once.** A transition to the current node restarts it without a
crossfade; a transition during a crossfade freezes what is on screen into a snapshot and fades
from that, and the node being left is disposed (its `exit` list runs) at once.

### Clocks and time

| built-in | value |
|---|---|
| `ticks` | the local player's age in ticks plus `partialTicks`: the same for every entity; it restarts when the local player respawns or changes dimension |
| `partialTicks` | progress between two game ticks, 0..1 |
| `ticksPerFrame` | ticks this frame lasted (capped at 1, 0 while paused) |
| `nodeTicksElapsed` | ticks since the node being evaluated was entered |
| `layerTicksElapsed` | ticks since the layer started |
| `nodeFadeProgress` | the linear progress of the crossfade the node is part of, the same number on both sides: 0 when the transition starts, 1 when it ends, 1 with no crossfade running. How much of a node is shown is `nodeFadeProgress` fading in and `1 − nodeFadeProgress` fading out; the transition's easing applies to the blend, not to this value |
| `nodeIsFinished` | the node's timed clips are done (below) |
| `random` | a random number, 0..1 |

The node and layer clocks advance by `ticksPerFrame` after the frame is posed (a node reads 0 on
its first frame) and pause while the layer's `@when` doesn't hold.

**`nodeIsFinished` holds** from the frame every clip of the node that has a `duration` has run it
(`nodeTicksElapsed >= duration`), on every frame after, until the node is left. It is not an
edge: the frame it becomes true is `{"rose": ["nodeIsFinished"]}`. A node with no items is
finished at once; a node whose items are all untimed never is.

```json
"sit_down": {"core:pose": {"pose": [{"core:clip": {"animationKey": ".../sitting_down.json", "duration": 8}}]},
             "@connections": [{"when": "nodeIsFinished", "then": "sit", "transitionDuration": 1}]}
```

**Timers are functions of a clock.** A value that rises from 0 to 1 over the first ticks of a
node is `{"linstep": ["nodeTicksElapsed", 0, 10]}`; a wait is `{"ge": ["nodeTicksElapsed", 80]}`.
Neither needs state.

## Pose items

A pose item is an entry in a node's `pose` list that contributes to the pose, composing onto the
items before it: `{"<kind>": {fields}}`, with the pose-item modifiers `@when`, `@space`,
`@damping`, `@snap`, `@mirror`, `@swapSides`, `@vectorModes` (and `@comment`).

- A **clip** (`core:clip`) plays keyframes. Built-in values for its `frame`: `clipLength` (the
  clip's length in its own units) and `clipDuration` (the item's `duration`).
- A **driver** is a pose item defined in Java: `core:axis_rotate`, `core:vector`, `core:offset`,
  `core:step_turn`, `core:accumulate`, `core:spring`, `mobends:cape`, `mobends:sword_trail`,
  the spider legs. Drivers take named fields, any number (`core:step_turn` has about 30). A
  driver may have side effects beyond the pose: `mobends:sword_trail` poses no bone and feeds the
  trail the renderer draws.

How the three constructs compare:

| | operation | statement | driver |
|---|---|---|---|
| produces | a value | a change to state | a contribution to the pose (and side effects) |
| written | `{"name": [args]}`, positional, about 3 at most | `{"set": [...]}` | `{"name": {named fields}}` |
| appears in | any expression | a scope's `enter` / `update` / `exit` list, a transition's list | a node's `pose` (and `enterPose`) |
| order | doesn't matter | list order | pose-list order |
| state | optional, per use | the state it names | optional, private (and may write declared state) |

### Driver outputs, accumulators and springs

A driver writes declared state only through fields that name it:

- **`out`**: an object mapping the driver's output names to declared number states, which the
  driver writes every frame it runs. `core:step_turn`'s outputs are `turnLag`, `turnSpeed`,
  `stepLift`, `stepImpact` and `stride`; an output left out isn't written.
- **`inout`**: the one state a driver reads and steps (accumulators and springs).

Each named target must be a `state` definition of type number (not a live definition or a
constant), checked at load.

`core:accumulate` and `core:spring` step their state once per frame, in pose-list order, so
they can follow what a driver before them computed in the same frame (springs following
`core:step_turn`'s outputs):

```json
"@define": {"onFeet": {"state": 0}, "turnSpeed": {"state": 0}, "armLag": {"state": 0}},
"core:pose": {"pose": [
  {"core:accumulate": {"inout": "node.onFeet", "min": 0, "max": 1,
                       "rate": {"if": ["animator.jumping", -0.15, 0.15]}}},
  {"core:step_turn": {"weight": "node.onFeet", "legs": [...], "out": {"turnSpeed": "node.turnSpeed"}}},
  {"core:spring": {"inout": "node.armLag", "target": "node.turnSpeed", "stiffness": 0.12, "friction": 0.3}}
]}
```

- `core:accumulate`: `inout`; `rate` (an expression, per tick, any sign); `min`, `max`
  (unbounded when left out). With `min` 0 and `max` 1 it is a ramp.
- `core:spring`: `inout`; `target` (an expression); `stiffness` (per tick²); `friction` (per
  tick). Its velocity is internal: a hidden slot of the spring, reset when the spring's node is
  entered, even when its value is `layer.` or `entity.` state and persists.
- **The value lives where its state is declared; the driver steps it while its node is posed.**
  A driver may name any scope's state, but runs only while its node is current or fading out. A
  value that must change every frame whatever the node is a `set` in an `update` list, or a
  built-in.

### Mirroring

A layer's mirror rule says when its mirrored items mirror, and which bones pair up:

```json
"mirror": {
  "@when": "entityIsLeftHanded",
  "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"],
            ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"],
            ["leftHeldItem", "rightHeldItem"]]
}
```

While the rule's `@when` holds, an item with a mirror modifier runs on a reflection of the pose
built so far, and its result is reflected back. Reflecting twice gives back the original, so the
item's composition rules don't change.

| | paired bones swap | Y and Z rotations, X offset negated |
|---|---|---|
| `@mirror` | yes | yes: the item's left-right mirror image |
| `@swapSides` | yes | no: the same motion, moved to the other side |

**Values are never negated.** Every value means the same everywhere, mirrored or not; the rule
has no `negate` list. What an item does mirrored follows from its modifier:

- **`@mirror`** for motion that belongs to a side: swings, clips, a head turned toward the
  weapon (`head`, Y, `-30` becomes `+30`).
- **No modifier** for an item on an unpaired bone that follows a world direction. The head
  turned by `entityHeadYaw` must look where the entity looks, so it isn't mirrored.
- **`@swapSides`** for an item on a paired bone that follows a world direction: the right arm
  turned by `entityHeadYaw` becomes the left arm turned by the same angle, still pointing where
  the entity looks.
- An item that mixes the two is split into two items.

If an input that has to be negated inside one mirrored item turns up, a built-in (`isMirrored`)
can be added without breaking a file.

## Entity values

**The rule:** a value that holds for every entity is a built-in, named `entity…`; one that
depends on the entity's class is a namespaced operation (`core:` for vanilla classes, `mobends:`
for Mo' Bends' own content). Every animated entity is an `EntityLivingBase` (addons can only
register living entities), so "every entity" means anything vanilla declares on `Entity` or
`EntityLivingBase`, or that Mo' Bends computes for every living entity.

### Entity built-ins

| built-ins | from |
|---|---|
| `entityLimbSwing`, `entityLimbSwingAmount`, `entitySwingProgress`, `entityHeadYaw`, `entityHeadPitch` | the arguments the vanilla renderer passes to every model, captured by the mutator (`entitySwingProgress` is vanilla's swing progress interpolated by `partialTicks`) |
| `entityIsSprinting`, `entityIsSneaking`, `entityIsAlive`, `entityIsInWater`, `entityIsRiding` | `Entity` |
| `entityIsChild`, `entityIsLeftHanded`, `entityHealth`, `entityIsSwinging`, `entityIsSleeping`, `entityIsElytraFlying` | `EntityLivingBase` (false or 0 unless a subclass says otherwise) |
| `entityId` | vanilla's entity id (per-entity variety) |
| `entityTicksExisted`, `entityTicksElytraFlying` | vanilla's age and glide time |
| `entityItemUseTicks`, `entityItemUseTicksLeft` | ticks the item in use has been used so far, and ticks left |
| `entityIsOnGround`, `entityIsClimbing` | Mo' Bends' own detection: collision boxes and stairs, with liftoff detection; ladders |
| `entityTicksInAir`, `entityTicksAfterTouchdown`, `entityTicksFalling`, `entityClimbingCycle` | Mo' Bends' counters over that detection |
| `entityTicksAfterAttack` | ticks since the last counted swing (below) |
| `entityIsUnderwater`, `entityLedgeHeight`, `entityClimbingRotation` | Mo' Bends' block checks around the entity |
| `entityIsDrawingBow`, `entityIsRidingLiving` | the held items; what the entity rides |
| `entityIsStandingStill`, `entityIsStrafing` | Mo' Bends' measured motion |
| `entityMotionY`, `entityPrevMotionY`, `entityInterpolatedMotionY`, `entitySpeed`, `entityXZSpeed`, `entityForwardMomentum`, `entitySidewaysMomentum` | Mo' Bends' measured motion: position changes each tick, so it works for every entity. `entitySpeed` and `entityXZSpeed` are the magnitudes interpolated by `partialTicks` (`getInterpolatedMotionMagnitude`, `getInterpolatedXZMotionMagnitude`) |
| `entityBodyYaw`, `entityWorldX/Y/Z` | the body yaw and position, interpolated as the renderer does (`core:step_turn`'s default inputs) |
| `entityRidingRelativeHeadYaw`, `entityRidingRelativeYaw` | relative to the ridden entity (0 unless riding something living) |
| `entityClimbingRenderYaw`, `entityClimbingBodyYaw`, `entityClimbingHeadYaw` | the yaws while climbing |

Values left out are added when something needs them; adding a built-in never breaks a file.

**`entityTicksAfterAttack`** counts swings for every entity: once per game tick it checks
vanilla's `isSwingInProgress`, counts a swing when one starts, and again whenever more than 5
ticks have passed while it keeps going (holding attack, mining). The counter goes back to 0 on a
counted swing and grows by `ticksPerFrame` every frame. `{"decreased": ["entityTicksAfterAttack"]}`
is the edge of a counted swing (`{"rose": ["entityIsSwinging"]}` would miss the re-counts of a
swing that keeps going).

### Values specific to a mob

Class-specific logic is a registered operation; class-specific memory is `entity.` state in the
mob's model definition.

| operation | |
|---|---|
| `core:is_flying` | `EntityPlayer` flying |
| `core:holds_item` [hand, item], `core:holds_any_item` [hand] | what a hand holds (`main_hand` / `off_hand`; an item id) |
| `core:active_hand_side` [side] | the side (`left` / `right`) of the hand the entity is using an item with |
| `mobends:use_action` [action], `mobends:attack_action` [action] | Mo' Bends' classification of the item in use (`food` / `bow` / `shield`, `UseActionType`) and of the held item's attack (`fists` / `sword` / `tool`, `ItemActions`, configurable) |
| `core:equipment_name` [slot, pattern] | the display name of equipment |
| `mobends:is_sitting`, `mobends:wolf_interested_angle`, `mobends:wolf_shake_angle` [offset], `mobends:wolf_tail_rotation` | the wolf |
| `mobends:is_beside_climbable`, `mobends:spider_wall_rotation` | the spider |
| `mobends:spin_attack_enabled` | the player's spin-attack config option |
| `core:entity_type`, `core:player_name`, `core:player_uuid`, `mobends:skin_variant` | type-file selectors only (no entity data exists yet) |

The player's state, for example. Its swing counter ignores a swing within 6 ticks of the last
counted one while the main hand holds an item, so a sword combo isn't restarted:

```json
"@define": {"ticksAfterAttack": {"state": 0}, "capeWavePhase": {"state": 0}},
"@on": {"update": [
  {"set": ["entity.ticksAfterAttack", {"add": ["entity.ticksAfterAttack", "ticksPerFrame"]}]},
  {"@when": {"and": [{"decreased": ["entityTicksAfterAttack"]},
                     {"or": [{"not": [{"core:holds_any_item": ["main_hand"]}]},
                             {"gt": ["entity.ticksAfterAttack", 6]}]}]},
   "set": ["entity.ticksAfterAttack", 0]},
  {"set": ["entity.capeWavePhase",
           {"mod": [{"add": ["entity.capeWavePhase",
                             {"mul": [{"if": [{"and": [{"core:is_flying": []}, "entityIsSprinting"]}, 4, 1]},
                                      "ticksPerFrame"]}]},
                    380]}]}
]}
```

Other mob-specific values in their files:

- player: `flightSpeedFactor` (`clamp(entitySpeed, 0, 0.2) / 0.2`), `flightPitch` (the angle
  between the motion and straight up, `atan2(entityXZSpeed, entityMotionY)` in degrees, times
  `flightSpeedFactor`), both live definitions,
  `canSpinAttack` (`mobends:spin_attack_enabled` and not `entityIsRiding`), `sprintJumpLeg`
  (state);
- squid: its rotation and the two "rotation low" flags (`field` reads);
- wolf: `tailWag` (over `mobends:wolf_interested_angle`, `entityTicksExisted`, `partialTicks`);
- zombie: its animation set (a constant from `entityId`) and walking style (a random value
  re-rolled every 80–100 ticks);
- spider: crawl progress and render yaw (state, or `mobends:` operations).

### Per-entity data

What an entity keeps besides its state: the entity, its bones and their smoothing, and render
outputs (the sword trail, the wolf pup's head size).

None of that differs by Java class, so **one generic data class serves every entity**: the bones
come from the model definition, and per-mob parts are **components** the model definition
switches on (`mobends:sword_trail`, `mobends:cape`). Code that cast to a data class today asks
for a component instead (`data.getComponent(SwordTrail.class)`). This is the end state of
*Moving mobs out of Java*: a mod's mob comes with no Java data class.

## Language reference

| group | operations |
|---|---|
| arithmetic | `add sub mul div min max` (2+, folded left), `mod` (floored) `pow atan2`, `neg abs sqrt floor ceil` |
| trigonometry | `sin cos` (radians); `mcsin mccos` (Minecraft's sine table, for values matching vanilla models; pure, so language rather than `core:`) |
| shaping | `clamp` [value, min, max], `lerp` [from, to, t], `easeIn easeOut easeInOut` [t, power] |
| steps | `linstep` [x, edge0, edge1] (0 below `edge0`, 1 above `edge1`, linear between), `smoothstep` [x, edge0, edge1] |
| angles | `wrapDegrees` [a], `lerpAngle` [from, to, t] (the short way round) |
| comparison | `lt le gt ge eq ne` |
| logic | `and or not` (no short-circuit) |
| choice | `if` [condition, then, else] (evaluates both branches) |
| entity | `field` [path] (+ `@fallback`), `exists` [path] (whether a field path reaches a non-null object) |
| per-use state | `decreased` [x], `rose` [b], `fell` [b] |

Built-in values: the clocks and node phases (*Clocks and time*, *Node phases*), `clipLength` and
`clipDuration`, the `entity…` built-ins (*Entity built-ins*).

## Operations and drivers in Java

An operation is a signature and up to three steps:

1. **Signature**, declared once and used for checking, error messages and documentation: the
   name (the registry adds the mod id), the parameters (name, kind, whether the last one
   repeats), the result type, the modifiers it accepts (`@fallback`), and whether it is
   **pure** (same arguments, same result, no entity, no state; a pure operation with constant
   arguments is computed once at load).
2. **Bind**, once when the program is compiled for an entity class: it gets the constant
   arguments and the entity class, does its one-time work (looks up `minecraft:torch` as an
   `Item`, compiles a pattern), and returns the evaluator, or says the operation doesn't apply to
   that class (the `@fallback` is used, or the load fails).
3. **Evaluate**, every frame: it gets the context (entity, built-in values, scopes), its evaluated
   number and boolean arguments, and its state.
4. **State**, for stateful operations only: named fields, scalars or **arrays**, with initial
   values. Array sizes can depend on what bind saw (a driver configured with four legs keeps four
   of each per-leg value). They are laid out flat in the entity's state array when the program is
   compiled; every place the operation is written gets its own, reset when the scope holding that
   place is created.

Three levels of registration, all through the registry that adds the mod id:

```java
// A pure function of numbers. Computed once at load when its argument is a literal.
registry.registerFunction("smoothstep", t -> t * t * (3 - 2 * t));

// Reads the entity. The class is the applicability check: other classes get the @fallback or
// a load error, and the operation never casts.
registry.registerEntityReader("is_wet", Entity.class, entity -> entity.isWet());

// Everything else: the full signature, bind, evaluate and state.
registry.registerOperation(Operation.named("distance_to_nearest")
        .param("entityType", Kind.STRING)
        .returns(Type.NUMBER)
        .bind((args, entityClass) -> {
            Class<? extends Entity> target = EntityList.getClass(new ResourceLocation(args.string(0)));
            if (target == null) throw args.error(0, "unknown entity type");
            return context -> nearestDistance(context.entity(), target);
        }));
```

- **Drivers** are registered the same way (signature, bind, evaluate, state) but take named
  fields. Their private state (`core:step_turn`'s planted feet, the spider's legs) is declared
  state, arrays included. A driver's signature names its outputs, which a file maps to states in
  `out`, and whether it takes an `inout` state (*Driver outputs, accumulators and springs*).
- A stateful operation or driver gets initial values for its state and no enter / exit hooks;
  hooks are added if something needs them.
- Mo' Bends defines its own operations and drivers with the same API. The language operations
  use the same signature model but live in a table addons can't write to, which keeps bare names
  Mo' Bends'.
- The language needs a little more typing than addons: `if` returns the type of its branches,
  `eq` compares two numbers or two booleans. That stays internal; addon operations have fixed
  types.

### Where the API lives

The operation and driver API is part of `core/`, the engine shared by every Minecraft version, so
it can't name a Minecraft type (`Entity`, `Item`, `ResourceLocation`). Each Minecraft version's
mod layer adds entity-typed helpers on top (`registerEntityReader`, the `Entity`-typed
`distance_to_nearest` above), and `AddonAnimationRegistry` adds the mod id.

### Decided

- **The entity in core** is an opaque `Object` and its `Class<?>`. Bind checks applicability
  with `isAssignableFrom` and returns the evaluator, or says the operation doesn't apply. The mod
  layer's helpers do the typed cast, so addon code never casts.
- **State is floats only.** A boolean is 0 or 1, and state holds no objects, which keeps an
  entity's state one flat `float[]`. A signature declares its slots and gets back handles,
  `FloatSlot` or `FloatArraySlot` (sized at bind: four legs keep four of each per-leg value);
  evaluate reads and writes through them on the entity's state. Everything drivers keep in Java
  fields today (`StepTurnDriver`'s planted feet, the spider's legs) is positions and angles.
  Whatever bind computes that isn't per entity (an `Item` looked up from `minecraft:torch`, a
  compiled `Pattern`) is captured in the evaluator, which is part of the program.
- **Driver fields are Gson template classes**, as today, with field types restricted to a fixed
  set: `NumberExpr`, `BoolExpr`, `StateRef` (for `inout` and `out` targets), `BoneRef`,
  primitives, enums, and lists of nested templates (`core:step_turn`'s `legs`). The loader
  reflects over the class to check kinds, word the errors and generate the docs. Operations keep
  their builder signature: they have a few positional arguments, drivers many named, nested
  fields.
- **Selector-safe operations** carry a flag in their signature: they bind against the entity
  class and evaluate against the selector context (player name, UUID, skin variant), not entity
  data. Any other operation in a type file's selector is a load error. This replaces
  `registerSelectorCondition`.

### To settle while prototyping

Settled by porting `core:spring` and `core:step_turn` to the new API, which will show any gap:

- **Arguments at evaluate** are passed already evaluated, through a reused view (`args.number(0)`,
  `args.bool(1)`) that doesn't allocate. Nothing short-circuits, so an operation never needs its
  arguments lazily.
- **What bind gets**: the constant arguments (`args.string(i)`, `args.choice(i)`,
  `args.constant(i)`), the entity class, `args.error(i, message)` for errors in the operation's
  own words, and, for drivers, bone lookup from name to index.
- **How much of the pose drivers see**: a narrow `PoseWriter` (by bone index, in a space:
  `PRE`, `POST`, `OVERRIDE`) rather than `Pose`, so the pose buffers can change without breaking
  addons.
- **Purity**: `registerFunction` implies it; the full builder opts in with `.pure()`. A pure
  operation that isn't only loses constant folding.

## Runtime

### Program and per-entity state

- **Program**: an animator is compiled into an immutable tree (the node and machine graph,
  expressions, resolved names and field paths, bound operations, bone indices), shared by every
  entity it was compiled for.
- **State**: every stateful element gets a slot at compile time: a node's clock, an
  accumulator's and a spring's values, a spring's velocity, an edge trigger's memory, a layer's
  current node, the definitions' state and constants. An entity owns only a flat state array and
  its pose buffers; evaluation takes the entity's state alongside the program.
- A scope's slots are one contiguous range, reset when the scope is created.
- **Constant folding.** A constant that reads nothing per entity (no entity value, no state, no
  `random`) is computed once and lives in the program, not in every entity's state; so is a pure
  operation with constant arguments. This is the compiler's choice and changes nothing in files.

### What a program is compiled for

A program is cached by everything that changes the compiled result:

- the animator file and its extensions, in order (with their `extends` chains);
- their trust, and the resource-pack limits in force;
- the entity class: `field` paths resolve against it and operations bind against it. Names don't
  depend on the entity: they are declared in files or are built-ins.

The cache is cleared on a resource reload and when the server's policy changes. A path or
operation that fails on one entity class fails that class's program only; failures are reported
once per animator file and extensions.

## Open questions

- **Addon API details.** Argument passing at evaluate, what bind gets, the pose drivers see,
  and purity, to settle while porting `core:spring` and `core:step_turn` (*To settle while
  prototyping*).

---

# Part 2: Migrating from the old design

## Policy

v2 has not shipped. Anything in the format, the serializers and the addon API can change, and no
upgrade path for v2 files is needed.

## Why

An audit of the v2 format (`misc/kumo-format.md`, the deserializers in
`core/kumo/state/serializer`, and the shipped animators) found:

- **Several small languages for one job.** Expressions (numbers), trigger conditions (truth
  values), type-file selector conditions, and the `variables[]` recipe of model definitions
  (`scale`, `offset`, `fn`, `add`, `product`) each compute values in their own syntax.
- **Conditions are the bulk of the animators.** Across the shipped animators: 280 `core:state`,
  154 `core:compare`, 134 `core:and`, 123 `core:not` (92 of them `not(state)`). The longest
  condition (in `player.json`) is 472 characters.
- **Seven ways of deciding what a JSON value is**: a `type` key, a `type` key with a default,
  which key is present (`driver` vs `animationKey`), the one key of an operation object, a
  string meaning a named condition in one place and a named expression or variable in another,
  and maps with a reserved `"default"` key (damping, additive space).
- **Keys with two meanings.** A ramp's `when` is its up/down switch, but every other item's
  `when` skips it (`PoseNode.java` special-cases `RampDriver`). `set` on nodes, branches and
  connections takes constants; the `core:set` driver takes an expression.
- **Meanings with two names.** `triggerCondition` (connections) vs `when` (everything else);
  `name` (ramp, accumulate, spring) vs `variable` (`core:set`); `condition` vs `conditions`,
  `value` vs `values`; `"jumping"` vs `{"type": "core:named", "name": "jumping"}`.
- **Unknown names fail late or not at all.** Variables and states are only checked on the first
  frame; an unknown property silently matches only `unset`; a model definition's missing `field`
  silently becomes the constant `offset`.

## From the old constructs to the new

### Conditions become expressions

`core:compare`, `core:and`, `core:or`, `core:not`, `core:named` and the separate trigger
condition registry go away: comparisons, `and`, `or` and `not` are language operations, and a
named condition is a scoped definition. The old rule that `core:and` / `core:or` evaluate every
operand (so `core:decreased` never misses a frame) carries over to `and` / `or`. Type-file
selectors (`core:entity_type`, ..., and their own `and` / `or` / `not`) use the same syntax.

New language operations: `linstep`, `smoothstep`, `wrapDegrees`, `lerpAngle`, the comparisons,
`and` / `or` / `not`, `if`, `field`, `exists`, `rose`, `fell`.

### Named values become definitions

Every old named value becomes a definition:

| old | new |
|---|---|
| named expressions and named conditions (`expressions`, `conditions`) | live definitions |
| layer `variables`; `set` maps of nodes, branches and connections | state; `set` statements in `enter` and transition lists |
| layer `variables` written only inside one machine (`player.json`'s and `skeleton.json`'s `combo` in `sword`, `fist` in `fists`) | `machine.` state; the layer's branches into the machines reset them to 0, which a machine's state does on entry anyway |
| node variables written by ramps, accumulators, springs, `core:set` | state written by drivers and statements |
| a model definition's `variables[]` (`field` / `prevField` / `scale` / `offset` / `fn` / `add` / `product`) | entity live definitions over `field` |
| subject variables and states registered in Java (`registerVariable`, `registerState`), properties (`getProperty`) | built-ins and registered operations (*Entity values from the data classes*) |

What a string meant in an expression depended on what happened to be declared: a named
expression, else a node variable once written, else a layer variable, else the subject's
variable. Names now carry their scope.

The old `field` list in model definitions meant fallback names (the first that exists); a `field`
path now means a chain of fields, and fallbacks are the `fallback` option.

### Connections are written like selector branches

A connection was `{"target": "jump", "triggerCondition": ...}`; it is
`{"when": ..., "then": "jump"}`, the same as a selector branch, with its other keys
(`transitionDuration`, the easing) beside them.

### Conditions are `@when`

Every other `when` (pose items, layers, the mirror rule) becomes `@when` (*Structured objects*).
A ramp's own `when`, which collided with the item `when`, goes away with the ramp (*Ramps,
springs and accumulators*).

### Comments are `@comment`

Files carried notes in a plain `comment` key (20 uses: `iron_golem`, `creeper`, `cow`, `wolf`,
their model definitions and the example extensions), which loaded only because Gson ignores keys
it doesn't know. Under the one-key rule a second key without `@` is an error, and structured
objects now reject unknown keys, so they become `@comment`.

### One key, plus modifiers

The old format told constructs apart three ways: an operation by its one key, a driver by a
`driver` key, a clip by having `animationKey`. A pose item's generic fields (`when`, `space`,
`damping`, ...) sat in the same flat object as the driver's own fields, which is how a ramp's own
`when` (its up/down switch) came to collide with the item `when` every other pose item has.

Now every operation, statement and pose item is one key naming it, with its own content as the
value, and `@` modifiers for what the engine does with it (*How constructs are written*):

| old | new |
|---|---|
| `{"driver": "core:axis_rotate", "bone": "head", "axis": "X", "angle": 10, "space": "PRE", "when": ...}` | `{"@when": ..., "core:axis_rotate": {"bone": "head", "axis": "X", "angle": 10}, "@space": "PRE"}` |
| `{"animationKey": ".../walk.json", "frame": ..., "damping": {...}}` | `{"core:clip": {"animationKey": ".../walk.json", "frame": ...}, "@damping": {...}}` |
| `{"type": "core:property", ...}` and other conditions | operations (*Conditions become expressions*) |

An earlier version of this design gave pose items a `type` key instead; the one-key form
replaced it, so that operations, statements and pose items share one rule. Nodes follow it too:
`{"type": "core:pose", "pose": [...]}` becomes `{"core:pose": {"pose": [...]}}`, and
`damping`, `snapOnEnter` and `enterPose`, which every node type inherited (`core:fallthrough` and
`core:vanilla` rejected them by hand), move into `core:pose`'s own fields. Layers and machines
keep their structured form.

### Scope keys under `@`

A node's fields were one flat object: what the engine reads for every node (`connections`,
`tags`, `set`, `expressions`, `conditions`) next to what its type reads (`pose`). Addons register
node types, so a field core added to every node could collide with a field an addon's node type
already had. Everything a scope has whatever its type now goes under `@define`, `@on` and
`@connections` (*Structured objects*):

| old | new |
|---|---|
| `expressions`, `conditions`, layer `variables` | `@define` (*Named values become definitions*) |
| `set` | a `set` statement in the `enter` list of `@on` |
| `connections` | `@connections`, on nodes, machines and layers |
| `tags` | removed (*Tags are removed*) |

### Statements

The old statement-like pieces were scattered: `set` maps of constants on a node (applied when it
starts), a branch and a connection (when taken; nested branches add theirs, outermost first, the
node's last); a layer's `variables`; `core:set`, a pose item assigning an expression every frame;
ramps, accumulators and springs, updating every frame in pose-stack order. They become `set`
statements in `enter`, `update`, `exit` and transition lists; `core:set` goes away.

### Ramps, springs and accumulators

The old items, all pose items whose value was a node variable, reset to `initial` on every entry
and stepped once per frame the node was posed (also while fading out; not while the layer's
`when` was false):

| | arguments | value |
|---|---|---|
| `core:ramp` | `name`; `speed` (per tick, default 0.1); `downSpeed` (default `speed`, 0 = never down); `when` (up while it holds, down otherwise; none = always up); `initial` (0); `readBeforeAdvance` | 0..1 |
| `core:accumulate` | `name`; `rate` (expression, per tick); `initial` (0); `min`, `max` | unbounded unless clamped |
| `core:spring` | `name`; `target` (expression); `stiffness` (per tick², 0.2); `friction` (per tick, 0.4); `initial` (0); velocity hidden | follows `target` |

Items after one read this frame's value, items before it last frame's (`readBeforeAdvance`
flipped that for the items after). A ramp ignored the item `when`; an accumulator or spring with
a `when` froze while it didn't hold. Only 7 of the 22 shipped uses need state: the 2 switching
ramps, the golem's 4 springs and the spider's `wigglePhase`.

**Timer ramps become `linstep`.** 14 of the 16 shipped ramps (`relax`, `bringUp`, `bringUpPrev`
in `player.json` and `skeleton.json`) have `downSpeed: 0` and no `when`: they only rise from 0 at
`speed` per tick from the node's entry.

| old | new |
|---|---|
| `{"driver": "core:ramp", "name": "relax", "speed": 0.1, "downSpeed": 0}`, read as `"relax"` | `{"linstep": ["nodeTicksElapsed", 0, 10]}` |
| `bringUp`, `speed` 0.7 (shield) | `{"linstep": ["nodeTicksElapsed", 0, 1.43]}` |
| `bringUp`, `speed` 0.15 (eating) | `{"linstep": ["nodeTicksElapsed", 0, 6.67]}` |
| `bringUpPrev` (the same with `readBeforeAdvance`), read by `{"core:compare": bringUpPrev >= 1}` | `{"ge": ["nodeTicksElapsed", 6.67]}` |

- Written once per node as a live definition where several items read it.
- A ramp stepped before the items after it read it, so its value ran one frame ahead of the
  `linstep` of the same clock (`clamp((nodeTicksElapsed + ticksPerFrame) × speed)`): these
  animations shift by one frame. Pauses and restarts are unchanged.

**Switching ramps merge into `core:accumulate`.** `onFeet` (iron golem) and `deep` (swimming
player) become accumulators clamped to 0..1 with a signed `rate`; `core:ramp` goes away with its
`when`, `speed`, `downSpeed` and `readBeforeAdvance`.

**`readBeforeAdvance` is dropped.** It was added when porting the old Java animation bits, to
match frame for frame the ones that computed their eased value before advancing their ramp
(`b595999`, "kumo: New animation system"). It only shifts a value by one frame; nothing depends
on it beyond the parity goldens. Its 5 uses: `bringUpPrev` (×4, gone with `linstep`) and `deep`.

**Accumulators and springs** keep their behaviour, but step a declared state named by `inout`
instead of a `name` with its own `initial`.

**Driver outputs are named.** `core:step_turn` wrote `turnLag`, `turnSpeed`, `stepLift`,
`stepImpact` and `stride` into the node scope by fixed names (`StepTurnDriver.publish`), and the
spider drivers `groundLevel`. They now write the declared states their `out` field names.

### Tags are removed

A node's `tags` were its layer's *actions*; `core:action` (`{"type": "core:action", "tag": "jump"}`)
held while any layer's current node had the tag (`KumoContext.isActionActive`, over
`LayerState.getActions()`, the current node's tags). It let one layer follow another's choice, and
extensions follow the animator they extend. Its quirks: a node fading out no longer counted; a
disabled layer (its `when` false) still did; a layer reading a later layer's tags saw that layer's
node from the previous frame; a misspelt or renamed tag silently never held.

`tags` and `core:action` go away for definitions (*Following another layer's choice*), and
extensions follow what the type publishes at entity level (*Extensions*). The 12 shipped uses:

| where | tag | what for | becomes |
|---|---|---|---|
| `player.json` (5) | `stand`, `walk`, `sprint` | the upper-body layers follow the locomotion layer | live definitions shared with the locomotion selector (its chain has no connections out of these nodes) |
| `pig_zombie.json` (4) | `stand`, `walk` | the hunch layer's selector and two items | live definitions |
| `skeleton.json` (1) | `walk` | a layer `when` | a live definition |
| `cow.json` (1) | `walk` | its selector follows `quadruped.json`'s walk node, which it `extends` | a live definition (`quadruped`'s selector is `jumping → jump; STANDING_STILL → stand; else walk`, no memory) |
| `iron_golem.json` (1) | `jump` | the `onFeet` ramp falls while the jump node is current | state published by the locomotion layer: its selector holds walk or stand in a dead band (`walkStart` / `walkStop`), and its jump node leaves through a connection |

`cow.json` shares `quadruped.json`'s `animator.` scope (*`extends`*), so it reads the walk
definition `quadruped.json` declares.

### Drivers

The 12 old drivers did three jobs:

- **pose writers** (`core:axis_rotate`, `core:vector`, `core:offset`, `core:step_turn`, the
  spider legs, `mobends:cape`): stay drivers;
- **value updaters** (`core:ramp`, `core:accumulate`, `core:spring`, `core:set`): see above;
- **side effects** (`mobends:sword_trail`): stays a driver.

Their private state moves from Java fields (`StepTurnDriver`'s planted feet, `SpiderData`'s
`Limb` objects) into declared state.

### Mirroring loses `negate`

A mirror rule's `negate` list made the named variables read negated inside mirrored items
(`KumoContext.resolveVariable`), and named expressions built from them followed, since they were
re-evaluated on every read. With names resolved to slots and live definitions evaluated once per
frame, that would need mirrored variants of definitions, and values that mean different things
in different places.

Every shipped use cancels out. The three rules (`player`, `skeleton`, `pig_zombie`) negate only
`headYaw`, and the only mirrored items reading it are
`{"driver": "core:axis_rotate", "bone": "head", "axis": "Y", "angle": "headYaw", "space": "PRE", "mirror": true}`
(6 in `player`, 6 in `skeleton`, 1 in `pig_zombie`). The head has no pair, so reflecting the
pose, turning by `−headYaw` and reflecting back is turning by `headYaw`: the item does what it
would with no `mirror` at all. They were marked only because the items around them are.

So `negate` goes away (*Mirroring*), and those 13 items lose `"mirror": true`, with an identical
pose. The rule's `when` becomes `@when`, and the item bones `renderLeftItemRotation` /
`renderRightItemRotation` in its pairs are renamed (*Smaller renames and cleanups*).

### Built-ins and time

| old | new |
|---|---|
| `elapsed` | `nodeTicksElapsed` |
| a clip's `clipLength` / `duration` names in `frame` | `clipLength`, `clipDuration` |
| `ticks`, `partialTicks`, `ticksPerFrame`, `random` (subject variables) | built-ins, same names and meaning (`ticks` is still the local player's age, as in `DataUpdateHandler`) |
| the layer's clock, readable only through `core:ticks_passed` | `layerTicksElapsed` |
| crossfade progress (not readable) | `nodeFadeProgress` |
| `core:animation_finished` | `nodeIsFinished` (same meaning) |

**`core:ticks_passed`** (`ticksToPass`) held once the layer's clock had moved `ticksToPass` past
the moment what it was written on was entered (`TicksPassedCondition` noted the layer's time in
`onNodeStarted`). It is dropped for the clocks:

| written on | counted from | replacement |
|---|---|---|
| a node's connection or item `when` | the node's entry | `{"ge": ["nodeTicksElapsed", n]}` |
| the layer's selector, connections or `when` | the layer's start | `{"ge": ["layerTicksElapsed", n]}` |
| a machine's selector or connections | the machine's entry | a `machine.` state advanced in its `update` list (no shipped animator needs it) |

Its only shipped uses, the wolf's breathing:

```json
"idle":    {"connections": [{"target": "breathe", "transitionDuration": 4, "triggerCondition": {"type": "core:ticks_passed", "ticksToPass": 80}}]},
"breathe": {"connections": [{"target": "idle",    "transitionDuration": 4, "triggerCondition": {"type": "core:ticks_passed", "ticksToPass": 50}}]}
```

become `{"when": {"ge": ["nodeTicksElapsed", 80]}, "then": "breathe", "transitionDuration": 4}` and
the same with `50` and `"idle"`. The spec also suggested it for
an intro on a layer's selector (`layerTicksElapsed`); tests use it on node connections and items.

**Node re-entry.** The rule that a node never runs twice at once is the old engine's
(`LayerState.beginTransition`): a transition to the current node restarted it without a
crossfade, whatever its duration, and an interrupted crossfade froze a snapshot.

### Entity values from the data classes

Every old subject variable, state and property, with its uses in the shipped animators where
counted.

**Built-ins, renamed:**

| old | new |
|---|---|
| `limbSwing`, `limbSwingAmount`, `swingProgress`, `headYaw`, `headPitch` | `entityLimbSwing`, `entityLimbSwingAmount`, `entitySwingProgress`, `entityHeadYaw`, `entityHeadPitch` |
| `SPRINTING`, `SNEAKING`, `ALIVE`, `CHILD`, `LEFT_HANDED`, `health` | `entityIsSprinting`, `entityIsSneaking`, `entityIsAlive`, `entityIsChild`, `entityIsLeftHanded`, `entityHealth` |
| `ON_GROUND`, `CLIMBING`, `UNDERWATER`, `ledgeHeight`, `climbingRotation` | `entityIsOnGround`, `entityIsClimbing`, `entityIsUnderwater`, `entityLedgeHeight`, `entityClimbingRotation` |
| `DRAWING_BOW`, `RIDING_LIVING` | `entityIsDrawingBow`, `entityIsRidingLiving` |
| `SWINGING` (18), `IN_WATER` (2), `RIDING` (23), `SLEEPING` (2), `ELYTRA_FLYING` (1) | `entityIsSwinging`, `entityIsInWater`, `entityIsRiding`, `entityIsSleeping`, `entityIsElytraFlying` |
| `STANDING_STILL` (65), `STRAFING` (1) | `entityIsStandingStill`, `entityIsStrafing` |
| `motionY` (4), `prevMotionY` (2), `interpolatedMotionY` (16), `motionMagnitude` (16), `xzMotionMagnitude` (3), `forwardMomentum` (5), `sidewaysMomentum` (4) | `entityMotionY`, `entityPrevMotionY`, `entityInterpolatedMotionY`, `entitySpeed`, `entityXZSpeed`, `entityForwardMomentum`, `entitySidewaysMomentum` |
| `bodyYaw`, `worldX/Y/Z` (`core:step_turn`'s defaults) | `entityBodyYaw`, `entityWorldX/Y/Z` |
| `ridingRelativeHeadYaw` (1), `ridingRelativeYaw` (1) | `entityRidingRelativeHeadYaw`, `entityRidingRelativeYaw` |
| `climbingRenderYaw` (1), `climbingBodyYaw` (4), `climbingHeadYaw` (1), `climbingCycle` (1) | `entityClimbingRenderYaw`, `entityClimbingBodyYaw`, `entityClimbingHeadYaw`, `entityClimbingCycle` |
| `ticksExisted`, `ticksInAir`, `ticksAfterTouchdown`, `ticksFalling`, `ticksAfterAttack` | `entityTicksExisted`, `entityTicksInAir`, `entityTicksAfterTouchdown`, `entityTicksFalling`, `entityTicksAfterAttack` |
| `itemUseMaxCount`, `itemUseCount` | `entityItemUseTicks`, `entityItemUseTicksLeft`: named for what they hold, not after vanilla's backwards `getItemInUseMaxCount` (ticks used so far) and `getItemInUseCount` (ticks left) |
| (new) | `entityId`, `entityTicksElytraFlying` |

- `swingProgress` was the mutator's interpolated value (vanilla's `getSwingProgress(partialTicks)`,
  a straight line between the last tick's value and this tick's, wrapping when a new swing
  starts); the old `entitySwingProgress` was vanilla's raw field, used once. The name goes to the
  interpolated value, and the one raw use switches to it (reading the raw field would only keep
  its jitter).
- The old `entityXZSpeed` (3 uses) read vanilla's `motionX`/`motionZ`, which the server doesn't
  send for other entities: they read 0 for every player but the local one. It is dropped; its
  uses (the player's `riding` node, choosing `riding_fast` above 0.01, ported from the old
  `RidingAnimationBit`) move to the measured `entityXZSpeed`, with a new threshold: the measured
  speed includes the mount carrying the rider, so 0.01 would pick `riding_fast` whenever the
  mount moves. (So the old split only ever worked for the local player.)

**Dropped, written out where used:**

| old (uses) | instead |
|---|---|
| `AIRBORNE` (8), `MOVING_HORIZONTALLY` (12) | `{"not": ["entityIsOnGround"]}`, `{"not": ["entityIsStandingStill"]}` |
| `headYawAbs` (4) | `{"abs": ["entityHeadYaw"]}` |
| `aimedBowTicks` (16) | `{"min": ["entityItemUseTicks", 15]}`, as an animator definition |
| the biped's `CAN_SPIN_ATTACK` (always true) | nothing |
| `rotationYaw`, `motionX/Z`, `prevMotionX/Z`, `movementAngle` (0) | nothing until needed |

**Specific to a mob:**

| old (uses) | new |
|---|---|
| `FLYING` (1) | `core:is_flying` |
| properties `mainHandItem`, `offHandItem`, `activeItem`, `mainHandUseAction`, `offHandUseAction`, `useActionType`, `attackActionType`, `activeHand`, `primaryHand`, `activeHandSide` (a hard-coded `switch` in `LivingEntityData` / `BipedEntityData`) | property operations with string or choice arguments. The shipped animators use five: `mainHandItem` / `offHandItem` → `core:holds_item`, `core:holds_any_item`; `activeHandSide` → `core:active_hand_side`; `useActionType` → `mobends:use_action`; `attackActionType` → `mobends:attack_action` (*Values specific to a mob*). The others get an operation when something needs them |
| `core:equipment_name` (a condition) | `core:equipment_name` [slot, pattern] |
| wolf: `SITTING`, `interestedAngle`, `shakeAngleHead` / `Mane` / `Tail`, `tailRotation`, `tailWag` (1) | `mobends:is_sitting`, `mobends:wolf_interested_angle`, `mobends:wolf_shake_angle`, `mobends:wolf_tail_rotation`; `tailWag` a wolf definition |
| spider: `BESIDE_CLIMBABLE`, wall facing, `crawlProgress` (2), `crawlRenderYaw` (1) | `mobends:is_beside_climbable`, `mobends:spider_wall_rotation`; spider state or operations |
| squid: `squidRotation` (2), `SQUID_ROTATION_LOW` (2), `SQUID_PREV_ROTATION_LOW` (2) | `field` reads in the squid's files |
| player: `flightSpeedFactor` (4), `flightPitch` (2), `CAN_SPIN_ATTACK` (4), `SPRINT_JUMP_LEG`, the cape phase | player definitions and state (`flightPitch` is a live definition over the motion built-ins, *Values specific to a mob*); `mobends:spin_attack_enabled` |
| zombie: `animationSet` (2), `currentWalkingState` (1) | zombie state: a constant from `entityId`; a random value on a timer |

**The swing filter.** `LivingEntityData.updateClient` counted swings for every living entity
(once per tick: when `isSwingInProgress` and either not counted yet or more than 5 ticks since,
`onAttack()`), `onAttack` reset `ticksAfterAttack`, and `LivingEntityData.update` advanced it by
`ticksPerFrame` every frame. `PlayerData.onAttack` additionally ignored a swing within 6 ticks of
the last one while the main hand held an item. The built-in keeps the shared counting; the
player's filter moves to its model definition (see *Values specific to a mob*), and `player.json` reads
`entity.ticksAfterAttack` instead of the built-in.

### Properties

The old properties were strings read through a hard-coded `switch` with no registry, and an
unknown one silently matched only `unset`. As operations with string arguments, the language
needs no string type.

### Addon API

`AddonAnimationRegistry.registerTriggerCondition` goes away, and so does
`registerSelectorCondition` (selector operations are flagged operations). Expression operations
had no registry method: addons could only call the global `ExpressionOperations.register`, with no
mod id, types, entity class or state. They get registry methods (*Operations and drivers in
Java*). Drivers keep their Gson template classes, with the typed field set; `IPoseItem`'s
`onNodeStarted` / `advance` and the state in Java fields give way to declared state.

Registration timing is broken today: `Addons.registerAddon` calls `registerContent` only if
`CoreClient` already exists, and nothing calls it later, so an addon registered too early is
silently dropped. The new registry queues registrations and replays them once the core exists,
and rejects a registration made after the first animator has loaded.

## Background: what data classes do

A data class (`EntityData` and its subclasses) is the companion object of one tracked entity
(`EntityDatabase`, by entity id). Surveyed 2026-10-03, it does seven jobs:

1. **Holds the bones and their smoothing.** `initModelPose` creates the `ModelPartTransform`s and
   the entity-level `globalOffset`, `localOffset`, `renderRotation`, `centerRotation`;
   `updateParts` advances their smoothing every frame. KUMO writes targets through `getBone`;
   rendering reads parts through `getPartForName` (mutators, `BindPoint`).
2. **Senses the world once per game tick** (`updateClient`): its own motion from position
   changes, its own ground check with touchdown and liftoff events, climbing, swing detection.
3. **Keeps clocks and memory every frame** (`update`): `ticksInAir`, `ticksAfterTouchdown`,
   `ticksFalling`, `ticksAfterAttack`, `climbingCycle`; the player's `sprintJumpLeg` and cape
   phase; the zombie's random walking style; the spider's crawl and per-leg IK state.
4. **Defines the animator's vocabulary**: `registerVariable`, `registerState`, the `getProperty`
   switch.
5. **Holds state for code drivers**, which cast to it: the cape driver to `PlayerData`, the sword
   trail to `BipedEntityData.swordTrail`, the spider legs to `SpiderData.limbs`.
6. **Owns the animator**: `setAnimator`, `animate()` (called from `Mutator` while rendering),
   `wantsVanilla()`.
7. **Sets up the model, and some behaviour**: the wolf pup's head size every frame; the player's
   6-tick swing filter (`PlayerData.onAttack`).

Where each goes:

| data-class state | in the new design |
|---|---|
| clocks and memory (3) | the same kind as state slots: `entity…` built-ins when shared by every entity, `entity.` state in a mob's model definition otherwise |
| code drivers' state (5) | driver state, declared |
| vocabulary (4) | built-ins and registered operations |
| bones and smoothing (1) | stays per-entity data; the animator's result is copied into the bones' targets each frame, so per-bone floats exist twice |
| render outputs (sword trail, pup head) | stay per-entity data |
| sensing (2) | built-ins computed by Mo' Bends |

The data-class memory differs from slots in scope (an entity's life, read by every layer and
extension) and rate (some per tick, some per frame); hence entity-level state and the `entity…`
built-ins.

## Splitting program from state

### How animators are compiled today

Verified 2026-10-03: **an animator is compiled per entity.** Only the parsed template is shared.

- `AnimatorResources` caches the parsed `AnimatorTemplate` per resource location.
- Every `EntityData` gets its own `KumoAnimatorController` (`EntityData.setAnimator`, called per
  entity by the type's data factory, `EntityType.java:183-186`).
- The controller builds its own `KumoAnimatorState` lazily on the first `animate(subject)`
  (`KumoAnimatorController.ensureLoaded`). That constructor compiles expressions and conditions,
  so every zombie compiles `biped.json` for itself.
- The compiled objects hold that entity's runtime state in their fields (a ramp's value, an edge
  trigger's memory, a node's clock, a layer's current node), which is why they can't be shared.
- Compiling doesn't see the subject: the constructor takes none, and bones are bound on the first
  frame (`Skeleton.bind`). But `ensureLoaded` runs inside `animate(subject)`, so passing the
  subject into compilation is a small change.
- An entity's class can't change under its animator: when its type changes, its data (and so its
  animator) is made anew.

### Measurements

Measured with `misc/bench/InstancingBench.java`: two mock engines running a synthetic animator
shaped like `player.json` (7 layers, 41 nodes, 295 items, 556 condition nodes, 1,043 expression
nodes). Both do the same work per frame and share the expression classes, so only instancing and
the layout of state differ. Java 8 (Minecraft 1.12's runtime), Apple M3 Pro, 2026-10-03.

Instancing, all entities' animators:

| entities | unified (today) | split | speedup |
|---|---|---|---|
| 1 | 0.03 ms | 0.03 ms (the compile, ~28 µs) | 1× |
| 10 | 0.27 ms | 0.04 ms | 7× |
| 100 | 2.7 ms | 0.10 ms | 27× |
| 1,000 | 26.5 ms | 0.55 ms | 48× |
| 10,000 | 639 ms | 16 ms | 40× |

One frame, all entities:

| entities | unified | split | speedup |
|---|---|---|---|
| 1–100 | 8.5 µs per entity | 8.6 µs per entity | 1.0× |
| 1,000 | 9.7 ms | 8.9 ms | 1.09× |
| 10,000 | 108.5 ms | 89.6 ms | 1.21× |

Retained heap for 10,000 animators: 617 MB unified (63 KB per entity), 135 MB split (13.8 KB per
entity, almost all of it the 7 layers × 5 pose buffers both need; the state slots are about
0.3 KB).

The wins:

- **No compile hitches.** Instancing is lazy, on an entity's first frame, so entities appearing
  together (joining a world, loading chunks) compile in the same frame. 100 players cost ~2.7 ms
  of a 16.7 ms frame today; with a cached program, ~0.1 ms. Instancing goes from ~27 µs to ~1 µs
  per entity.
- **About a fifth of the memory.** The unified engine duplicates ~49 KB of compiled tree per
  entity.
- **Runtime unchanged at Minecraft's usual entity counts**, 9–21 % faster from about a thousand
  entities, where the unified trees stop fitting in cache.
- **Cheaper stateful operations.** Per-use memory falls out of slot allocation; named conditions
  today get it by copying the condition at every use.

The mock understates today's instancing cost (no Gson trees, `ExpressionScope` chains, eager
validation or skeleton binding), and its runtime is simpler than KUMO's (names resolved to
indices in both; no previous node evaluated during a crossfade). Read the ratios, not the
absolute times.

Run it with
`javac -d /tmp/bench misc/bench/InstancingBench.java && java -Xmx4g -cp /tmp/bench InstancingBench`
(about 2 minutes). Options: `--shape` prints the generated animator's shape and exits;
`-Dseed=N` generates another animator (40, the default, is the one closest to `player.json`);
`-Dsections=runtime` runs only the runtime measurement; `-Dlookup=string` reads names as today's
runtime does (below).

## A first step in the current runtime: resolve names at compile time

Independent of the format rework and of the split, so it can land first; both build on it.

Today every read of a name happens by string, every frame:

- A variable (`Expression.Variable`) calls `KumoContext.resolveVariable(name)`, which runs
  `has(name)` then `get(name)` on the node's `VariableScope` (each a linear scan comparing
  strings), the same two on the layer's, then `subject.getVariable(name)`: a `HashMap` lookup and
  a supplier call. A mirrored item adds a `HashSet` lookup for the negated names.
- A state (`StateCondition`) calls `subject.getState(name)`: a `HashMap` lookup and a supplier
  call.
- An unknown name is found on the first frame it is read, not at load.

Every name can be resolved once, when the animator is compiled:

- **Node and layer variables are known from the template.** Every writer names them statically:
  ramps, accumulators and springs (`name`), `core:set` (`variable`), `core:step_turn` (`turnLag`,
  `turnSpeed`, `stepLift`, `stepImpact`, `stride`), the spider drivers (`groundLevel`), a layer's
  `variables` and the `set` maps. So `VariableScope` can become a fixed array.
- **Subject names** become indices too: `IKumoSubject` gains a way to look a name up once
  (`indexOfVariable(name)` / `indexOfState(name)`) and read by index; `EntityData` keeps its
  suppliers in arrays.
- **Mirroring**: whether a read is negated is known per item, so it becomes a flag on the compiled
  read.
- Unknown names fail at load.

One behaviour changes: a node variable shadows a layer or subject name today only once it has
been written (`VariableScope.has`), and a written name stays for the node's life. Ramps,
accumulators and springs write on entry, but `core:step_turn`, the spider drivers and `core:set`
write on their first evaluation, so an item before them reads the layer's or the subject's value
of that name. Resolved statically, the read always goes to the node's slot (0 until written).
Nothing shipped is known to rely on the old behaviour; to check against the parity goldens.

Measured with `-Dlookup=string` (the node scope holding two names, the layer one, the subject a
map of 48 variables and 24 states), against reads by index:

| entities | by index | by name (today) | cost of names |
|---|---|---|---|
| 1 | 8.8 µs per entity | 9.1 µs | +4 % |
| 10 | 8.5 µs | 9.9 µs | +17 % |
| 100 | 8.6 µs | 10.2 µs | +18 % |
| 1,000 | 9.6 µs | 10.9 µs | +13 % |
| 10,000 | 10.3 µs | 12.7 µs | +23 % |

(Unified engine, one frame; the split engine shows the same.) The gain is real but modest; the
bigger win is that unknown names fail at load.

## Moving mobs out of Java

Surveyed 2026-10-03. A model definition can already describe the player's mesh: `ModelPlayer`'s
fields are in the generated tables, each skin variant's `RenderPlayer` gets its own mutator, and
`split` makes elbows and knees. What it can't do is everything around the mesh.

Registration:

- **A definition can't take a built-in key.** `TypeFiles` reuses an existing bender with the same
  key (`TypeFiles.java:48-61`), so a definition keyed `player` is silently ignored while
  `PlayerBender` is registered (`DefaultAddon.java:31`).
- **Defined benders are never an entity's default model.** They are registered with
  `registerTypeBender`, not as defaults (`EntityBenderRegistry.java:75-82`), so they get no
  built-in type, and types or extensions without a `model` can't reach them. Needed: a way for a
  definition or type file to be the default for its entity class, under the built-in id.

Data that code casts to:

- `BipedEntityData`: the held-item and armour layers, `ArmorWrapper`, the sword trail driver and
  renderer;
- `PlayerData`: the cape driver and layer, elytra, the supporter accessories layer;
- `SpiderData`: the spider leg drivers; `WolfData`: `LayerWolfMisc`.

Needed: these read bones by name (as `BindPoint` already does) and per-entity parts as components
a definition switches on (sword trail, cape, item orientations `rightHeldItem` /
`leftHeldItem`). With every cast gone, one generic data class serves every entity (*Per-entity
data*).

State and logic (the player's states, counters, sprint-jump leg, cape phase, swing filter and
spin-attack config; the zombie's random walking style; the wolf's method-backed angles) are what
definitions, built-ins and operations cover; the zombie's needs `entityId` and a timer.

Rendering, where definitions fall short:

- **Layers.** `DefinedMutator.swapLayer` does nothing (`DefinedMutator.java:169-172`), so a
  defined mob keeps vanilla's armour, held-item and head layers, which draw their own unanimated
  model copies (the sheep's wool and the charged creeper's armour are the same problem). The
  player also needs the cape, elytra and accessories layers. Needed: a `layers` section mapping
  vanilla layers to animated replacements, and an armour wrapper that finds parts by bone name.
- **Stand-ins for parented bones.** A bone with a `parent` leaves an invisible stand-in in the
  vanilla field (`DefinedMutator.java:265-286`). Vanilla code that goes through the field then
  hits the stand-in: held items are placed at the origin (`postRenderArm`), the first-person hand
  renders nothing, and the skin-part toggles (sleeves, jacket, hat) and armour visibility are
  ignored. Needed: stand-ins that forward visibility, rendering and `postRender` to the real part.
- **Items and armour through split segments.** The Java parts pass the transform on to the
  forearm when an item is attached; defined parts don't. Needed: `postRender` through split
  segments, or a per-bone post-offset.
- **Pivots.** The Java biped pivots the body at the hips and offsets its boxes; a definition's
  `position` moves the pivot and the boxes together. Needed: a pivot that re-bases the boxes.
- **Overlay parts.** The player's sleeve and trouser layers (`bipedLeftArmwear`, ...) must split
  and follow the forearm and foreleg. Needed: split segments attached to another bone's segments.
- **First person.** The arm's rest pose for the first-person hand is Java
  (`PlayerMutator.poseForFirstPersonView`). Needed: a list of bones to rest in first person.
- **Slim skins.** Pivots are adopted once from whichever renderer comes first, but the skin
  variant reads `default` until the skin downloads. Needed: adopt again when the renderer
  changes.
- **Renderer settings.** The sneak offset (4 while flying, 5 otherwise) and the sword trail pass.

Other mobs:

- **Zombies, skeletons:** the biped points above.
- **Spider:** longer legs rebuilt from constants (definitions can't resize geometry), leg IK state
  on `SpiderData`.
- **Squid:** close; needs the comparisons and a section offset.
- **Wolf:** nose, mouth and ears are cut from one part's boxes (needs bones built from selected
  boxes), texture rotations for the body and mane, and extra textured meshes (`LayerWolfMisc`).

The plan for the player:

1. Stand-ins that forward visibility, rendering and `postRender` to the real part, and
   `postRender` through split segments.
2. Pivots that re-base the boxes; overlay segments attached to another bone's segments.
3. A `layers` section mapping vanilla layers to animated ones, with a bone-name armour wrapper
   (also fixes the sheep's wool and the charged creeper's armour).
4. Components (sword trail, cape, item orientations) instead of casts to data classes.
5. Renderer settings (sneak offset, trail) and the first-person rest pose.
6. Default-model registration for definitions under the built-in id; then delete `PlayerBender`.
7. Adopting pivots again when the renderer changes (slim skins).

Zombies, skeletons and the rest follow the same steps, plus their own items above.

## Smaller renames and cleanups

Decided:

- The item bones `renderLeftItemRotation` / `renderRightItemRotation` are renamed
  `leftHeldItem` / `rightHeldItem`.

Proposed, not yet agreed:

- Singular / plural pairs collapse to the list form (`values`, `entityTypes`, `names`, `uuids`).
- The reserved `"default"` key in damping and additive-space maps can't be a bone name. Either
  move it out of the bone map (`{"default": 0.3, "bones": {...}}`), or spell it `@default`
  (`{"@default": 0.3, "body": 0.5}`), extending `@` to mean "a key the format reserves".
- Enum casing (`EASE_IN_OUT`, `PRE`) vs lower-case registry ids (`core:axis_rotate`).

## Format version

Assumed: the format stays `formatVersion: 2`, since v2 hasn't shipped.

## Migration work

Everything besides the engine that changes with the format (surveyed 2026-10-03):

- **`animation-lab/tools/gen_animators.ts`** (run with `./gradlew generateAnimators` in
  `animation-lab`) generates 11 animators: `biped`, `zombie`, `skeleton`, `pig_zombie`, `player`,
  `squid`, `spider`, `zombie_villager`, `quadruped`, `villager`, `chicken`. Its condition helpers
  (`cmp`, `state`, `AND` / `OR` / `NOT`, `prop`, `dec`, `conn`), clip and driver objects and
  selectors all change. It also holds the riding node's speed threshold, which needs a new value.
- **Animators not generated:** `iron_golem.json`, `creeper.json`, `cow.json` (hand-edited, per
  the generator's header), `wolf.json` (not in the generator's list).
- **Example packs** (`misc/examples/`): the `dance`, `zombie_arms`, `vanilla_swim`, `wave`
  animators; the type files with selectors (`bendy_tester.json`); their READMEs.
- **Type files** with selectors: `src/main/resources/assets/mobends/bends/types/*.json`.
- **Model definitions** using `variables[]`: `chicken.json`, `iron_golem.json`.
- **Tests with inline animator JSON:** `core`: `KumoAnimatorStateTest`, `KumoSerializerTest`
  (`TestSubject` parses); `animation-lab`: `ExtensionsTest`, `ClipFrameTest`,
  `AnimationLimitsTest`, `ExpressionTest` (and its fixture `expressions_parent.json`),
  `EntityTypesTest`.
- **Tests on shipped assets:** `KumoParityTest` (against `animation-lab/golden`),
  `SideEffectParityTest`, `SpinAttackTest`, `DefinedModelsTest`, `StepTurnTest`,
  `DanceExtensionTest`.
- **Parity goldens to re-record** (`./gradlew record` in `animation-lab`), where behaviour moves
  on purpose: the eating, shield and sprint-jump scenarios (`linstep`, one frame); the swimming
  `deep` ramp (`readBeforeAdvance` dropped); the riding scenarios (the measured speed); anything
  reading node variables before their writer (*A first step in the current runtime*).
- **Lab bootstrap:** `LabBootstrap` mirrors the mod's registrations (`MinecraftKumoOperations`,
  the four `mobends:` drivers) and changes with the registration API.
- **Addon API:** `AddonAnimationRegistry`, `DefaultAddon`, `MinecraftKumoOperations`.
- **The held-item bones** (`leftHeldItem` / `rightHeldItem`): `BipedEntityData`,
  `LayerCustomHeldItem`, `SwordTrail`, `gen_animators.ts`, the `biped`, `player`, `skeleton` and
  `pig_zombie` animators, and 17 clips under `animations/biped`, `animations/player` and
  `animations/pigzombie`.
- **Docs:** `misc/kumo-format.md` (most sections), `docs/animation.md`, `docs/content.md` (the
  authoring recipes), `docs/overview.md` (extension points), `CHANGELOG.md`.

## Open questions about the migration

- **The riding threshold.** A new value for `riding_fast` on the measured speed, picked by running
  the riding scenario in the lab.
