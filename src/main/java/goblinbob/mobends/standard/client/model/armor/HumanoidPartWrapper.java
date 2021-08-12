package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class HumanoidPartWrapper implements IPartWrapper
{
    protected IPartWrapper.DataPartSelector dataPartSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;

    /**
     * The vanilla body part model.
     */
    protected ModelRenderer vanillaPart;
    /**
     * The container for the vanilla part.
     */
    protected PartContainer partContainer;

    public HumanoidPartWrapper(
        ModelBiped vanillaModel,
        ModelRenderer vanillaPart,
        IPartWrapper.ModelPartSetter modelPartSetter,
        IPartWrapper.DataPartSelector dataPartSelector)
    {
        this.dataPartSelector = dataPartSelector;
        this.modelPartSetter = modelPartSetter;
        this.vanillaPart = vanillaPart;

        // Mutating by default

        if (vanillaPart instanceof PartContainer)
        {
            throw new MalformedArmorModelException("Tried to mutate a previously mutated part. " +
                "A ModelRenderer instance has to have been used between Model instances.");
        }

        // Create a part container for the original MR.
        this.partContainer = new PartContainer(vanillaModel, vanillaPart);
    }

    @Override
    public void syncUp(BipedEntityData<?> data)
    {
        partContainer.syncUp(dataPartSelector.selectPart(data));
    }

    @Override
    public void apply(ArmorWrapper armorWrapper)
    {
        // Replacing the parts both on the original and the wrapper. That way, any visibility change will be applied to a proper instance.
        this.modelPartSetter.replacePart(armorWrapper, partContainer);
        this.modelPartSetter.replacePart(armorWrapper.original, partContainer);

        partContainer.isHidden = vanillaPart.isHidden;
        partContainer.showModel = vanillaPart.showModel;
    }

    @Override
    public void deapply(ArmorWrapper armorWrapper)
    {
        // Replacing the parts both on the original and the wrapper. That way, any visibility change will be applied to a proper instance.
        this.modelPartSetter.replacePart(armorWrapper, vanillaPart);
        this.modelPartSetter.replacePart(armorWrapper.original, vanillaPart);

        vanillaPart.isHidden = partContainer.isHidden;
        vanillaPart.showModel = partContainer.showModel;
    }

    @Override
    public IPartWrapper setParent(IModelPart parent)
    {
        partContainer.setParent(parent);
        return this;
    }

    @Override
    public IPartWrapper offsetInner(float x, float y, float z)
    {
        partContainer.setInnerOffset(x, y, z);
        return this;
    }
}
