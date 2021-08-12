package goblinbob.mobends.standard.client.model.armor;

import java.util.List;

import goblinbob.mobends.core.client.model.BoxFactory;
import goblinbob.mobends.core.client.model.BoxMutator;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

public class HumanoidLimbWrapper implements IPartWrapper
{
    private final ModelRenderer vanillaPart;
    protected IPartWrapper.DataPartSelector upperPartDataSelector;
    protected IPartWrapper.ModelPartSetter modelPartSetter;
    private final IPartWrapper.DataPartSelector lowerPartDataSelector;
    private final float inflation;

    private ModelPart upperPart;
    private ModelPart upperPartAnchor;
    private ModelPart lowerPart;
    private ModelPart lowerPartAnchor;

    public HumanoidLimbWrapper(
        ModelBiped vanillaModel,
        ModelRenderer vanillaPart,
        IPartWrapper.ModelPartSetter modelPartSetter,
        IPartWrapper.DataPartSelector upperPartDataSelector,
        IPartWrapper.DataPartSelector lowerPartDataSelector,
        float cutPlane,
        float inflation)
    {
        this.vanillaPart = vanillaPart;
        this.upperPartDataSelector = upperPartDataSelector;
        this.lowerPartDataSelector = lowerPartDataSelector;
        this.modelPartSetter = modelPartSetter;
        this.inflation = inflation;

        if (vanillaPart instanceof PartContainer)
        {
            throw new MalformedArmorModelException("Tried to mutate a previously mutated part. " +
                "A ModelRenderer instance has to have been used between Model instances.");
        }

        this.upperPart = new ModelPart(vanillaModel, false);
        this.upperPartAnchor = (new ModelPart(vanillaModel, false));
        this.lowerPart = new ModelPart(vanillaModel, false);
        this.lowerPartAnchor = (new ModelPart(vanillaModel, false));

        this.upperPart.addChild(this.upperPartAnchor);
        this.upperPart.addChild(this.lowerPart);
        this.lowerPart.addChild(this.lowerPartAnchor);

        upperPart.mirror = upperPartAnchor.mirror = lowerPart.mirror = lowerPartAnchor.mirror = vanillaPart.mirror;

        this.sliceAppendage(vanillaModel, vanillaPart, cutPlane);
    }

    private void sliceAppendage(ModelBiped vanillaModel, ModelRenderer vanillaPart, float cutPlane)
    {
        // Storing the vanilla boxes
        List<ModelBox> vanillaBoxes = vanillaPart.cubeList;

        for (ModelBox box : vanillaBoxes)
        {
            final BoxMutator mutator = BoxMutator.createFrom(vanillaModel, vanillaPart, box);

            if (mutator == null)
            {
                continue;
            }

            if (mutator.getFactory().min.y < cutPlane)
            {
                // Upper leg, try to cut the bottom
                BoxFactory lowerPartFactory = mutator.sliceFromBottom(cutPlane);

                // Adding the upper part to the mutated boxes list.
                upperPartAnchor.addBox(mutator.getFactory().inflate(inflation, 0, inflation).create(upperPart));

                if (lowerPartFactory != null)
                {
                    float lowerInflation = inflation + 0.001F;
                    lowerPartAnchor.addBox(lowerPartFactory.inflate(lowerInflation, 0, lowerInflation).create(upperPart));
                }
            }
            else
            {
                // Lower leg, adding the unchanged box.
                lowerPartAnchor.addVanillaBox(box);
            }
        }

        // Reassigning children
        List<ModelRenderer> vanillaChildren = vanillaPart.childModels;

        if (vanillaChildren != null)
        {
            for (ModelRenderer child : vanillaChildren)
            {
                if (child == null)
                    continue;

                if (child.rotationPointY < cutPlane)
                {
                    // This child will appear as a child of the UPPER part.
                    upperPartAnchor.addChild(child);
                }
                else
                {
                    // This child will appear as a child of the LOWER part.
                    lowerPartAnchor.addChild(child);
                }
            }
        }
    }

    @Override
    public void syncUp(BipedEntityData<?> data)
    {
        upperPart.syncUp(upperPartDataSelector.selectPart(data));

        if (lowerPart != null)
        {
            lowerPart.syncUp(lowerPartDataSelector.selectPart(data));
        }
    }

    @Override
    public void apply(ArmorWrapper armorWrapper)
    {
        // Replacing the parts both on the original and the wrapper. That way, any visibility change will be applied to a proper instance.
        this.modelPartSetter.replacePart(armorWrapper, upperPart);
        this.modelPartSetter.replacePart(armorWrapper.original, upperPart);

        upperPart.isHidden = vanillaPart.isHidden;
        upperPart.showModel = vanillaPart.showModel;
    }

    @Override
    public void deapply(ArmorWrapper armorWrapper)
    {
        // Replacing the parts both on the original and the wrapper. That way, any visibility change will be applied to a proper instance.
        this.modelPartSetter.replacePart(armorWrapper, vanillaPart);
        this.modelPartSetter.replacePart(armorWrapper.original, vanillaPart);

        vanillaPart.isHidden = upperPart.isHidden;
        vanillaPart.showModel = upperPart.showModel;
    }

    @Override
    public IPartWrapper setParent(IModelPart parent)
    {
        upperPart.setParent(parent);
        return this;
    }

    @Override
    public IPartWrapper offsetInner(float x, float y, float z)
    {
        upperPartAnchor.setPosition(x, y, z);
        return this;
    }

    public HumanoidLimbWrapper offsetLower(float x, float y, float z)
    {
        lowerPartAnchor.setPosition(x, y, z);
        return this;
    }
}
