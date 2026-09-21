package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
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
        if (animatorTemplate.layers == null)
        {
            throw new MalformedKumoTemplateException("No layers were specified");
        }

        for (LayerTemplate template : animatorTemplate.layers)
        {
            layerStates.add(ILayerState.createFromTemplate(dataProvider, skeleton, template));
        }

        // Every bone name is known once the layers are instanced.
        pose = new Pose(skeleton);
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
