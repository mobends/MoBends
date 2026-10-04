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
 * then the current node (and, during a transition, the previous node or a frozen snapshot of the
 * blend) is evaluated, the two are cross-faded, and the result is composited onto the animator pose
 * in the layer's mode.
 */
public class LayerState
{

    /** The layer's own machine, the outermost. */
    private final MachineState machine;
    /** Every node of the layer, by index, and every machine, the outermost first. */
    private final List<MachineMember> nodes = new ArrayList<>();
    private final List<MachineState> machines = new ArrayList<>();
    private final ArmatureMask mask;
    private final LayerTemplate.LayerMode mode;
    private final Skeleton skeleton;
    private final Expression when;
    /** The layer's definitions and statement lists. */
    private final ScopeLists lists;
    // Sized by allocate(), once every layer of the animator has registered its bones.
    private boolean[] allowed;
    private final Quaternion rotationBeneath = new Quaternion();
    private final Quaternion rotationTemp = new Quaternion();
    private final Vec3f vectorBeneath = new Vec3f();

    // The entity's state (see StateLayout). Ints: the current node's index, the index of the node
    // fading out (-1 for none: the previous node), whether the previous node is a frozen snapshot,
    // the transition's easing, whether start() entered the node this frame (which stands for the
    // frame's decision), whether the "when" held on the last update; then, per machine, whether
    // it was left while its last node still fades out (it is disposed when that node is).
    private final int currentSlot, previousSlot, snapshotSlot, easingSlot, justStartedSlot, enabledSlot, pendingExitSlot;
    // Floats: the transition's progress and duration, the layer's clock.
    private final int progressSlot, durationSlot, elapsedSlot;
    // Pose buffers: the current and the previous node's poses, the frozen snapshot, the blend, and
    // the last output (what a snapshot freezes).
    private final int currentPose, previousPose, snapshotPose, outputPose, lastOutput;

