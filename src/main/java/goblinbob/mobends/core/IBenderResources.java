package goblinbob.mobends.core;

import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;

public interface IBenderResources
{
    MutationInstructions getMutationInstructions();
    AnimatorTemplate getAnimatorTemplate();
}
