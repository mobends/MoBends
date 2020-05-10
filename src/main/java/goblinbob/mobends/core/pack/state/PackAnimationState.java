package goblinbob.mobends.core.pack.state;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.*;
import goblinbob.mobends.core.pack.BendsPackData;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

public class PackAnimationState
{

    private KumoAnimatorState<EntityData<?>> kumoAnimatorState;
    private BendsPackData bendsPackData;

    private void initFor(BendsPackData data, String animatedEntityKey) throws MalformedKumoTemplateException
    {
        bendsPackData = data;

        if (data.targets == null)
        {
            throw new MalformedKumoTemplateException("No targets were specified!");
        }

        AnimatorTemplate targetTemplate = data.targets.get(animatedEntityKey);
        if (targetTemplate == null)
        {
            throw new MalformedKumoTemplateException("No targets were specified!");
        }

        kumoAnimatorState = new KumoAnimatorState<>(targetTemplate, data);
    }

    public void update(EntityData<?> entityData, BendsPackData data, String animatedEntityKey, float deltaTime) throws MalformedKumoTemplateException
    {
        // If a new BendsPack has been equipped, reinitialize the state.
        if (bendsPackData != data)
        {
            bendsPackData = data;
            try
            {
                initFor(data, animatedEntityKey);
            }
            catch(MalformedKumoTemplateException e)
            {
                bendsPackData = null;
                throw e;
            }
        }

        kumoAnimatorState.update(entityData, deltaTime);
    }

}
