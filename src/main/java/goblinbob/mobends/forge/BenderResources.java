package goblinbob.mobends.forge;

import goblinbob.mobends.core.IBenderResources;
import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;

import java.util.ArrayList;
import java.util.List;

public class BenderResources implements IBenderResources
{
    private MutationInstructions mutationInstructions;
    private AnimatorTemplate animatorTemplate;

    private List<MutationInstructions> partialMutators = new ArrayList<>();
    private List<AnimatorTemplate> partialAnimators = new ArrayList<>();

    public BenderResources()
    {
    }

    public void clear()
    {
        this.partialMutators.clear();
        this.partialAnimators.clear();
    }

    public void addPartialMutator(MutationInstructions mutationInstructions)
    {
        this.partialMutators.add(mutationInstructions);
    }

    public void addPartialAnimator(AnimatorTemplate animatorTemplate)
    {
        this.partialAnimators.add(animatorTemplate);
    }

    @Override
    public MutationInstructions getMutationInstructions()
    {
        return mutationInstructions;
    }

    @Override
    public AnimatorTemplate getAnimatorTemplate()
    {
        return animatorTemplate;
    }

    public void mergePartials() throws ResourceException
    {
        if (this.partialMutators.size() == 0)
            throw new ResourceException("No mutation instructions defined.");

        if (this.partialAnimators.size() == 0)
            throw new ResourceException("No animators defined.");

        this.mutationInstructions = this.partialMutators.get(0);
        this.animatorTemplate = this.partialAnimators.get(0);
    }
}
