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
 * An animator compiled: its layers, nodes, machines and expressions, its bones, and the layout of
 * the state every entity it animates keeps (see {@link StateLayout}). It holds nothing of any
 * entity, so one program serves every entity it was compiled for: each has a
 * {@link KumoAnimatorState} of its own.
 */
public final class KumoProgram
{

    final List<LayerState> layerStates = new ArrayList<>();
    final Skeleton skeleton = new Skeleton();
    /** Where the per-entity state goes. */
    final StateLayout layout;
    final VariableTable variables = new VariableTable();
    /** The entity's scope, which its model definition declares; null if it declares none. */
    @Nullable
    final DefinitionScope entityScope;
    @Nullable
    final ScopeLists entityLists;
    /** The animator's scope and each extension's: its definitions and statement lists, one per file. */
    final List<DefinitionScope> animatorScopes = new ArrayList<>();
    final List<List<ScopeLists>> animatorLists = new ArrayList<>();
    private final Map<Integer, MalformedKumoTemplateException> skippedExtensions = new LinkedHashMap<>();
    /** The animator's pose and the trusted layers' result, as the entity state's pose buffers. */
    final int pose, trustedPose;
    final boolean[] layerTrusted;
    boolean anyUntrusted;
    /** Per slot: the slot whose trusted value it is limited around (aliases share one). */
    final int[] limitGroup;
    /** Per slot: whether it is an entity-level vector (see {@link Skeleton#isVectorBone}). */
    final boolean[] vectorSlot;

    /**
     * Compiles an animator.
     *
     * @param entity          The entity animated: its class, which operations bind against, and the
     *                        entity scope its model definition declares (read as {@code entity.x}
     *                        by the animator and every extension). Null: unknown, with no entity
     *                        scope.
     * @param trusted         Whether the animator's own file comes from a trusted source (the mod
     *                        or another mod; see {@link IKumoInstancingContext#isTrusted}).
     * @param overlays        Animators whose layers go on top of the animator's, in order
     *                        (extensions). Each is its own animator: it sees its own names, not the
     *                        base's.
     * @param overlaysTrusted The same as {@code trusted}, for each overlay. A layer is trusted when
     *                        the file declaring it is and every clip it plays is; the others are
     *                        limited (see {@link KumoAnimatorState#setLimits}).
     */
    public KumoProgram(@Nullable EntityTemplate entity, AnimatorTemplate animatorTemplate, boolean trusted, List<AnimatorTemplate> overlays,
                       List<Boolean> overlaysTrusted, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        // Every scope of the animator, its extensions included, shares one table of the entity's values.
        ExpressionScope root = ExpressionScope.root(variables).forEntity(entity == null ? null : entity.entityClass);
        this.layout = root.getLayout();
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
        instanceLayers(layers, layerContexts);
        for (int i = 0; i < overlays.size(); i++)
        {
            // An extension's animator is a scope of its own: it never sees the extended animator's
            // names. One that fails to load (it reads an entity. name the model doesn't declare,
            // say) is left out: the animator and the other extensions still animate.
            int layerCount = layers.size(), scopeCount = animatorScopes.size();
            try
            {
                addAnimator(overlays.get(i), overlaysTrusted.get(i), dataProvider, layers, layerContexts, layersTrusted);
                instanceLayers(layers, layerContexts);
            }
            catch (MalformedKumoTemplateException e)
            {
                truncate(layers, layerCount);
                truncate(layerContexts, layerCount);
                truncate(layersTrusted, layerCount);
                truncate(layerStates, layerCount);
                truncate(animatorScopes, scopeCount);
                truncate(animatorLists, scopeCount);
                skippedExtensions.put(i, e);
            }
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
        // Every bone name is known once the layers are instanced.
        for (LayerState layer : layerStates)
        {
            layer.allocate();
        }
        // The animator's own pose (which reads the bones it doesn't write) and the trusted layers' result.
        pose = layout.pose(true);
        trustedPose = layout.pose();
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

    /** Instances the layers added since the last call. */
    private void instanceLayers(List<LayerTemplate> layers, List<IKumoInstancingContext> contexts) throws MalformedKumoTemplateException
    {
        for (int i = layerStates.size(); i < layers.size(); i++)
        {
            layerStates.add(new LayerState(contexts.get(i), skeleton, layers.get(i)));
        }
    }

    private static void truncate(List<?> list, int size)
    {
        while (list.size() > size)
        {
            list.remove(list.size() - 1);
        }
    }

    /**
     * The extensions that failed to load and were left out, by their index among the overlays,
     * with what went wrong.
     */
    public Map<Integer, MalformedKumoTemplateException> getSkippedExtensions()
    {
        return Collections.unmodifiableMap(skippedExtensions);
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

    /** Whether any layer comes from an untrusted source. */
    public boolean hasUntrustedLayers()
    {
        return anyUntrusted;
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