    public LayerState(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate layerTemplate) throws MalformedKumoTemplateException
    {
        this.mask = layerTemplate.mask;
        if (mask != null)
        {
            mask.validate();
        }
        this.mode = layerTemplate.mode == null ? LayerTemplate.LayerMode.OVERRIDE : layerTemplate.mode;
        this.skeleton = skeleton;
        DefinitionScope definitions = new DefinitionScope(DefinitionScope.Kind.LAYER, "the layer");
        definitions.declare(layerTemplate.define, context.getExpressionScope().isTrusted());
        ExpressionScope place = context.getExpressionScope().inside(definitions);
        definitions.compileIn(place);
        context = context.withScope(place);
        this.lists = ScopeLists.compile(definitions, layerTemplate.on, place);
        this.when = Expression.compileCondition(layerTemplate.when, place);

        Map<String, MachineMember> membersByName = new HashMap<>();
        this.machine = new MachineState(context, skeleton, layerTemplate, layerTemplate, membersByName);
        machine.link(membersByName);
        index(machine);

        StateLayout layout = context.getExpressionScope().getLayout();
        // Where the layer stands until it starts (see start).
        this.currentSlot = layout.ints(1, machine.initialNode().index);
        this.previousSlot = layout.ints(1, -1);
        this.snapshotSlot = layout.ints(1, 0);
        this.easingSlot = layout.ints(1, ConnectionTemplate.Easing.EASE_IN_OUT.ordinal());
        this.justStartedSlot = layout.ints(1, 0);
        this.enabledSlot = layout.ints(1, 1);
        this.pendingExitSlot = layout.ints(machines.size(), 0);
        this.progressSlot = layout.floats(1, 0);
        this.durationSlot = layout.floats(1, 0);
        this.elapsedSlot = layout.floats(1, 0);
        this.currentPose = layout.pose();
        this.previousPose = layout.pose();
        this.snapshotPose = layout.pose();
        this.outputPose = layout.pose();
        this.lastOutput = layout.pose();
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

    /** The node fading out (or frozen as a snapshot), or null. */
    @Nullable
    private MachineMember previous(ITriggerConditionContext context)
    {
        int index = context.getState().ints[previousSlot];
        return index < 0 ? null : nodes.get(index);
    }

    /**
     * Starts the layer: its definitions take their values and its {@code enter} list runs, its
     * "when" starts over, and its machine is entered as a transition into it would enter it (see
     * {@link #enter}), with nothing to crossfade from. This takes the place of the frame's
     * decision.
     */
    public void start(IKumoContext context) throws MalformedKumoTemplateException
    {
        context.enterNode(current(context).node);
        lists.enter(context);
        if (when != null)
        {
            when.restart(context);
        }
        List<MachineState> entered = new ArrayList<>();
        moveTo(descend(enter(machine, context, entered), context, entered), context);
        context.enterNode(current(context).node);
        startNode(context);
        context.getState().ints[justStartedSlot] = 1;
    }

    /** Ticks elapsed since the layer started. */
    public float getElapsedTicks(ITriggerConditionContext context)
    {
        return context.getState().floats[elapsedSlot];
    }

    /**
     * The linear progress of the crossfade {@code node} is part of, the same on both sides: 0 when
     * it starts, 1 when it ends, and 1 for a node no crossfade involves.
     */
    public float getFadeProgress(INodeState node, ITriggerConditionContext context)
    {
        MachineMember previous = previous(context);
        if (previous == null || (node != current(context).node && node != previous.node))
        {
            return 1F;
        }
        float[] floats = context.getState().floats;
        float duration = floats[durationSlot];
        return duration <= 0 ? 1F : Math.min(floats[progressSlot] / duration, 1F);
    }

    /** Whether {@code node} is the current node, while the crossfade into it runs. */
    public boolean isFadingIn(INodeState node, ITriggerConditionContext context)
    {
        return node == current(context).node && previous(context) != null;
    }

    /** Whether {@code node} is the current node, fully in. */
    public boolean isActive(INodeState node, ITriggerConditionContext context)
    {
        return node == current(context).node && previous(context) == null;
    }

    /** Whether {@code node} is the node the layer left, still posed while the crossfade runs. */
    public boolean isFadingOut(INodeState node, ITriggerConditionContext context)
    {
        MachineMember previous = previous(context);
        return previous != null && node == previous.node && node != current(context).node && context.getState().ints[snapshotSlot] == 0;
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
        if (!enabled)
        {
            // Disabled: the layer writes nothing and its clocks pause.
            return;
        }

        // 1. Transitions are decided before posing, so a state change shows up on the frame it happens.
        // On the frame the layer starts, entering it was the decision.
        ITransition fired = starting ? null : decide(context);
        if (fired != null)
        {
            beginTransition(fired, context);
        }
        MachineMember current = current(context);
        MachineMember previous = previous(context);
        boolean snapshot = state.ints[snapshotSlot] != 0;
        INodeState currentNode = current.node;
        INodeState previousNode = previous == null ? null : previous.node;

        // 2. The scopes' update lists: the layer's, its machines' from the outermost in, the node
        // fading out, the current node.
        lists.update(context);
        for (MachineState machine : current.path)
        {
            machine.update(context);
        }
        if (previous != null && !snapshot)
        {
            context.enterNode(previousNode);
            previous.scope.update(context);
        }
        context.enterNode(currentNode);
        current.scope.update(context);

        // 3. Evaluate, on top of the layers below.
        Pose currentPose = state.poses[this.currentPose];
        Pose previousPose = state.poses[this.previousPose];
        currentPose.setBelow(animatorPose);
        previousPose.setBelow(animatorPose);
        context.enterNode(currentNode);
        currentPose.clear();
        currentNode.evaluate(context, currentPose);

        Pose result = currentPose;
        if (previousNode != null)
        {
            float duration = state.floats[durationSlot];
            float t = ease(duration <= 0 ? 1F : state.floats[progressSlot] / duration, context);
            Pose source;
            if (snapshot)
            {
                source = state.poses[snapshotPose];
            }
            else
            {
                context.enterNode(previousNode);
                previousPose.clear();
                previousNode.evaluate(context, previousPose);
                source = previousPose;
                context.enterNode(currentNode);
            }
            if (currentNode.isFallthrough() || previousNode.isFallthrough())
            {
                // What one side doesn't pose, the layers below do: fade to (or from) them.
                fillFromBelow(source, currentPose, animatorPose);
                fillFromBelow(currentPose, source, animatorPose);
            }
            for (int i = 0; i < currentPose.size(); i++)
            {
                matchSpaces(source.get(i), currentPose.get(i), animatorPose, i);
                matchSpaces(currentPose.get(i), source.get(i), animatorPose, i);
            }
            Pose output = state.poses[outputPose];
            blend(source, currentPose, t, output);
            result = output;
        }

        // 4. Composite onto the animator pose.
        composite(result, animatorPose);
        state.poses[lastOutput].set(result);

        // 5. Advance clocks.
        state.floats[elapsedSlot] += deltaTime;
        currentNode.advance(context, deltaTime);
        if (previousNode != null)
        {
            if (!snapshot)
            {
                previousNode.advance(context, deltaTime);
            }
            state.floats[progressSlot] += deltaTime;
            if (state.floats[progressSlot] >= state.floats[durationSlot])
            {
                disposePrevious(context);
            }
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
     * Moves the layer. What the transition disposes of at once goes first (a node still fading
     * out, whose crossfade is cut short and frozen, and the node left when nothing fades from it):
     * their {@code exit} lists run. Then the transition's own {@code do} list, then the
     * {@code enter} lists of the scopes it enters, from the outermost in.
     */
    private void beginTransition(ITransition transition, IKumoContext context) throws MalformedKumoTemplateException
    {
        EntityState state = context.getState();
        MachineMember leftMember = current(context);
        List<MachineState> left = leftMember.path;
        INodeState leftNode = leftMember.node;
        boolean interrupting = previous(context) != null;
        float duration = transition.getDuration();
        if (interrupting)
        {
            disposePrevious(context);
        }
        if (interrupting || duration <= 0.0F)
        {
            exit(leftMember, context);
        }
        transition.getRun().run(context);
        List<MachineState> entered = new ArrayList<>();
        MachineMember target = descend(transition.getTarget(), context, entered);

        state.floats[durationSlot] = duration;
        state.ints[easingSlot] = transition.getEasing().ordinal();
        boolean restart = target.node == leftNode;
        if (restart && !interrupting && duration > 0.0F)
        {
            // A node is never run twice at once: it starts over without a crossfade.
            exit(leftMember, context);
        }

        if (duration <= 0.0F || restart)
        {
            state.ints[previousSlot] = -1;
            state.ints[snapshotSlot] = 0;
        }
        else if (interrupting)
        {
            // Interrupting a transition: freeze what is on screen and fade from that (no pop).
            state.poses[snapshotPose].set(state.poses[lastOutput]);
            state.ints[previousSlot] = leftMember.index;
            state.ints[snapshotSlot] = 1;
            state.floats[progressSlot] = 0;
        }
        else
        {
            state.ints[previousSlot] = leftMember.index;
            state.ints[snapshotSlot] = 0;
            state.floats[progressSlot] = 0;
        }

        moveTo(target, context);
        boolean fadingOut = state.ints[previousSlot] >= 0 && state.ints[snapshotSlot] == 0;
        for (MachineState machine : left)
        {
            if (!target.path.contains(machine))
            {
                // Disposed once the last of its nodes has faded out.
                if (fadingOut) state.ints[pendingExitSlot + machine.index] = 1;
                else machine.exit(context);
            }
        }
        context.enterNode(target.node);
        for (MachineState machine : target.path)
        {
            // Those around a node the transition names directly; the entered ones have started.
            if (!left.contains(machine) && !entered.contains(machine))
            {
                machine.start(context);
            }
        }
        startNode(context);
    }

    /** The node fading out is disposed (its crossfade ended, or a transition cut it short), and the machines it left. */
    private void disposePrevious(IKumoContext context)
    {
        EntityState state = context.getState();
        MachineMember previous = previous(context);
        if (previous != null && state.ints[snapshotSlot] == 0)
        {
            exit(previous, context);
        }
        state.ints[previousSlot] = -1;
        state.ints[snapshotSlot] = 0;
        for (MachineState machine : machines)
        {
            if (state.ints[pendingExitSlot + machine.index] != 0)
            {
                state.ints[pendingExitSlot + machine.index] = 0;
                machine.exit(context);
            }
        }
    }

    /** Runs the {@code exit} list of {@code member}'s node. */
    private void exit(MachineMember member, IKumoContext context)
    {
        context.enterNode(member.node);
        member.scope.exit(context);
        context.enterNode(current(context).node);
    }

    /** Follows {@code target} into the machines it leads into, entering each, down to a node. */
    private MachineMember descend(MachineMember target, IKumoContext context, List<MachineState> entered) throws MalformedKumoTemplateException
    {
        while (target.machine != null)
        {
            target = enter(target.machine, context, entered);
        }
        return target;
    }

    /**
     * Enters {@code machine}: the conditions of its selector and connections start over, then its
     * selector chooses where it goes (and the branch's {@code set} applies) or, where it chooses
     * nothing, its defaultOnEntry does.
     */
    private MachineMember enter(MachineState machine, IKumoContext context, List<MachineState> entered) throws MalformedKumoTemplateException
    {
        entered.add(machine);
        machine.start(context);
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
     * Starts the current node: its definitions take their values and its {@code enter} list runs,
     * then the node itself starts, and the conditions of its connections start over.
     */
    private void startNode(IKumoContext context) throws MalformedKumoTemplateException
    {
        MachineMember current = current(context);
        current.scope.enter(context);
        current.node.start(context);
        for (ConnectionState connection : current.connections)
        {
            connection.when.restart(context);
        }
    }

    private static final ConnectionTemplate.Easing[] EASINGS = ConnectionTemplate.Easing.values();

    private float ease(float t, ITriggerConditionContext context)
    {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        switch (EASINGS[context.getState().ints[easingSlot]])
        {
            case EASE_IN:
                return (float) Tween.easeIn(t, 2.0);
            case EASE_OUT:
                return (float) Tween.easeOut(t, 2.0);
            case EASE_IN_OUT:
                return (float) Tween.easeInOut(t, 2.0);
            case EXPONENTIAL:
                return (float) ((1.0 - Math.exp(-4.0 * t)) / (1.0 - Math.exp(-4.0)));
            case LINEAR:
            default:
                return t;
        }
    }

    /**
     * Gives {@code missing} what {@code present} poses and it doesn't, as the layers below have it:
     * a full rotation, offset or vector from {@code below} (when they pose it), a PRE / POST rotation
     * or an additive offset or vector as nothing (identity, zero).
     */
    private static void fillFromBelow(Pose missing, Pose present, Pose below)
    {
        for (int i = 0; i < missing.size(); i++)
        {
            BoneTarget m = missing.get(i);
            BoneTarget p = present.get(i);
            BoneTarget u = below.get(i);

            if (p.hasRotation && !m.hasRotation && u.hasRotation)
            {
                m.rotation.set(u.rotation);
                m.hasRotation = true;
            }
            if (p.hasPre && !m.hasPre)
            {
                m.pre.setIdentity();
                m.hasPre = true;
            }
            if (p.hasPost && !m.hasPost)
            {
                m.post.setIdentity();
                m.hasPost = true;
            }
            if (p.hasOffset && !m.hasOffset && (p.offsetAdditive || u.hasOffset))
            {
                if (p.offsetAdditive) m.offset.set(0, 0, 0);
                else m.offset.set(u.offset);
                m.offsetAdditive = p.offsetAdditive;
                m.hasOffset = true;
            }
            if (p.hasVector && !m.hasVector && (p.vectorAdditive || u.hasVector))
            {
                if (p.vectorAdditive) m.vector.set(0, 0, 0);
                else m.vector.set(u.vector);
                m.vectorAdditive = p.vectorAdditive;
                m.hasVector = true;
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
    }

    /** dest = nlerp(from, to, t); bones present on one side only are taken from that side. */
    private void blend(Pose from, Pose to, float t, Pose dest)
    {
        dest.clear();
        for (int i = 0; i < dest.size(); i++)
        {
            BoneTarget a = from.get(i);
            BoneTarget b = to.get(i);
            BoneTarget d = dest.get(i);

            blendQuat(a.hasRotation, a.rotation, b.hasRotation, b.rotation, t, d.rotation);
            d.hasRotation = a.hasRotation || b.hasRotation;
            blendQuat(a.hasPre, a.pre, b.hasPre, b.pre, t, d.pre);
            d.hasPre = !d.hasRotation && (a.hasPre || b.hasPre);
            blendQuat(a.hasPost, a.post, b.hasPost, b.post, t, d.post);
            d.hasPost = !d.hasRotation && (a.hasPost || b.hasPost);

            if (a.hasOffset && b.hasOffset)
            {
                d.offset.set(a.offset.x + (b.offset.x - a.offset.x) * t, a.offset.y + (b.offset.y - a.offset.y) * t, a.offset.z + (b.offset.z - a.offset.z) * t);
                d.hasOffset = true;
            }
            else if (a.hasOffset || b.hasOffset)
            {
                d.offset.set(a.hasOffset ? a.offset : b.offset);
                d.hasOffset = true;
            }
            d.offsetAdditive = b.hasOffset ? b.offsetAdditive : a.offsetAdditive;

            if (a.hasVector && b.hasVector)
            {
                d.vector.set(a.vector.x + (b.vector.x - a.vector.x) * t, a.vector.y + (b.vector.y - a.vector.y) * t, a.vector.z + (b.vector.z - a.vector.z) * t);
                d.hasVector = true;
            }
            else if (a.hasVector || b.hasVector)
            {
                d.vector.set(a.hasVector ? a.vector : b.vector);
                d.hasVector = true;
            }
            d.vectorAdditive = b.hasVector ? b.vectorAdditive : a.vectorAdditive;

            // Damping, snapping and vector modes follow the node being entered.
            d.smoothness = b.smoothness;
            d.vectorSmoothness.set(b.vectorSmoothness);
            d.snap = b.snap;
            d.vectorMode = b.vectorMode;
            d.hasSnapFrom = b.hasSnapFrom;
            d.snapFrom.set(b.snapFrom);
            d.hasVectorStart = b.hasVectorStart;
            d.vectorStart.set(b.vectorStart);
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
            boolean any = src.hasRotation || src.hasPre || src.hasPost || src.hasOffset || src.hasVector;
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

            if (!Float.isNaN(src.smoothness)) dst.smoothness = src.smoothness;
            if (!Float.isNaN(src.vectorSmoothness.x)) dst.vectorSmoothness.x = src.vectorSmoothness.x;
            if (!Float.isNaN(src.vectorSmoothness.y)) dst.vectorSmoothness.y = src.vectorSmoothness.y;
            if (!Float.isNaN(src.vectorSmoothness.z)) dst.vectorSmoothness.z = src.vectorSmoothness.z;
            if (src.snap) dst.snap = true;
            if (src.restartX) dst.restartX = true;
            if (src.restartY) dst.restartY = true;
            if (src.restartZ) dst.restartZ = true;
            if (src.hasVector) dst.vectorMode = src.vectorMode;
        }
    }

}
