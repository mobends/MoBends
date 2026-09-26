package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A running instance of an animator template for one subject. Each update evaluates every
 * layer into one pose and writes the pose's targets to the subject's bones; the bones' own
 * smoothing then does the damping, exactly as it does for the procedural animations.
 *
 * @param <S> the subject type; kept generic for source compatibility with the old EntityData-typed API.
 */
public class KumoAnimatorState<S extends IKumoSubject>
{

    private final List<ILayerState> layerStates = new ArrayList<>();
    private final Skeleton skeleton = new Skeleton();
    private final KumoContext context = new KumoContext();
    private final Pose pose;
    private boolean started = false;

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
        List<LayerTemplate> layers = new ArrayList<>();
        List<IKumoInstancingContext> layerContexts = new ArrayList<>();
        collectLayers(animatorTemplate, dataProvider, 0, layers, layerContexts);
        for (AnimatorTemplate overlay : overlays)
        {
            collectLayers(overlay, dataProvider, 0, layers, layerContexts);
        }
        if (layers.isEmpty())
        {
            throw new MalformedKumoTemplateException("No layers were specified");
        }
        for (int i = 0; i < layers.size(); i++)
        {
            LayerTemplate template = layers.get(i);
            IKumoInstancingContext layerContext = layerContexts.get(i).withExpressions(template.expressions);
            layerStates.add(ILayerState.createFromTemplate(layerContext, skeleton, template));
        }
        context.layers = layerStates;

        // Every bone name is known once the layers are instanced.
        pose = new Pose(skeleton, true);
    }

    /**
     * Collects the parent's layers (via "extends") first, then this animator's own, each with the
     * context it is instanced in: it sees the named expressions of the animator that declares it and
     * of that animator's parents, so a child animator can use and shadow its parent's, never the
     * other way around.
     *
     * @return The context of this animator's own declarations.
     */
    private static IKumoInstancingContext collectLayers(AnimatorTemplate template, IKumoInstancingContext context, int depth,
                                                        List<LayerTemplate> layers, List<IKumoInstancingContext> contexts) throws MalformedKumoTemplateException
    {
        IKumoInstancingContext animatorContext = context;
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
            animatorContext = collectLayers(parent, context, depth + 1, layers, contexts);
        }
        animatorContext = animatorContext.withExpressions(template.expressions);
        if (template.layers != null)
        {
            for (LayerTemplate layer : template.layers)
            {
                layers.add(layer);
                contexts.add(animatorContext);
            }
        }
        return animatorContext;
    }

    public void update(S subject, float deltaTime) throws MalformedKumoTemplateException
    {
        skeleton.bind(subject);

        context.subject = subject;
        context.deltaTime = deltaTime;
        pose.clear();

        for (ILayerState layer : layerStates)
        {
            context.layerState = layer;
            context.currentNode = null;

            if (!started)
            {
                layer.start(context);
            }

            layer.update(context, deltaTime, pose);
        }

        started = true;
        pose.writeTo(skeleton);
    }

    /** The tags of every layer's current node, in layer order. */
    public List<String> getActions()
    {
        List<String> actions = new ArrayList<>();
        for (ILayerState layer : layerStates)
        {
            actions.addAll(layer.getActions());
        }
        return actions;
    }

    public List<ILayerState> getLayers()
    {
        return layerStates;
    }

    public Skeleton getSkeleton()
    {
        return skeleton;
    }

}
