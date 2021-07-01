package goblinbob.mobends.forge;

import goblinbob.mobends.core.IBenderResources;
import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.core.mutation.MutationMetadata;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class BenderResources implements IBenderResources
{
    private ResourceLocation entityLocation;
    private MutationInstructions mutationInstructions;
    private MutationMetadata mutationMetadata;
    private List<AnimatorTemplate> partialAnimators;

    private AnimatorTemplate combinedAnimatorTemplate;

    public BenderResources(MutationInstructions instructions, MutationMetadata metadata, List<AnimatorTemplate> partialAnimators) throws ResourceException
    {
        this.entityLocation = new ResourceLocation(instructions.getTarget());
        this.mutationInstructions = instructions;
        this.mutationMetadata = metadata;
        this.partialAnimators = partialAnimators;

        this.mergePartials();
    }

    public void addPartialAnimator(AnimatorTemplate animatorTemplate)
    {
        this.partialAnimators.add(animatorTemplate);
    }

    public ResourceLocation getEntityLocation()
    {
        return entityLocation;
    }

    @Override
    public MutationInstructions getMutationInstructions()
    {
        return mutationInstructions;
    }

    @Override
    public MutationMetadata getMutationMetadata()
    {
        return mutationMetadata;
    }

    @Override
    public AnimatorTemplate getAnimatorTemplate()
    {
        return combinedAnimatorTemplate;
    }

    public void mergePartials() throws ResourceException
    {
        if (this.partialAnimators.size() == 0)
            throw new ResourceException("No animators defined.");

        this.combinedAnimatorTemplate = this.partialAnimators.get(0);
    }
}
