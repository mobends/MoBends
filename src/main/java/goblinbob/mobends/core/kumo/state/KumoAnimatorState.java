package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.ArrayList;
import java.util.List;

public class KumoAnimatorState<D extends EntityData<?>>
{

    private List<ILayerState> layerStates = new ArrayList<>();
    private KumoContext context = new KumoContext();

    public KumoAnimatorState(AnimatorTemplate animatorTemplate, IKumoInstancingContext dataProvider) throws MalformedKumoTemplateException
    {
        if (animatorTemplate.layers == null)
        {
            throw new MalformedKumoTemplateException("No layers were specified");
        }

        for (LayerTemplate template : animatorTemplate.layers)
        {
            layerStates.add(ILayerState.createFromTemplate(dataProvider, template));
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

            // Updating the layer.
            layer.update(context, deltaTime);
        }
    }

}
