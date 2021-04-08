package goblinbob.mobends.forge;

import goblinbob.mobends.core.IBenderResources;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.mutation.MutationInstructions;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class BenderResources implements IBenderResources
{
    private final ResourceLocation mutatorLocation;
    private final ResourceLocation animatorLocation;

    private MutationInstructions mutationInstructions;
    private AnimatorTemplate animatorTemplate;

    public BenderResources(ResourceLocation mutatorLocation, ResourceLocation animatorLocation)
    {
        this.mutatorLocation = mutatorLocation;
        this.animatorLocation = animatorLocation;
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

    @Override
    public <D extends IEntityData> void loadResources(ISerialContext<D> serialContext) throws IOException
    {
        this.mutationInstructions = GsonResources.get(this.mutatorLocation, MutationInstructions.class);
        this.animatorTemplate = AnimatorTemplate.deserialize(serialContext, BinaryResources.getStream(this.animatorLocation));
    }
}
