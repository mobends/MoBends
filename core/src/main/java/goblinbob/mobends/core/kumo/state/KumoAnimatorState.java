package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.IRotationSink;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.pose.BoneTarget;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.EntityTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.NodeTemplate;
import goblinbob.mobends.core.kumo.state.template.PoseNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.ClipItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An animator animating one entity: a {@link KumoProgram}, shared by every entity it was compiled
 * for, and this entity's state. Each update evaluates every layer into one pose and writes the
 * pose's targets to the subject's bones; the bones' own smoothing then does the damping.
 */
public class KumoAnimatorState
{

    private final KumoProgram program;
    private final KumoContext context = new KumoContext();
    /** This entity's state of the program. */
    private final EntityState entityState;
    private boolean started = false;
    @Nullable
    private AnimationLimits limits;
    /** Per slot: the offset (or vector) the trusted layers last gave it. */
    private Vec3f[] lastTrusted;

    /** An entity animated by {@code program}. */
    public KumoAnimatorState(KumoProgram program)
    {
        this.program = program;
        this.entityState = program.layout.newState(program.skeleton);
        context.setState(entityState);
    }

    public KumoAnimatorState(AnimatorTemplate animatorTemplate, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(animatorTemplate, Collections.<AnimatorTemplate>emptyList(), dataProvider);
    }

    /** Compiles the animator for this entity alone (see {@link KumoProgram#KumoProgram}). */
    public KumoAnimatorState(AnimatorTemplate animatorTemplate, List<AnimatorTemplate> overlays, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(animatorTemplate, true, overlays, Collections.nCopies(overlays.size(), true), dataProvider);
    }

    /** Compiles the animator for this entity alone (see {@link KumoProgram#KumoProgram}). */
    public KumoAnimatorState(AnimatorTemplate animatorTemplate, boolean trusted, List<AnimatorTemplate> overlays, List<Boolean> overlaysTrusted,
                             IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(null, animatorTemplate, trusted, overlays, overlaysTrusted, dataProvider);
    }

    /** Compiles the animator for this entity alone (see {@link KumoProgram#KumoProgram}). */
    public KumoAnimatorState(@Nullable EntityTemplate entity, AnimatorTemplate animatorTemplate, boolean trusted, List<AnimatorTemplate> overlays,
                             List<Boolean> overlaysTrusted, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(new KumoProgram(entity, animatorTemplate, trusted, overlays, overlaysTrusted, dataProvider));
    }

    public KumoProgram getProgram()
    {
        return program;
    }

    /**
     * The extensions that failed to load and were left out, by their index among the overlays,
     * with what went wrong.
     */
    public Map<Integer, MalformedKumoTemplateException> getSkippedExtensions()
    {
        return program.getSkippedExtensions();
    }

    /**
     * Limits what the untrusted layers can do to the pose (null: no limits): see
     * {@link AnimationLimits}. The trusted layers' result, taken just before the first untrusted
     * layer, is what the limits are measured from.
     */
    public void setLimits(@Nullable AnimationLimits limits)
    {
        this.limits = limits;
    }

    /** Whether any layer comes from an untrusted source. */
    public boolean hasUntrustedLayers()
    {
        return program.anyUntrusted;
    }

    /**
     * Animates {@code subject} for a frame.
     *
     * @throws MalformedKumoTemplateException when the animator fails, e.g. it reads a variable
     *                                        the subject doesn't have; it can't be used after that.
     */
    public void update(IKumoSubject subject, float deltaTime) throws MalformedKumoTemplateException
    {
        try
        {
            animate(subject, deltaTime);
        }
        catch (RuntimeException e)
        {
            throw new MalformedKumoTemplateException("The animator failed: " + e.getMessage(), e);
        }
    }

