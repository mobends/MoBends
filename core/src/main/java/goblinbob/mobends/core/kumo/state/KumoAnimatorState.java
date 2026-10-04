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
import java.util.List;
import java.util.Map;

/**
 * A running instance of an animator template for one subject. Each update evaluates every
 * layer into one pose and writes the pose's targets to the subject's bones; the bones' own
 * smoothing then does the damping.
 */
public class KumoAnimatorState
{

    private final List<LayerState> layerStates = new ArrayList<>();
    private final Skeleton skeleton = new Skeleton();
    private final KumoContext context = new KumoContext();
    private final VariableTable variables = new VariableTable();
    /** The animator's scope and each extension's: its definitions and statement lists, one per file. */
    /** The entity's scope, which its model definition declares; null if it declares none. */
    @Nullable
    private final DefinitionScope entityScope;
    @Nullable
    private final ScopeLists entityLists;
    private final List<DefinitionScope> animatorScopes = new ArrayList<>();
    private final List<List<ScopeLists>> animatorLists = new ArrayList<>();
    private final Pose pose;
    private boolean started = false;
    private final boolean[] layerTrusted;
    private boolean anyUntrusted;
    @Nullable
    private AnimationLimits limits;
    private Pose trustedPose;
    /** Per slot: the offset (or vector) the trusted layers last gave it. */
    private Vec3f[] lastTrusted;
    /** Per slot: the slot whose trusted value it is limited around (aliases share one). */
    private final int[] limitGroup;
    /** Per slot: whether it is an entity-level vector (see {@link Skeleton#isVectorBone}). */
    private final boolean[] vectorSlot;

    public KumoAnimatorState(AnimatorTemplate animatorTemplate, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(animatorTemplate, Collections.<AnimatorTemplate>emptyList(), dataProvider);
    }

    /**
     * @param overlays Animators whose layers go on top of the animator's, in order (extensions).
     *                 Each is its own animator: it sees its own named expressions, not the base's.
     */
    public KumoAnimatorState(AnimatorTemplate animatorTemplate, List<AnimatorTemplate> overlays, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(animatorTemplate, true, overlays, Collections.nCopies(overlays.size(), true), dataProvider);
    }

    /**
     * @param trusted         Whether the animator's own file comes from a trusted source (the mod
     *                        or another mod; see {@link IKumoInstancingContext#isTrusted}).
     * @param overlaysTrusted The same for each overlay. A layer is trusted when the file declaring
     *                        it is and every clip it plays is; the others are limited by
     *                        {@link #setLimits}.
     */
    public KumoAnimatorState(AnimatorTemplate animatorTemplate, boolean trusted, List<AnimatorTemplate> overlays, List<Boolean> overlaysTrusted,
                             IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        this(null, animatorTemplate, trusted, overlays, overlaysTrusted, dataProvider);
    }

