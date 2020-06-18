package goblinbob.mobends.core.pack;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Collection;

public class BendsPackPerformer
{

    public static final BendsPackPerformer INSTANCE = new BendsPackPerformer();

    public void performCurrentPack(EntityData<?> entityData, String animatedEntityKey, @Nullable Collection<String> actions)
    {
        final BendsPackData packData = PackDataProvider.INSTANCE.getAppliedData();
        if (packData == null)
        {
            return;
        }

        try
        {
            entityData.packAnimationState.update(entityData, packData, animatedEntityKey, DataUpdateHandler.ticksPerFrame);
        }
        catch (MalformedKumoTemplateException e)
        {
            // Resetting the applied packs due to malformed templates.
            e.printStackTrace();
            PackManager.INSTANCE.resetAppliedPacks(true);
        }
    }

}
