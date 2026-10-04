package goblinbob.mobends.core.kumo.state;

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
    /** The machines around the current node, from the layer's own inwards. */
    private final List<MachineState> path = new ArrayList<>();
    private final ArmatureMask mask;
    private final LayerTemplate.LayerMode mode;
    private final Skeleton skeleton;
    private final Expression when;
    /** The layer's definitions and statement lists. */
    private final ScopeLists lists;
    /** Machines the layer left whose last node is still fading out: they are disposed when it is. */
    private final List<MachineState> pendingExits = new ArrayList<>();

    // Sized by allocate(), once every layer of the animator has registered its bones.
    private boolean[] allowed;
    private Pose currentPose;
    private Pose previousPose;
    private Pose snapshotPose;
    private Pose outputPose;
    private Pose lastOutput;
    private final Quaternion rotationBeneath = new Quaternion();
    private final Quaternion rotationTemp = new Quaternion();
    private final Vec3f vectorBeneath = new Vec3f();

    private INodeState previousNode;
    /** The member of {@link #previousNode}, whose scope it is. */
    private MachineMember previous;
    private MachineMember current;
    private INodeState currentNode;
    private boolean previousIsSnapshot;
    private float transitionProgress = 0.0F;
    private float transitionDuration = 0.0F;
    private ConnectionTemplate.Easing transitionEasing = ConnectionTemplate.Easing.EASE_IN_OUT;
    private float elapsedTicks = 0.0F;
    /** Whether the layer's "when" held on the last update. */
    private boolean enabled = true;
    /** Whether {@link #start} entered the node this frame, which stands for the frame's decision. */
    private boolean justStarted;

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
        // Where the layer stands until it starts (see start).
        moveTo(machine.initialNode());
    }

    /** Sizes the layer's buffers after the skeleton, once it has every bone of the animator. */
    void allocate()
    {
        currentPose = new Pose(skeleton);
        previousPose = new Pose(skeleton);
        snapshotPose = new Pose(skeleton);
        outputPose = new Pose(skeleton);
        lastOutput = new Pose(skeleton);

        allowed = new boolean[skeleton.size()];
        for (int i = 0; i < skeleton.size(); i++)
        {
            allowed[i] = mask == null || mask.doesAllow(skeleton.nameOf(i));
        }
    }

    /**
     * Starts the layer: its definitions take their values and its {@code enter} list runs, its
     * "when" starts over, and its machine is entered as a transition into it would enter it (see
     * {@link #enter}), with nothing to crossfade from. This takes the place of the frame's
     * decision.
     */
    public void start(IKumoContext context) throws MalformedKumoTemplateException
    {
        context.enterNode(currentNode);
        lists.enter(context);
        if (when != null)
        {
            when.restart(context);
        }
        List<MachineState> entered = new ArrayList<>();
        moveTo(descend(enter(machine, context, entered), context, entered));
        context.enterNode(currentNode);
        startNode(context);
        justStarted = true;
    }

    /** Ticks elapsed since the layer started. */
    public float getElapsedTicks()
    {
        return elapsedTicks;
    }

    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    /** True while the layer is in a {@code core:vanilla} node (and its "when" holds). */
    public boolean wantsVanilla()
    {
        return currentNode.isVanilla() && enabled;
    }

    /** Evaluates the layer for this frame and composites its output into {@code animatorPose}. */
    public void update(IKumoContext context, float deltaTime, Pose animatorPose) throws MalformedKumoTemplateException
    {
        boolean starting = justStarted;
        justStarted = false;
        context.enterNode(currentNode);

        enabled = when == null || when.test(context);
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

        // 2. The scopes' update lists: the layer's, its machines' from the outermost in, the node
        // fading out, the current node.
        lists.update(context);
        for (MachineState machine : path)
        {
            machine.update(context);
        }
        if (previous != null && !previousIsSnapshot)
        {
            context.enterNode(previousNode);
            previous.scope.update(context);
        }
        context.enterNode(currentNode);
        current.scope.update(context);

        // 3. Evaluate, on top of the layers below.
        currentPose.setBelow(animatorPose);
        previousPose.setBelow(animatorPose);
        context.enterNode(currentNode);
        currentPose.clear();
        currentNode.evaluate(context, currentPose);

        Pose result = currentPose;
        if (previousNode != null)
        {
            float t = ease(transitionDuration <= 0 ? 1F : transitionProgress / transitionDuration);
            Pose source;
            if (previousIsSnapshot)
            {
                source = snapshotPose;
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
            blend(source, currentPose, t, outputPose);
            result = outputPose;
        }

        // 4. Composite onto the animator pose.
        composite(result, animatorPose);
        lastOutput.set(result);

        // 5. Advance clocks.
        elapsedTicks += deltaTime;
        currentNode.advance(context, deltaTime);
        if (previousNode != null)
        {
            if (!previousIsSnapshot)
            {
                previousNode.advance(context, deltaTime);
            }
            transitionProgress += deltaTime;
            if (transitionProgress >= transitionDuration)
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
        List<MachineState> left = new ArrayList<>(path);
        MachineMember leftMember = current;
        INodeState leftNode = currentNode;
        boolean interrupting = previousNode != null;
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

        transitionDuration = duration;
        transitionEasing = transition.getEasing();
        boolean restart = target.node == leftNode;
        if (restart && !interrupting && duration > 0.0F)
        {
            // A node is never run twice at once: it starts over without a crossfade.
            exit(leftMember, context);
        }

        if (duration <= 0.0F || restart)
        {
            previousNode = null;
            previous = null;
            previousIsSnapshot = false;
        }
        else if (interrupting)
        {
            // Interrupting a transition: freeze what is on screen and fade from that (no pop).
            snapshotPose.set(lastOutput);
            previousNode = leftNode;
            previous = leftMember;
            previousIsSnapshot = true;
            transitionProgress = 0;
        }
        else
        {
            previousNode = leftNode;
            previous = leftMember;
            previousIsSnapshot = false;
            transitionProgress = 0;
        }

        moveTo(target);
        for (MachineState machine : left)
        {
            if (!path.contains(machine))
            {
                // Disposed once the last of its nodes has faded out.
                if (previous != null && !previousIsSnapshot) pendingExits.add(machine);
                else machine.exit(context);
            }
        }
        context.enterNode(currentNode);
        for (MachineState machine : path)
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
        if (previous != null && !previousIsSnapshot)
        {
            exit(previous, context);
        }
        previousNode = null;
        previous = null;
        previousIsSnapshot = false;
        for (MachineState machine : pendingExits)
        {
            machine.exit(context);
        }
        pendingExits.clear();
    }

    /** Runs the {@code exit} list of {@code member}'s node. */
    private void exit(MachineMember member, IKumoContext context)
    {
        context.enterNode(member.node);
        member.scope.exit(context);
        context.enterNode(currentNode);
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

    /** Makes {@code node} the current node, with the machines around it as the path. */
    private void moveTo(MachineMember node)
    {
        current = node;
        currentNode = node.node;
        path.clear();
        for (MachineState machine = node.parent; machine != null; machine = machine.member == null ? null : machine.member.parent)
        {
            path.add(0, machine);
        }
    }

    /**
     * Starts the current node: its definitions take their values and its {@code enter} list runs,
     * then the node itself starts, and the conditions of its connections start over.
     */
    private void startNode(IKumoContext context) throws MalformedKumoTemplateException
    {
        current.scope.enter(context);
        currentNode.start(context);
        for (ConnectionState connection : current.connections)
        {
            connection.when.restart(context);
        }
    }

    private float ease(float t)
    {
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        switch (transitionEasing)
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
