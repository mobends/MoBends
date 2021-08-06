package goblinbob.mobends.standard.client.model.armor;

import java.util.LinkedList;
import java.util.List;

import goblinbob.mobends.core.client.model.BoxFactory;
import goblinbob.mobends.core.client.model.BoxMutator;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.client.model.MutatedBox;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBox;

public class HumanoidLimbWrapper extends HumanoidPartWrapper
{
    private List<ModelBox> vanillaBoxes = new LinkedList<>();
    /**
     * A list of both slices boxes and original boxes that didn't have to get sliced.
     */
    private List<ModelBox> mutatedBoxes = new LinkedList<>();

    private IPartWrapper.DataPartSelector lowerPartDataSelector;
    private ModelPart lowerPart;

    public HumanoidLimbWrapper(
        IPartWrapper.DataPartSelector upperPartDataSelector,
        IPartWrapper.DataPartSelector lowerPartDataSelector,
        IPartWrapper.ModelPartSelector modelPartSelector,
        IPartWrapper.ModelPartSetter modelPartSetter)
    {
        super(upperPartDataSelector, modelPartSelector, modelPartSetter);

        this.lowerPartDataSelector = lowerPartDataSelector;
    }

    @Override
    public void mutate(ModelBiped originalModel)
    {
        super.mutate(originalModel);

        this.sliceAppendage(originalModel);
    }

    private void sliceAppendage(ModelBiped originalModel)
    {
        final float cutPlane = 6.0F;
        List<ModelBox> cubeList = vanillaPart.cubeList;

        // Storing the vanilla boxes
        vanillaBoxes.addAll(cubeList);

        this.lowerPart = new ModelPart(originalModel, false);
        lowerPart.mirror = vanillaPart.mirror;
        this.partContainer.addChild(lowerPart);

        for (ModelBox box : vanillaBoxes)
        {
            final BoxMutator mutator = BoxMutator.createFrom(originalModel, vanillaPart, box);

            if (mutator == null)
            {
                continue;
            }

            if (mutator.getFactory().min.y < cutPlane)
            {
                // Upper leg, try to cut the bottom
                BoxFactory lowerPartFactory = mutator.sliceFromBottom(cutPlane);

                // Adding the upper part to the mutated boxes list.
                mutatedBoxes.add(mutator.getFactory().create(this.partContainer));

                if (lowerPartFactory != null)
                {
                    float slightInflation = 0.01F;
                    MutatedBox lowerPartBox = lowerPartFactory.offset(0, -cutPlane, 2F).inflate(slightInflation, 0, slightInflation).create(this.partContainer);
                    lowerPart.addBox(lowerPartBox);
                }
            }
            else
            {
                // Lower leg, adding the unchanged box.
                lowerPart.addVanillaBox(box);
            }
        }
    }

    @Override
    public void syncUp(BipedEntityData<?> data)
    {
        super.syncUp(data);

        if (lowerPart != null)
        {
            lowerPart.syncUp(lowerPartDataSelector.selectPart(data));
        }
    }

    @Override
    public void apply(ArmorWrapper armorWrapper)
    {
        super.apply(armorWrapper);

        vanillaPart.cubeList.clear();
        vanillaPart.cubeList.addAll(mutatedBoxes);
    }

    @Override
    public void deapply(ArmorWrapper armorWrapper)
    {
        super.deapply(armorWrapper);

        vanillaPart.cubeList.clear();
        vanillaPart.cubeList.addAll(vanillaBoxes);
    }
}