    /**
     * @param entity The entity animated: its class, which operations bind against, and the entity
     *               scope its model definition declares (read as {@code entity.x} by the animator
     *               and every extension). Null: unknown, with no entity scope.
     */
    public KumoAnimatorState(@Nullable EntityTemplate entity, AnimatorTemplate animatorTemplate, boolean trusted, List<AnimatorTemplate> overlays,
                             List<Boolean> overlaysTrusted, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        // Every scope of the animator, its extensions included, shares one table of the entity's values.
        ExpressionScope root = ExpressionScope.root(variables).forEntity(entity == null ? null : entity.entityClass);
        if (entity != null)
        {
            entityScope = new DefinitionScope(DefinitionScope.Kind.ENTITY, "the model definition");
            entityScope.declare(entity.define, entity.trusted);
            // The only place that reads the entity's fields.
            ExpressionScope place = root.inside(entityScope).trusted(entity.trusted).readingFieldsOf(entity.entityClass);
            entityScope.compileIn(place);
            entityLists = ScopeLists.compile(entityScope, entity.on, place);
            root = root.inside(entityScope);
        }
        else
        {
            entityScope = null;
            entityLists = null;
        }
        dataProvider = new ScopedInstancingContext(dataProvider, root);
        List<LayerTemplate> layers = new ArrayList<>();
        List<IKumoInstancingContext> layerContexts = new ArrayList<>();
        List<Boolean> layersTrusted = new ArrayList<>();
        addAnimator(animatorTemplate, trusted, dataProvider, layers, layerContexts, layersTrusted);
        for (int i = 0; i < overlays.size(); i++)
        {
            // An extension's animator is a scope of its own: it never sees the extended animator's names.
            addAnimator(overlays.get(i), overlaysTrusted.get(i), dataProvider, layers, layerContexts, layersTrusted);
        }
        this.layerTrusted = new boolean[layers.size()];
        for (int i = 0; i < layers.size(); i++)
        {
            layerTrusted[i] = layersTrusted.get(i) && playsTrustedClips(layers.get(i), dataProvider);
            anyUntrusted |= !layerTrusted[i];
        }
        if (layers.isEmpty())
        {
            throw new MalformedKumoTemplateException("No layers were specified");
        }
        for (int i = 0; i < layers.size(); i++)
        {
            layerStates.add(new LayerState(layerContexts.get(i), skeleton, layers.get(i)));
        }

        // Every bone name is known once the layers are instanced.
        for (LayerState layer : layerStates)
        {
            layer.allocate();
        }
        pose = new Pose(skeleton, true);
        trustedPose = new Pose(skeleton);
        // "root" and "globalOffset" are two names of the same vector: limited as one.
        limitGroup = new int[skeleton.size()];
        vectorSlot = new boolean[skeleton.size()];
        Map<String, Integer> groups = new HashMap<>();
        for (int i = 0; i < limitGroup.length; i++)
        {
            String name = skeleton.nameOf(i);
            String key = Skeleton.ROOT.equals(name) ? Skeleton.GLOBAL_OFFSET : name;
            Integer group = groups.putIfAbsent(key, i);
            limitGroup[i] = group == null ? i : group;
            vectorSlot[i] = Skeleton.isVectorBone(name);
        }
    }

    /**
     * Adds an animator (the main one or an extension): one scope for it and the animators it
     * {@code extends}, whose definitions and statement lists come first, and its layers after
     * theirs, each in the place of its own file (trusted or not).
     */
    private void addAnimator(AnimatorTemplate template, boolean trusted, IKumoInstancingContext context,
                             List<LayerTemplate> layers, List<IKumoInstancingContext> contexts, List<Boolean> layersTrusted) throws MalformedKumoTemplateException
    {
        List<AnimatorTemplate> chain = new ArrayList<>();
        List<Boolean> chainTrusted = new ArrayList<>();
        collectChain(template, trusted, context, 0, chain, chainTrusted);

        DefinitionScope scope = new DefinitionScope(DefinitionScope.Kind.ANIMATOR, "the animator");
        for (int i = 0; i < chain.size(); i++)
        {
            scope.declare(chain.get(i).define, chainTrusted.get(i));
        }
        ExpressionScope place = context.getExpressionScope().inside(scope);
        scope.compileIn(place);
        List<ScopeLists> lists = new ArrayList<>();
        for (int i = 0; i < chain.size(); i++)
        {
            ExpressionScope filePlace = place.trusted(chainTrusted.get(i));
            lists.add(ScopeLists.compile(scope, chain.get(i).on, filePlace));
            if (chain.get(i).layers != null)
            {
                for (LayerTemplate layer : chain.get(i).layers)
                {
                    layers.add(layer);
                    contexts.add(context.withScope(filePlace));
                    layersTrusted.add(chainTrusted.get(i));
                }
            }
        }
        animatorScopes.add(scope);
        animatorLists.add(lists);
    }

