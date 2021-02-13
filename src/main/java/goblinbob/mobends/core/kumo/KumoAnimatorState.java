package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;

public class KumoAnimatorState<D extends IEntityData>
{
    private List<ILayerState> layerStates = new ArrayList<>();
    private KumoContext context = new KumoContext();
    private boolean started = false;

    public KumoAnimatorState(AnimatorTemplate animatorTemplate, IKumoInstancingContext<D> context) throws MalformedKumoTemplateException
    {
        if (animatorTemplate.layers == null)
        {
            throw new MalformedKumoTemplateException("No layers were specified");
        }

        for (LayerTemplate template : animatorTemplate.layers)
        {
            layerStates.add(template.produceInstance(context));
        }
    }

    public void update(D entityData, float deltaTime) throws MalformedKumoTemplateException
    {
        // Populating the context.
        context.entityData = entityData;

        for (ILayerState layer : layerStates)
        {
            // Populating the context.
            context.layerState = layer;

            if (!started)
            {
                layer.start(context);
            }

            // Updating the layer.
            layer.update(context, deltaTime);
        }

        started = true;
    }
}
