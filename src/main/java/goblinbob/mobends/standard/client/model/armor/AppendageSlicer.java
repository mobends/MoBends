package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.*;
import goblinbob.mobends.core.util.ModelUtils;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AppendageSlicer
{

    private LinkedList<ModelRenderer> originalModelRenderers = new LinkedList<>();
    private PartBoxes vanillaPartBoxes = new PartBoxes();
    private PartBoxes mutatedPartBoxes = new PartBoxes();

    public void registerOriginalModelRenderer(ModelRenderer renderer)
    {
        this.originalModelRenderers.add(renderer);
    }

    protected void apply()
    {
        for (ModelRenderer part : this.vanillaPartBoxes.keySet())
        {
            part.cubeList.clear();
        }

        for (Map.Entry<ModelRenderer, List<ModelBox>> entry : this.mutatedPartBoxes.entrySet())
        {
            entry.getKey().cubeList.addAll(entry.getValue());
        }
    }

    protected void deapply()
    {
        for (Map.Entry<ModelRenderer, List<ModelBox>> entry : this.vanillaPartBoxes.entrySet())
        {
            ModelRenderer renderer = entry.getKey();
            renderer.cubeList.clear();
            renderer.cubeList.addAll(entry.getValue());
        }
    }

    protected void slice(MutatedArmorModel armor, ModelPartContainer part, PartGroup targetGroup, float cutPlane)
    {
        final ModelRenderer originalPart = part.getModel();
        vanillaPartBoxes.clearRenderer(originalPart);
        mutatedPartBoxes.clearRenderer(originalPart);

        for (int i = originalPart.cubeList.size() - 1; i >= 0; i--)
        {
            final net.minecraft.client.model.ModelBox box = originalPart.cubeList.get(i);
            vanillaPartBoxes.put(originalPart, box);

            final BoxMutator mutator = BoxMutator.createFrom(armor, originalPart, box);

            if (mutator == null)
            {
                continue;
            }

            mutator.includeParentTransform(ModelUtils.getParentsList(originalPart, this.originalModelRenderers));

            if (mutator.getGlobalBoxY() < cutPlane)
            {
                // Upper leg, try to cut the bottom
                BoxFactory lowerPartFactory = mutator.sliceFromBottom(cutPlane, true);

                mutatedPartBoxes.put(originalPart, mutator.getFactory().create(part));

                if (lowerPartFactory != null)
                {
                    ModelPart modelPart = new ModelPart(armor, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
                    modelPart.mirror = part.mirror;
                    MutatedBox lowerPart = lowerPartFactory.create(modelPart);
                    modelPart.cubeList.add(lowerPart);
                    ModelPartContainer partContainer = new ModelPartContainer(armor, modelPart);
                    targetGroup.add(partContainer);
                }
            }
            else
            {
                // Lower leg
                ModelPart modelPart = new ModelPart(armor, mutator.getTextureOffsetX(), mutator.getTextureOffsetY());
                modelPart.mirror = part.mirror;
                MutatedBox lowerBox = mutator.getFactory().create(modelPart);
                modelPart.cubeList.add(lowerBox);
                ModelPartContainer partContainer = new ModelPartContainer(armor, modelPart);
                targetGroup.add(partContainer);
            }
        }
    }

}