    /** The animators {@code template} extends, the outermost first, then {@code template}. */
    private static void collectChain(AnimatorTemplate template, boolean trusted, IKumoInstancingContext context, int depth,
                                     List<AnimatorTemplate> chain, List<Boolean> chainTrusted) throws MalformedKumoTemplateException
    {
        if (template.extendsAnimator != null)
        {
            if (depth > 8)
            {
                throw new MalformedKumoTemplateException("Animator 'extends' chain is too deep (cycle?).");
            }
            AnimatorTemplate parent = context.getAnimator(template.extendsAnimator);
            if (parent == null)
            {
                throw new MalformedKumoTemplateException(String.format("Cannot resolve the animator to extend: '%s'.", template.extendsAnimator));
            }
            collectChain(parent, context.isTrusted(template.extendsAnimator), context, depth + 1, chain, chainTrusted);
        }
        chain.add(template);
        chainTrusted.add(trusted);
    }

    /** Whether every clip the layer's nodes play comes from a trusted source. */
    private static boolean playsTrustedClips(LayerTemplate layer, IKumoInstancingContext context)
    {
        for (NodeTemplate node : layer.allNodes())
        {
            List<PoseItemTemplate> items = new ArrayList<>();
            if (node instanceof PoseNodeTemplate && ((PoseNodeTemplate) node).pose != null) items.addAll(((PoseNodeTemplate) node).pose);
            if (node.enterPose != null) items.addAll(node.enterPose);
            for (PoseItemTemplate item : items)
            {
                if (item instanceof ClipItemTemplate && !context.isTrusted(((ClipItemTemplate) item).animationKey))
                {
                    return false;
                }
            }
        }
        return true;
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
        return anyUntrusted;
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
        variables.bind(subject);
        skeleton.bind(subject);

        context.beginFrame(subject, deltaTime);
        pose.clear();

        boolean limiting = limits != null && anyUntrusted;
        if (limiting && lastTrusted == null)
        {
            startLimiting();
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
        if (entityScope != null)
        {
            if (started) entityScope.updateLive(context);
            else entityScope.start(context);
            if (started) entityLists.runUpdate(context);
            else entityLists.runEnter(context);
        }
        for (int i = 0; i < animatorScopes.size(); i++)
        {
            if (started) animatorScopes.get(i).updateLive(context);
            else animatorScopes.get(i).start(context);
            for (ScopeLists lists : animatorLists.get(i))
            {
                if (started) lists.runUpdate(context);
                else lists.runEnter(context);
            }
        }

        boolean measured = false;
        for (int i = 0; i < layerStates.size(); i++)
        {
            LayerState layer = layerStates.get(i);
            if (limiting && !measured && !layerTrusted[i])
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
        pose.writeTo(skeleton);
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
            Vec3f last = lastTrusted[limitGroup[i]];
            if (vectorSlot[i])
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
            Vec3f last = lastTrusted[limitGroup[i]];
            if (vectorSlot[i])
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
    private void startLimiting()
    {
        lastTrusted = new Vec3f[pose.size()];
        Vec3f[] fallback = new Vec3f[pose.size()];
        for (int i = 0; i < lastTrusted.length; i++)
        {
            lastTrusted[i] = new Vec3f();
        }
        for (int i = 0; i < lastTrusted.length; i++)
        {
            Vec3f last = lastTrusted[limitGroup[i]];
            fallback[i] = last;
            IBoneSink sink = skeleton.sink(i);
            if (sink == null || limitGroup[i] != i) continue;
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
        for (LayerState layer : layerStates)
        {
            nodes.add(layer.getCurrentNode().getName());
        }
        return nodes;
    }

    /** True while any layer is in a {@code core:vanilla} node: the entity should be drawn vanilla. */
    public boolean wantsVanilla()
    {
        for (LayerState layer : layerStates)
        {
            if (layer.wantsVanilla()) return true;
        }
        return false;
    }

    public List<LayerState> getLayers()
    {
        return layerStates;
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }

}