    private void animate(IKumoSubject subject, float deltaTime) throws MalformedKumoTemplateException
    {
        bind(subject);
        context.beginFrame(subject, deltaTime);
        Pose pose = entityState.poses[program.pose];
        Pose trustedPose = entityState.poses[program.trustedPose];
        pose.clear();

        boolean limiting = limits != null && program.anyUntrusted;
        if (limiting && lastTrusted == null)
        {
            startLimiting(pose);
        }
        else if (!limiting && lastTrusted != null)
        {
            // Limits lifted: next time they apply, they start again from the bones as they are.
            lastTrusted = null;
            pose.setFallbackValues(null);
        }
        // The entity's scope, then the animators': created on the first frame, then their update
        // lists every frame, before any layer.
        context.enterNode(null);
        context.setLayerState(null);
        if (program.entityScope != null)
        {
            if (started) program.entityScope.updateLive(context);
            else program.entityScope.start(context);
            if (started) program.entityLists.runUpdate(context);
            else program.entityLists.runEnter(context);
        }
        for (int i = 0; i < program.animatorScopes.size(); i++)
        {
            if (started) program.animatorScopes.get(i).updateLive(context);
            else program.animatorScopes.get(i).start(context);
            for (ScopeLists lists : program.animatorLists.get(i))
            {
                if (started) lists.runUpdate(context);
                else lists.runEnter(context);
            }
        }

        boolean measured = false;
        for (int i = 0; i < program.layerStates.size(); i++)
        {
            LayerState layer = program.layerStates.get(i);
            if (limiting && !measured && !program.layerTrusted[i])
            {
                trustedPose.set(pose);
                measured = true;
            }
            context.setLayerState(layer);

            if (!started)
            {
                layer.start(context);
            }

            layer.update(context, deltaTime, pose);
        }

        started = true;
        if (limiting)
        {
            limit(pose, trustedPose, limits);
        }
        pose.writeTo();
    }

    /** Binds the entity state to {@code subject}, the first time it is animated: its bones, and where it keeps the values the animator reads. */
    private void bind(IKumoSubject subject) throws MalformedKumoTemplateException
    {
        if (entityState.boundTo == subject && entityState.sinks.length == program.skeleton.size())
        {
            return;
        }
        program.variables.bind(subject, entityState);
        entityState.sinks = program.skeleton.sinksOf(subject);
        entityState.boundTo = subject;
    }

    /**
     * Keeps every offset within the limits of where the trusted layers put it. A bone they don't
     * write this frame keeps what they gave it last (the rest position if they never did): not its
     * live value, which untrusted layers could otherwise push a little further every frame.
     */
    private void limit(Pose pose, Pose trusted, AnimationLimits limits)
    {
        for (int i = 0; i < pose.size(); i++)
        {
            BoneTarget reference = trusted.get(i);
            Vec3f last = lastTrusted[program.limitGroup[i]];
            if (program.vectorSlot[i])
            {
                if (reference.hasVector) setWritten(last, reference.vector);
            }
            else if (reference.hasOffset)
            {
                setWritten(last, reference.offset);
            }
        }
        for (int i = 0; i < pose.size(); i++)
        {
            BoneTarget target = pose.get(i);
            Vec3f last = lastTrusted[program.limitGroup[i]];
            if (program.vectorSlot[i])
            {
                if (target.hasVector) clampAround(target.vector, last, limits.maxBodyOffset);
            }
            else if (target.hasOffset)
            {
                // An offset has no "not written" axis: a NaN one would make the part disappear.
                if (Float.isNaN(target.offset.x)) target.offset.x = last.x;
                if (Float.isNaN(target.offset.y)) target.offset.y = last.y;
                if (Float.isNaN(target.offset.z)) target.offset.z = last.z;
                clampAround(target.offset, last, limits.maxPartOffset);
            }
            // Rotations are free, but a broken one (NaN, infinite) would make the part disappear: dropped.
            if (target.hasRotation && !isFinite(target.rotation)) target.hasRotation = false;
            if (target.hasPre && !isFinite(target.pre)) target.hasPre = false;
            if (target.hasPost && !isFinite(target.post)) target.hasPost = false;
        }
    }

