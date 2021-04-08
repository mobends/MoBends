package goblinbob.mobends.core;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.mutation.MutationInstructions;

import java.io.IOException;

public interface IBenderResources
{
    MutationInstructions getMutationInstructions();
    AnimatorTemplate getAnimatorTemplate();
    <D extends IEntityData> void loadResources(ISerialContext<D> serialContext) throws IOException;
}
