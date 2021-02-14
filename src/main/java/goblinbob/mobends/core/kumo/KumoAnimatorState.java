package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class KumoAnimatorState<D extends IEntityData>
{
    private List<ILayerState<D>> layerStates = new ArrayList<>();
    private KumoContext<D> context = new KumoContext<>();
    private boolean started = false;

    public KumoAnimatorState(AnimatorTemplate animatorTemplate, IKumoInstancingContext<D> context)
    {
        if (animatorTemplate.layers == null)
        {
            throw new AnimationRuntimeException("No layers were specified");
        }

        for (LayerTemplate template : animatorTemplate.layers)
        {
            layerStates.add(template.instantiate(context));
        }
    }

    public void update(D entityData, float ticksPassed)
    {
        // Populating the context.
        context.entityData = entityData;
        context.ticksPassed = ticksPassed;

        for (ILayerState<D> layer : layerStates)
        {
            // Populating the context.
            context.layerState = layer;

            if (!started)
            {
                layer.start(context);
            }

            // Updating the layer.
            layer.update(context);
        }

        started = true;
    }
}
