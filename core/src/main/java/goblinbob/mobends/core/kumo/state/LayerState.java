package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.pose.BoneTarget;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.ArmatureMask;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.util.Tween;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A layer: the outermost machine of its nodes. Each frame it decides its node (see {@link #decide}),
 * then the current node and the nodes still fading out are evaluated and cross-faded, oldest
 * first, and the result is composited onto the animator pose.
 *
 * <p>The nodes fading out form a chain: each fades into the next, the last into the current node,
 * each over its own transition. Every node in it keeps running (its clocks, its update list, its
 * pose), so nothing on screen freezes when a transition interrupts another. A node is never run
 * twice at once, so one the layer goes back into while it still fades out stays in the chain as it
 * was last posed (frozen), and so does a node that starts over. The chain holds at most
 * {@link #MAX_FADING} nodes: past that, what is on screen is frozen into one.
 */
public class LayerState
{

    /** The layer's own machine, the outermost. */
    private final MachineState machine;
    /** Every node of the layer, by index, and every machine, the outermost first. */
    private final List<MachineMember> nodes = new ArrayList<>();
    private final List<MachineState> machines = new ArrayList<>();
    private final ArmatureMask mask;
    private final Skeleton skeleton;
    private final Expression when;
    /** Ticks the layer takes to fade in when its "when" starts holding, and out when it stops. */
    private final float fade;
    /** The layer's definitions and statement lists. */
    private final ScopeLists lists;
    // Sized by allocate(), once every layer of the animator has registered its bones.
    private boolean[] allowed;
    private final Quaternion rotationBeneath = new Quaternion();
    private final Quaternion rotationTemp = new Quaternion();
    private final Vec3f vectorBeneath = new Vec3f();

    /** How many nodes can fade out at once. */
    static final int MAX_FADING = 3;

    // The entity's state (see StateLayout). Ints: the current node's index, how many nodes fade out,
    // whether start() entered the node this frame (which stands for the frame's decision), whether
    // the "when" held on the last update; per machine, whether it was left while one of its nodes
    // still fades out (it is disposed when the last is) and whether its scope exists (from its
    // entry until its exit list runs); and per node fading out, oldest first, its
    // index (-1: frozen) and the easing of the transition out of it (into the next one, or the
    // current node).
    private final int currentSlot, fadingCountSlot, justStartedSlot, enabledSlot, offSlot, pendingExitSlot, aliveSlot, fadingNodeSlot, fadingEasingSlot;
    // Floats: the layer's clock and its weight (how far it is faded in; -1 before its first frame);
    // per node fading out, the progress and duration of the transition out of it.
    private final int elapsedSlot, weightSlot, fadingProgressSlot, fadingDurationSlot;
    // Pose buffers: the current node's pose, each fading node's (as it was last posed), the blend
    // (what is on screen, once the layer is posed), a scratch copy for blending and, for a layer
    // with a "when", the layer faded in or out (-1 for none).
    private final int currentPose, blendPose, scratchPose, weightedPose;
    private final int[] fadingPoses = new int[MAX_FADING];

    public LayerState(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        this.mask = layerTemplate.mask;
        if (mask != null)
        {
            mask.validate();
        }
        this.skeleton = skeleton;
        DefinitionScope definitions = new DefinitionScope(DefinitionScope.Kind.LAYER, "the layer");
        definitions.declare(layerTemplate.define, context.getExpressionScope().isTrusted());
        definitions.declareFunctions(layerTemplate.functions);
        ExpressionScope place = context.getExpressionScope().inside(definitions);
        definitions.compileIn(place);
        context = context.withScope(place);
        this.lists = ScopeLists.compile(definitions, layerTemplate.on, place);
        this.when = Expression.compileCondition(layerTemplate.when, place);
        if (layerTemplate.fade < 0 || Float.isNaN(layerTemplate.fade))
        {
            throw new MalformedKumoTemplateException("A layer's \"@fade\" is a number of ticks, 0 or more.");
        }
        this.fade = layerTemplate.fade;

        Map<String, MachineMember> membersByName = new HashMap<>();
        this.machine = new MachineState(context, skeleton, layerTemplate, layerTemplate, membersByName);
        machine.link(membersByName);
        index(machine);

        StateLayout layout = context.getExpressionScope().getLayout();
        // Where the layer stands until it starts (see start).
        this.currentSlot = layout.ints(1, machine.initialNode().index);
        this.fadingCountSlot = layout.ints(1, 0);
        this.justStartedSlot = layout.ints(1, 0);
        this.enabledSlot = layout.ints(1, 1);
        this.offSlot = layout.ints(1, 0);
        this.pendingExitSlot = layout.ints(machines.size(), 0);
        this.aliveSlot = layout.ints(machines.size(), 0);
        this.fadingNodeSlot = layout.ints(MAX_FADING, -1);
        this.fadingEasingSlot = layout.ints(MAX_FADING, ConnectionTemplate.Easing.EASE_IN_OUT_QUAD.ordinal());
        this.elapsedSlot = layout.floats(1, 0);
        this.weightSlot = layout.floats(1, -1);
        this.fadingProgressSlot = layout.floats(MAX_FADING, 0);
        this.fadingDurationSlot = layout.floats(MAX_FADING, 0);
        this.currentPose = layout.pose();
        this.blendPose = layout.pose();
        this.scratchPose = layout.pose();
        this.weightedPose = when == null ? -1 : layout.pose();
        for (int k = 0; k < MAX_FADING; k++)
        {
            fadingPoses[k] = layout.pose();
        }
    }

    /** Numbers the layer's nodes and machines, and notes the machines around each node. */
    private void index(MachineState machine)
    {
        machine.index = machines.size();
        machines.add(machine);
        for (MachineMember member : machine.members)
        {
            if (member.machine != null)
            {
                index(member.machine);
                continue;
            }
            member.index = nodes.size();
            nodes.add(member);
            List<MachineState> path = new ArrayList<>();
            for (MachineState around = member.parent; around != null; around = around.member == null ? null : around.member.parent)
            {
                path.add(0, around);
            }
            member.path = Collections.unmodifiableList(path);
        }
    }

    /** Sizes the layer's buffers after the skeleton, once it has every bone of the animator. */
    void allocate()
    {
        allowed = new boolean[skeleton.size()];
        for (int i = 0; i < skeleton.size(); i++)
        {
            allowed[i] = mask == null || mask.doesAllow(skeleton.nameOf(i));
        }
    }

    /** The node the layer is in. */
    private MachineMember current(ITriggerConditionContext context)
    {
        return nodes.get(context.getState().ints[currentSlot]);
    }

    /** The {@code k}th node fading out, oldest first, or null where it is frozen. */
    @Nullable
    private MachineMember fading(EntityState state, int k)
    {
        int index = state.ints[fadingNodeSlot + k];
        return index < 0 ? null : nodes.get(index);
    }

    /** Where {@code node} fades out, oldest first, or -1 where it doesn't (or is frozen). */
    private int fadingIndexOf(INodeState node, EntityState state)
    {
        int count = state.ints[fadingCountSlot];
        for (int k = 0; k < count; k++)
        {
            MachineMember member = fading(state, k);
            if (member != null && member.node == node) return k;
        }
        return -1;
    }

    /** The eased progress of the transition out of the {@code k}th node fading out. */
    private float fadeWeight(EntityState state, int k)
    {
        float duration = state.floats[fadingDurationSlot + k];
        return ease(duration <= 0 ? 1F : state.floats[fadingProgressSlot + k] / duration, state.ints[fadingEasingSlot + k]);
    }

    /**
     * Starts the layer: its definitions take their values and its {@code enter} list runs, its
     * "when" starts over, and its machine is entered as a transition into it would enter it (see
     * {@link #enter}), with nothing to crossfade from. This takes the place of the frame's
     * decision.
     */
    public void start(IKumoContext context) throws MalformedKumoTemplateException
    {
        if (when != null)
        {
            when.restart(context);
        }
        enterLayer(context);
    }

    /** Enters the layer: its definitions and {@code enter} list, then its machine, down to a node (see {@link #start}). */
    private void enterLayer(IKumoContext context) throws MalformedKumoTemplateException
    {
        context.getState().floats[elapsedSlot] = 0;
        context.enterNode(current(context).node);
        lists.enter(context);
        moveTo(descend(enter(machine, context), context), context);
        context.enterNode(current(context).node);
        startNode(context);
        context.getState().ints[justStartedSlot] = 1;
    }

    /**
     * Leaves the layer, once its "when" has stopped holding and it has faded out: the nodes fading
     * out, the current node, the machines around it and the layer itself are disposed (their
     * {@code exit} lists run), innermost first.
     */
    private void leaveLayer(IKumoContext context)
    {
        EntityState state = context.getState();
        dropFading(state.ints[fadingCountSlot], context);
        MachineMember current = current(context);
        exit(current, context);
        for (int i = current.path.size() - 1; i >= 0; i--)
        {
            exitMachine(current.path.get(i), context);
        }
        for (MachineState machine : machines)
        {
            if (state.ints[pendingExitSlot + machine.index] != 0) exitMachine(machine, context);
        }
        context.enterNode(current.node);
        lists.exit(context);
    }

    /** Ticks elapsed since the layer started (or, with a "when", last came back). */
    public float getElapsedTicks(ITriggerConditionContext context)
    {
        return context.getState().floats[elapsedSlot];
    }

    /**
     * The linear progress of the crossfade {@code node} is part of: for a node fading out, the
     * transition out of it; for the current node, the transition into it. 0 when it starts, 1 when
     * it ends, and 1 for a node no crossfade involves.
     */
    public float getFadeProgress(INodeState node, ITriggerConditionContext context)
    {
        EntityState state = context.getState();
        int count = state.ints[fadingCountSlot];
        int k = node == current(context).node ? count - 1 : fadingIndexOf(node, state);
        if (k < 0)
        {
            return 1F;
        }
        float duration = state.floats[fadingDurationSlot + k];
        return duration <= 0 ? 1F : Math.min(state.floats[fadingProgressSlot + k] / duration, 1F);
    }

    /** Whether {@code node} is the current node, while the crossfade into it runs. */
    public boolean isFadingIn(INodeState node, ITriggerConditionContext context)
    {
        return node == current(context).node && context.getState().ints[fadingCountSlot] > 0;
    }

    /** Whether {@code node} is the current node, fully in. */
    public boolean isActive(INodeState node, ITriggerConditionContext context)
    {
        return node == current(context).node && context.getState().ints[fadingCountSlot] == 0;
    }

    /** Whether {@code node} is one the layer left, still posed while the crossfade out of it runs. */
    public boolean isFadingOut(INodeState node, ITriggerConditionContext context)
    {
        return node != current(context).node && fadingIndexOf(node, context.getState()) >= 0;
    }

    public INodeState getCurrentNode(ITriggerConditionContext context)
    {
        return current(context).node;
    }

    /** True while the layer is in a {@code core:vanilla} node (and its "when" holds). */
    public boolean wantsVanilla(ITriggerConditionContext context)
    {
        return current(context).node.isVanilla() && context.getState().ints[enabledSlot] != 0;
    }

    /** Evaluates the layer for this frame and composites its output into {@code animatorPose}. */
    public void update(IKumoContext context, float deltaTime, Pose animatorPose) throws MalformedKumoTemplateException
    {
        EntityState state = context.getState();
        boolean starting = state.ints[justStartedSlot] != 0;
        state.ints[justStartedSlot] = 0;
        context.enterNode(current(context).node);

        boolean enabled = when == null || when.test(context);
        state.ints[enabledSlot] = enabled ? 1 : 0;
        float weight = 1F;
        if (when != null)
        {
            // The layer fades in while its "when" holds and out while it doesn't (at once on its
            // first frame, or with no fade). Faded out, it is left, and writes nothing; it starts
            // over when it comes back.
            weight = state.floats[weightSlot];
            if (weight < 0 || fade <= 0) weight = enabled ? 1F : 0F;
            else weight = Math.max(0F, Math.min(1F, weight + (enabled ? deltaTime : -deltaTime) / fade));
            state.floats[weightSlot] = weight;
            if (weight <= 0)
            {
                if (state.ints[offSlot] == 0)
                {
                    leaveLayer(context);
                    state.ints[offSlot] = 1;
                }
                return;
            }
            if (state.ints[offSlot] != 0)
            {
                state.ints[offSlot] = 0;
                enterLayer(context);
                starting = true;
                state.ints[justStartedSlot] = 0;
            }
            context.enterNode(current(context).node);
        }

        // 1. Transitions are decided before posing, so a state change shows up on the frame it happens.
        // On the frame the layer starts, entering it was the decision.
        ITransition fired = starting ? null : decide(context);
        if (fired != null)
        {
            beginTransition(fired, context);
        }
        MachineMember current = current(context);
        INodeState currentNode = current.node;
        int count = state.ints[fadingCountSlot];

        // 2. The scopes' update lists: the layer's, its machines' from the outermost in (every one
        // that exists: around the current node or a node still fading out), the nodes fading out,
        // oldest first, the current node.
        lists.update(context);
        for (MachineState machine : machines)
        {
            if (posedInside(machine, state))
            {
                machine.update(context);
            }
        }
        for (int k = 0; k < count; k++)
        {
            MachineMember member = fading(state, k);
            if (member != null)
            {
                context.enterNode(member.node);
                member.scope.update(context);
            }
        }
        context.enterNode(currentNode);
        current.scope.update(context);

        // 3. Evaluate, on top of the layers below, and crossfade the chain, oldest first.
        Pose currentPose = state.poses[this.currentPose];
        currentPose.setBelow(animatorPose);
        currentPose.clear();
        currentNode.evaluate(context, currentPose);

        Pose result = currentPose;
        if (count > 0)
        {
            for (int k = 0; k < count; k++)
            {
                MachineMember member = fading(state, k);
                if (member != null)
                {
                    Pose posed = state.poses[fadingPoses[k]];
                    posed.setBelow(animatorPose);
                    context.enterNode(member.node);
                    posed.clear();
                    member.node.evaluate(context, posed);
                }
            }
            context.enterNode(currentNode);
            // Blending fills and matches both sides; the poses themselves stay as they were posed
            // (a frozen one is used again next frame).
            Pose blend = state.poses[blendPose];
            Pose scratch = state.poses[scratchPose];
            blend.set(state.poses[fadingPoses[0]]);
            for (int k = 0; k < count; k++)
            {
                scratch.set(k + 1 < count ? state.poses[fadingPoses[k + 1]] : currentPose);
                crossfade(blend, scratch, fadeWeight(state, k), animatorPose);
            }
            result = blend;
        }

        // 4. Composite onto the animator pose, faded in as far as the layer is: a crossfade with
        // nothing (the layers below), whose smoothing and snapping follow the side fading in.
        if (weight < 1F)
        {
            Pose nothing = state.poses[weightedPose];
            Pose layer = state.poses[scratchPose];
            nothing.clear();
            layer.set(result);
            float shown = ease(weight, ConnectionTemplate.Easing.EASE_IN_OUT_QUAD.ordinal());
            if (enabled)
            {
                crossfade(nothing, layer, shown, animatorPose);
                result = nothing;
            }
            else
            {
                crossfade(layer, nothing, 1F - shown, animatorPose);
                result = layer;
            }
        }
        composite(result, animatorPose);

        // 5. Advance clocks; a transition that has ended leaves the nodes before it.
        state.floats[elapsedSlot] += deltaTime;
        currentNode.advance(context, deltaTime);
        int ended = -1;
        for (int k = 0; k < count; k++)
        {
            MachineMember member = fading(state, k);
            if (member != null)
            {
                member.node.advance(context, deltaTime);
            }
            state.floats[fadingProgressSlot + k] += deltaTime;
            if (state.floats[fadingProgressSlot + k] >= state.floats[fadingDurationSlot + k])
            {
                ended = k;
            }
        }
        if (ended >= 0)
        {
            dropFading(ended + 1, context);
        }
    }

    /**
     * What moves the layer this frame, or null if it stays: the selectors of the machines around the
     * node, from the layer's own inwards, the first that leads somewhere else than where the layer is;
     * else the connections of the node, then of the machines around it from the innermost out, the
     * first met. Every condition is evaluated, whatever is chosen (edge triggers such as
     * {@code core:decreased} track the variable they watch).
     */
    private ITransition decide(IKumoContext context) throws MalformedKumoTemplateException
    {
        MachineMember current = current(context);
        List<MachineState> path = current.path;
        ITransition fired = null;
        for (int i = 0; i < path.size(); i++)
        {
            Selector selector = path.get(i).selector;
            if (selector == null)
            {
                continue;
            }
            Selector.Branch branch = selector.choose(context);
            MachineMember here = i + 1 < path.size() ? path.get(i + 1).member : current;
            if (fired == null && branch != null && branch.getTarget() != here)
            {
                fired = branch;
            }
        }
        fired = firstMet(current.connections, fired, context);
        for (int i = path.size() - 1; i >= 0; i--)
        {
            fired = firstMet(path.get(i).connections, fired, context);
        }
        return fired;
    }

    private static ITransition firstMet(List<ConnectionState> connections, ITransition fired, IKumoContext context) throws MalformedKumoTemplateException
    {
        for (ConnectionState connection : connections)
        {
            if (connection.when.test(context) && fired == null)
            {
                fired = connection;
            }
        }
        return fired;
    }

    /**
     * Moves the layer. What the transition disposes of at once goes first (with no crossfade, every
     * node fading out and the node left; with the chain full, the same, frozen into what is on
     * screen; the machines it leaves, innermost first, unless the node left fades out; and the
     * machines it enters again): their {@code exit} lists run. Then the transition's own {@code do}
     * list, then the {@code enter} lists of the scopes it enters, from the outermost in. A node it goes back into
     * that still fades out (or the node it leaves, when it starts over) is frozen as it was last
     * posed, and its {@code exit} list runs, just before it starts again.
     */
    private void beginTransition(ITransition transition, IKumoContext context) throws MalformedKumoTemplateException
    {
        EntityState state = context.getState();
        MachineMember leftMember = current(context);
        List<MachineState> left = leftMember.path;
        float duration = transition.getDuration();
        boolean fades = duration > 0.0F;
        if (!fades)
        {
            dropFading(state.ints[fadingCountSlot], context);
            exit(leftMember, context);
        }
        else if (state.ints[fadingCountSlot] == MAX_FADING)
        {
            // The chain is full: what is on screen (the last blend) is frozen into one, and fades out.
            state.poses[fadingPoses[0]].set(state.poses[blendPose]);
            freeze(0, context);
            dropFading(MAX_FADING, context, 1);
            exit(leftMember, context);
            leftMember = null;
        }
        // The machines the layer leaves, innermost first: disposed at once, or, where the node left
        // fades out, once the last of their nodes has.
        MachineMember named = transition.getTarget();
        List<MachineState> around = around(named);
        boolean leftFadesOut = fades && leftMember != null;
        for (int i = left.size() - 1; i >= 0; i--)
        {
            MachineState machine = left.get(i);
            boolean enteredAgain = named.machine != null && (machine == named.machine || isInside(machine, named.machine));
            if (around.contains(machine) || enteredAgain)
            {
                continue;
            }
            if (leftFadesOut) state.ints[pendingExitSlot + machine.index] = 1;
            else exitMachine(machine, context);
        }
        // The machines the transition enters again (around where it leads but not around the node
        // left, or the one it leads into) are left too, with every machine inside them: a scope
        // is created on every entry, and its exit list runs before it is created again.
        for (MachineState machine : around)
        {
            if (!left.contains(machine)) disposeMachine(machine, context);
        }
        if (named.machine != null)
        {
            disposeMachine(named.machine, context);
        }
        transition.getRun().run(context);
        // Then entered, from the outermost in, down to a node.
        for (MachineState machine : around)
        {
            if (!left.contains(machine)) startMachine(machine, context);
        }
        MachineMember target = descend(named, context);

        if (fades)
        {
            int count = state.ints[fadingCountSlot];
            if (leftMember != null)
            {
                int again = fadingIndexOf(target.node, state);
                if (again >= 0)
                {
                    freeze(again, context);
                }
                count = state.ints[fadingCountSlot];
                state.poses[fadingPoses[count]].set(state.poses[currentPose]);
                state.ints[fadingNodeSlot + count] = leftMember.index;
                state.ints[fadingCountSlot] = ++count;
                if (target == leftMember)
                {
                    // A node is never run twice at once: it fades out as it was last posed, and starts over.
                    freeze(count - 1, context);
                }
            }
            int k = count - 1;
            state.floats[fadingProgressSlot + k] = 0;
            state.floats[fadingDurationSlot + k] = duration;
            state.ints[fadingEasingSlot + k] = transition.getEasing().ordinal();
        }

        moveTo(target, context);
        context.enterNode(target.node);
        startNode(context);
    }

    /** Freezes the {@code k}th node fading out as it was last posed: it is disposed (its {@code exit} list runs). */
    private void freeze(int k, IKumoContext context)
    {
        EntityState state = context.getState();
        MachineMember member = fading(state, k);
        state.ints[fadingNodeSlot + k] = -1;
        if (member != null)
        {
            exit(member, context);
        }
        exitLeftMachines(context);
    }

    /** Drops the first {@code n} nodes fading out (their crossfades have ended, or are cut short); see {@link #dropFading(int, IKumoContext, int)}. */
    private void dropFading(int n, IKumoContext context)
    {
        dropFading(n, context, 0);
    }

    /**
     * Drops the nodes fading out from the {@code from}th up to the {@code n}th (exclusive): the live
     * ones are disposed, and the rest move down in their place, then the machines that were left
     * with none of their nodes posed any more.
     */
    private void dropFading(int n, IKumoContext context, int from)
    {
        EntityState state = context.getState();
        int count = state.ints[fadingCountSlot];
        for (int k = from; k < n; k++)
        {
            MachineMember member = fading(state, k);
            state.ints[fadingNodeSlot + k] = -1;
            if (member != null)
            {
                exit(member, context);
            }
        }
        int removed = n - from;
        for (int k = n; k < count; k++)
        {
            int to = k - removed;
            state.ints[fadingNodeSlot + to] = state.ints[fadingNodeSlot + k];
            state.ints[fadingEasingSlot + to] = state.ints[fadingEasingSlot + k];
            state.floats[fadingProgressSlot + to] = state.floats[fadingProgressSlot + k];
            state.floats[fadingDurationSlot + to] = state.floats[fadingDurationSlot + k];
            state.poses[fadingPoses[to]].set(state.poses[fadingPoses[k]]);
        }
        state.ints[fadingCountSlot] = count - removed;
        exitLeftMachines(context);
    }

    /** Disposes the machines left while one of their nodes faded out, once none of their nodes is posed any more. */
    private void exitLeftMachines(IKumoContext context)
    {
        EntityState state = context.getState();
        // Innermost first, as a layer leaves the machines around its node (machines are indexed
        // outermost first).
        for (int i = machines.size() - 1; i >= 0; i--)
        {
            MachineState machine = machines.get(i);
            if (state.ints[pendingExitSlot + machine.index] != 0 && !posedInside(machine, state))
            {
                exitMachine(machine, context);
            }
        }
    }

    /** Whether the current node or a live node fading out is inside {@code machine}. */
    private boolean posedInside(MachineState machine, EntityState state)
    {
        if (nodes.get(state.ints[currentSlot]).path.contains(machine))
        {
            return true;
        }
        int count = state.ints[fadingCountSlot];
        for (int k = 0; k < count; k++)
        {
            MachineMember member = fading(state, k);
            if (member != null && member.path.contains(machine)) return true;
        }
        return false;
    }

    /** Runs the {@code exit} list of {@code machine}, if its scope exists. */
    private void exitMachine(MachineState machine, IKumoContext context)
    {
        EntityState state = context.getState();
        state.ints[pendingExitSlot + machine.index] = 0;
        if (state.ints[aliveSlot + machine.index] == 0)
        {
            return;
        }
        state.ints[aliveSlot + machine.index] = 0;
        machine.exit(context);
    }

    /**
     * Creates {@code machine}'s scope. One that still exists (a node of it fades out) is disposed
     * first, with every machine inside it (see {@link #disposeMachine}).
     */
    private void startMachine(MachineState machine, IKumoContext context) throws MalformedKumoTemplateException
    {
        disposeMachine(machine, context);
        context.getState().ints[aliveSlot + machine.index] = 1;
        machine.start(context);
    }

    /** Runs the {@code exit} lists of {@code machine}, if its scope exists, and of every machine inside it whose scope does, innermost first. */
    private void disposeMachine(MachineState machine, IKumoContext context)
    {
        if (context.getState().ints[aliveSlot + machine.index] == 0)
        {
            return;
        }
        // Machines are indexed outermost first: the ones inside come after it.
        for (int i = machines.size() - 1; i > machine.index; i--)
        {
            MachineState inner = machines.get(i);
            if (isInside(inner, machine))
            {
                exitMachine(inner, context);
            }
        }
        exitMachine(machine, context);
    }

    /** Whether {@code inner} is a machine somewhere inside {@code outer}. */
    private static boolean isInside(MachineState inner, MachineState outer)
    {
        for (MachineState around = inner.member == null ? null : inner.member.parent; around != null; around = around.member == null ? null : around.member.parent)
        {
            if (around == outer) return true;
        }
        return false;
    }

    /** The machines around {@code member} (a node or a machine), the outermost first. */
    private static List<MachineState> around(MachineMember member)
    {
        if (member.machine == null)
        {
            return member.path;
        }
        List<MachineState> path = new ArrayList<>();
        for (MachineState around = member.parent; around != null; around = around.member == null ? null : around.member.parent)
        {
            path.add(0, around);
        }
        return path;
    }

    /** Runs the {@code exit} list of {@code member}'s node. */
    private void exit(MachineMember member, IKumoContext context)
    {
        context.enterNode(member.node);
        member.scope.exit(context);
        context.enterNode(current(context).node);
    }

    /** Follows {@code target} into the machines it leads into, entering each, down to a node. */
    private MachineMember descend(MachineMember target, IKumoContext context) throws MalformedKumoTemplateException
    {
        while (target.machine != null)
        {
            target = enter(target.machine, context);
        }
        return target;
    }

    /**
     * Enters {@code machine}: the conditions of its selector and connections start over, then its
     * selector chooses where it goes (and the branch's {@code set} applies) or, where it chooses
     * nothing, its defaultOnEntry does.
     */
    private MachineMember enter(MachineState machine, IKumoContext context) throws MalformedKumoTemplateException
    {
        startMachine(machine, context);
        Selector.Branch branch = machine.selector == null ? null : machine.selector.choose(context);
        if (branch == null)
        {
            return machine.defaultOnEntry;
        }
        branch.getRun().run(context);
        return branch.getTarget();
    }

    /** Makes {@code node} the current node. */
    private void moveTo(MachineMember node, ITriggerConditionContext context)
    {
        context.getState().ints[currentSlot] = node.index;
    }

    /**
     * Starts the current node: its clock starts over, its definitions take their values and its
     * {@code enter} list runs, then the node itself starts, and the conditions of its connections
     * start over.
     */
    private void startNode(IKumoContext context) throws MalformedKumoTemplateException
    {
        MachineMember current = current(context);
        // The clock first: the definitions and the enter list see the node as entered.
        current.node.resetClock(context);
        current.scope.enter(context);
        current.node.start(context);
        for (ConnectionState connection : current.connections)
        {
            connection.when.restart(context);
        }
    }

    private static final ConnectionTemplate.Easing[] EASINGS = ConnectionTemplate.Easing.values();

    private static float ease(float t, int easing)
    {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        switch (EASINGS[easing])
        {
            case EASE_IN_QUAD:
                return (float) Tween.easeIn(t, 2.0);
            case EASE_OUT_QUAD:
                return (float) Tween.easeOut(t, 2.0);
            case EASE_IN_OUT_QUAD:
                return (float) Tween.easeInOut(t, 2.0);
            case EASE_IN_CUBIC:
                return (float) Tween.easeIn(t, 3.0);
            case EASE_OUT_CUBIC:
                return (float) Tween.easeOut(t, 3.0);
            case EASE_IN_OUT_CUBIC:
                return (float) Tween.easeInOut(t, 3.0);
            case LINEAR:
            default:
                return t;
        }
    }

    /**
     * Gives {@code missing} what {@code present} poses and it doesn't, as it is beneath the layer: a
     * full rotation, offset, vector or scale as {@code below} (a base pose) has it, a PRE / POST
     * rotation, an additive offset or vector or a relative scale as nothing (identity, zero, one).
     */
    private static void fillFromBelow(Pose missing, Pose present, Pose below)
    {
        for (int i = 0; i < missing.size(); i++)
        {
            BoneTarget m = missing.get(i);
            BoneTarget p = present.get(i);

            if (p.hasRotation && !m.hasRotation)
            {
                // Relative parts of the missing side fold onto what is beneath (see matchSpaces).
                if (!m.hasPre && !m.hasPost)
                {
                    below.absoluteRotation(i, m.rotation);
                    m.hasRotation = true;
                }
            }
            if (p.hasPre && !m.hasPre && !m.hasRotation)
            {
                m.pre.setIdentity();
                m.hasPre = true;
            }
            if (p.hasPost && !m.hasPost && !m.hasRotation)
            {
                m.post.setIdentity();
                m.hasPost = true;
            }
            if (p.hasOffset && !m.hasOffset)
            {
                if (p.offsetAdditive) m.offset.set(0, 0, 0);
                else below.absoluteOffset(i, m.offset);
                m.offsetAdditive = p.offsetAdditive;
                m.hasOffset = true;
            }
            if (p.hasVector && !m.hasVector)
            {
                if (p.vectorAdditive) m.vector.set(0, 0, 0);
                else below.absoluteVector(i, m.vector);
                m.vectorAdditive = p.vectorAdditive;
                m.hasVector = true;
            }
            if (p.hasScale && !m.hasScale)
            {
                if (p.scaleRelative) m.scale.set(1, 1, 1);
                else below.absoluteScale(i, m.scale);
                m.scaleRelative = p.scaleRelative;
                m.hasScale = true;
            }
        }
    }

    /**
     * Where {@code absolute} has an absolute value and {@code relative} only a relative one (a PRE /
     * POST rotation, an additive offset or vector), resolves {@code relative} against what lies
     * beneath the layer, so the two are blended in the same space.
     */
    private void matchSpaces(BoneTarget relative, BoneTarget absolute, Pose below, int slot)
    {
        if (absolute.hasRotation && !relative.hasRotation && (relative.hasPre || relative.hasPost))
        {
            if (!below.absoluteRotation(slot, rotationBeneath))
            {
                rotationBeneath.setIdentity();
            }
            // pre * beneath * post, as composing the relative parts would give.
            if (relative.hasPre)
            {
                Quaternion.mul(relative.pre, rotationBeneath, rotationTemp);
                rotationBeneath.set(rotationTemp);
            }
            if (relative.hasPost)
            {
                Quaternion.mul(rotationBeneath, relative.post, rotationTemp);
                rotationBeneath.set(rotationTemp);
            }
            relative.rotation.set(rotationBeneath);
            relative.hasRotation = true;
            relative.hasPre = false;
            relative.hasPost = false;
        }
        if (absolute.hasOffset && !absolute.offsetAdditive && relative.hasOffset && relative.offsetAdditive)
        {
            if (below.absoluteOffset(slot, vectorBeneath))
            {
                relative.offset.add(vectorBeneath.x, vectorBeneath.y, vectorBeneath.z);
            }
            relative.offsetAdditive = false;
        }
        if (absolute.hasVector && !absolute.vectorAdditive && relative.hasVector && relative.vectorAdditive)
        {
            if (below.absoluteVector(slot, vectorBeneath))
            {
                relative.vector.add(vectorBeneath.x, vectorBeneath.y, vectorBeneath.z);
            }
            relative.vectorAdditive = false;
        }
        if (absolute.hasScale && !absolute.scaleRelative && relative.hasScale && relative.scaleRelative)
        {
            if (below.absoluteScale(slot, vectorBeneath))
            {
                relative.scale.scale(vectorBeneath.x, vectorBeneath.y, vectorBeneath.z);
            }
            relative.scaleRelative = false;
        }
    }

    /**
     * {@code from} becomes the crossfade from it to {@code to} at {@code t}: what one side poses and
     * the other doesn't is filled from beneath the layer, both are brought into the same space, then
     * every channel is interpolated (nlerp for rotations). Both poses are changed.
     */
    private void crossfade(Pose from, Pose to, float t, Pose below)
    {
        fillFromBelow(from, to, below);
        fillFromBelow(to, from, below);
        for (int i = 0; i < from.size(); i++)
        {
            matchSpaces(from.get(i), to.get(i), below, i);
            matchSpaces(to.get(i), from.get(i), below, i);
        }
        for (int i = 0; i < from.size(); i++)
        {
            BoneTarget a = from.get(i);
            BoneTarget b = to.get(i);

            boolean hasRotation = a.hasRotation || b.hasRotation;
            blendQuat(a.hasRotation, a.rotation, b.hasRotation, b.rotation, t, a.rotation);
            blendQuat(a.hasPre, a.pre, b.hasPre, b.pre, t, a.pre);
            blendQuat(a.hasPost, a.post, b.hasPost, b.post, t, a.post);
            a.hasPre = !hasRotation && (a.hasPre || b.hasPre);
            a.hasPost = !hasRotation && (a.hasPost || b.hasPost);
            a.hasRotation = hasRotation;

            lerp(a.hasOffset, a.offset, b.hasOffset, b.offset, t);
            a.offsetAdditive = b.hasOffset ? b.offsetAdditive : a.offsetAdditive;
            a.hasOffset = a.hasOffset || b.hasOffset;
            lerp(a.hasVector, a.vector, b.hasVector, b.vector, t);
            a.vectorAdditive = b.hasVector ? b.vectorAdditive : a.vectorAdditive;
            a.hasVector = a.hasVector || b.hasVector;
            lerp(a.hasScale, a.scale, b.hasScale, b.scale, t);
            a.scaleRelative = b.hasScale ? b.scaleRelative : a.scaleRelative;
            a.hasScale = a.hasScale || b.hasScale;

            // Smoothing, snapping and vector modes follow the node being entered.
            a.smoothing = b.smoothing;
            a.vectorSmoothing.set(b.vectorSmoothing);
            a.offsetSmoothing.set(b.offsetSmoothing);
            a.snap = b.snap;
            a.vectorMode = b.vectorMode;
            a.offsetMode = b.offsetMode;
            a.hasSnapFrom = b.hasSnapFrom;
            a.snapFrom.set(b.snapFrom);
            a.hasVectorStart = b.hasVectorStart;
            a.vectorStart.set(b.vectorStart);
            a.restartX = a.restartY = a.restartZ = false;
        }
    }

    /** {@code a} becomes the interpolation from it to {@code b} at {@code t}; only one side having a value, that side's. */
    private static void lerp(boolean hasA, Vec3f a, boolean hasB, Vec3f b, float t)
    {
        if (hasA && hasB)
        {
            a.set(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
        }
        else if (hasB)
        {
            a.set(b);
        }
    }

    private void blendQuat(boolean hasA, Quaternion a, boolean hasB, Quaternion b, float t, Quaternion dest)
    {
        if (hasA && hasB)
        {
            PoseMath.nlerp(a, b, t, dest);
        }
        else if (hasA)
        {
            dest.set(a);
        }
        else if (hasB)
        {
            dest.set(b);
        }
    }

    private void composite(Pose layerPose, Pose animatorPose)
    {
        for (int i = 0; i < layerPose.size(); i++)
        {
            if (!allowed[i])
            {
                continue;
            }
            BoneTarget src = layerPose.get(i);
            BoneTarget dst = animatorPose.get(i);
            boolean any = src.hasRotation || src.hasPre || src.hasPost || src.hasOffset || src.hasVector || src.hasScale;
            if (!any)
            {
                continue;
            }

            if (src.hasRotation || src.hasPre || src.hasPost)
            {
                animatorPose.composeRotation(i, src);
            }
            if (src.hasOffset)
            {
                animatorPose.composeOffset(i, src.offset.x, src.offset.y, src.offset.z, src.offsetAdditive ? Pose.Space.PRE : Pose.Space.OVERRIDE);
            }
            if (src.hasVector)
            {
                animatorPose.composeVector(i, src.vector.x, src.vector.y, src.vector.z, src.vectorAdditive ? Pose.Space.PRE : Pose.Space.OVERRIDE);
            }
            if (src.hasScale)
            {
                animatorPose.composeScale(i, src.scale.x, src.scale.y, src.scale.z, src.scaleRelative ? Pose.Space.PRE : Pose.Space.OVERRIDE);
            }
            if (src.hasSnapFrom)
            {
                dst.hasSnapFrom = true;
                dst.snapFrom.set(src.snapFrom);
            }
            if (src.hasVectorStart)
            {
                dst.hasVectorStart = true;
                dst.vectorStart.set(src.vectorStart);
            }

            if (!Float.isNaN(src.smoothing)) dst.smoothing = src.smoothing;
            if (!Float.isNaN(src.vectorSmoothing.x)) dst.vectorSmoothing.x = src.vectorSmoothing.x;
            if (!Float.isNaN(src.vectorSmoothing.y)) dst.vectorSmoothing.y = src.vectorSmoothing.y;
            if (!Float.isNaN(src.vectorSmoothing.z)) dst.vectorSmoothing.z = src.vectorSmoothing.z;
            if (!Float.isNaN(src.offsetSmoothing.x)) dst.offsetSmoothing.x = src.offsetSmoothing.x;
            if (!Float.isNaN(src.offsetSmoothing.y)) dst.offsetSmoothing.y = src.offsetSmoothing.y;
            if (!Float.isNaN(src.offsetSmoothing.z)) dst.offsetSmoothing.z = src.offsetSmoothing.z;
            if (src.snap) dst.snap = true;
            if (src.restartX) dst.restartX = true;
            if (src.restartY) dst.restartY = true;
            if (src.restartZ) dst.restartZ = true;
            if (src.hasVector) dst.vectorMode = src.vectorMode;
            if (src.hasOffset) dst.offsetMode = src.offsetMode;
        }
    }

}
