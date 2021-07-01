package goblinbob.mobends.forge;

import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.core.mutation.MutationMetadata;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenderResourcesFactory<C>
{
    private Map<ResourceLocation, MutationMetadata> locationToMetadataMap = new HashMap<>();

    private Map<String, List<AnimatorTemplate>> idToAnimatorsMap = new HashMap<>();

    public void registerMetadata(MutationMetadata metadata) throws ResourceException
    {
        ResourceLocation entityLocation;
        try
        {
            entityLocation = new ResourceLocation(metadata.getTarget());
        }
        catch(ResourceLocationException e)
        {
            throw new ResourceException(e.getMessage());
        }
        catch(NullPointerException e)
        {
            throw new ResourceException("No target was specified in the metadata.");
        }

        locationToMetadataMap.put(entityLocation, metadata);
    }

    public void registerPartialAnimator(AnimatorTemplate animatorTemplate)
    {
        idToAnimatorsMap.computeIfAbsent(
            animatorTemplate.mutatorId,
            (key) -> new ArrayList<>()
        ).add(animatorTemplate);
    }

    public BenderResources registerMutator(MutationInstructions mutationInstructions) throws ResourceException
    {
        String id = mutationInstructions.getID();
        ResourceLocation entityLocation;
        try
        {
            entityLocation = new ResourceLocation(mutationInstructions.getTarget());
        }
        catch(ResourceLocationException|NullPointerException e)
        {
            throw new ResourceException(e.getMessage());
        }

        if (!locationToMetadataMap.containsKey(entityLocation))
        {
            throw new ResourceException(String.format("No metadata defined for target '%s'.", entityLocation.toString()));
        }

        if (!idToAnimatorsMap.containsKey(id))
        {
            throw new ResourceException(String.format("No animators defined for mutator '%s'.", id));
        }

        return new BenderResources(mutationInstructions, locationToMetadataMap.get(entityLocation), idToAnimatorsMap.get(id));
    }
}
