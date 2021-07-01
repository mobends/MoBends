package goblinbob.mobends.core;

import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.core.mutation.MutationMetadata;

public interface IBenderResources
{
    MutationMetadata getMutationMetadata();
    MutationInstructions getMutationInstructions();
    AnimatorTemplate getAnimatorTemplate();
}