    private static boolean isFinite(Quaternion q)
    {
        return Float.isFinite(q.x) && Float.isFinite(q.y) && Float.isFinite(q.z) && Float.isFinite(q.w);
    }

    /**
     * Starts measuring from the bones' current offsets. From now on the layers' relative offsets
     * and vectors (with nothing under them) build on what the trusted layers gave the bone last,
     * not on its live value, which the untrusted layers have pushed: otherwise that push would
     * carry into the trusted value and grow a little every frame.
     */
    private void startLimiting(Pose pose)
    {
        lastTrusted = new Vec3f[pose.size()];
        Vec3f[] fallback = new Vec3f[pose.size()];
        for (int i = 0; i < lastTrusted.length; i++)
        {
            lastTrusted[i] = new Vec3f();
        }
        for (int i = 0; i < lastTrusted.length; i++)
        {
            Vec3f last = lastTrusted[program.limitGroup[i]];
            fallback[i] = last;
            IBoneSink sink = i < entityState.sinks.length ? entityState.sinks[i] : null;
            if (sink == null || program.limitGroup[i] != i) continue;
            IVectorSink vector = sink.asVector();
            IRotationSink rotation = sink.asRotation();
            if (vector != null) last.set(vector.getVectorTarget());
            else if (rotation != null && rotation.hasOffset()) last.set(rotation.getOffset());
        }
        pose.setFallbackValues(fallback);
    }

    /** Copies the axes {@code value} writes: a NaN axis means "not written" (the bone keeps it). */
    private static void setWritten(Vec3f dest, Vec3f value)
    {
        if (!Float.isNaN(value.x)) dest.x = value.x;
        if (!Float.isNaN(value.y)) dest.y = value.y;
        if (!Float.isNaN(value.z)) dest.z = value.z;
    }

    /**
     * Moves {@code value} back within {@code max} of {@code center}, measuring only the axes it
     * writes (NaN ones are left alone: they don't move the bone). An infinite value goes to the
     * center.
     */
    private static void clampAround(Vec3f value, Vec3f center, float max)
    {
        float dx = Float.isNaN(value.x) ? 0 : value.x - center.x;
        float dy = Float.isNaN(value.y) ? 0 : value.y - center.y;
        float dz = Float.isNaN(value.z) ? 0 : value.z - center.z;
        double length = Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
        if (length <= max)
        {
            return;
        }
        float k = Double.isInfinite(length) ? 0 : (float) (max / length);
        if (!Float.isNaN(value.x)) value.x = Float.isInfinite(dx) ? center.x : center.x + dx * k;
        if (!Float.isNaN(value.y)) value.y = Float.isInfinite(dy) ? center.y : center.y + dy * k;
        if (!Float.isNaN(value.z)) value.z = Float.isInfinite(dz) ? center.z : center.z + dz * k;
    }

    /** The name of every layer's current node, in layer order (for tests and debugging). */
    public List<String> getCurrentNodes()
    {
        List<String> nodes = new ArrayList<>();
        for (LayerState layer : program.layerStates)
        {
            nodes.add(layer.getCurrentNode(context).getName());
        }
        return nodes;
    }

    /** True while any layer is in a {@code core:vanilla} node: the entity should be drawn vanilla. */
    public boolean wantsVanilla()
    {
        for (LayerState layer : program.layerStates)
        {
            if (layer.wantsVanilla(context)) return true;
        }
        return false;
    }

    /** The context the animator evaluates in, with this entity's state: for reading the layers' (see {@link #getLayers}). */
    public KumoContext getContext()
    {
        return context;
    }

    public List<LayerState> getLayers()
    {
        return program.layerStates;
    }

    public Skeleton getSkeleton()
    {
        return program.skeleton;
    }

}
