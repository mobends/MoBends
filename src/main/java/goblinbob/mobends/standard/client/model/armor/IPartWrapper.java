package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public interface IPartWrapper
{
    public void apply(ArmorWrapper armorWrapper);
    public void deapply(ArmorWrapper armorWrapper);
    public void syncUp(BipedEntityData<?> data);

    public IPartWrapper offsetInner(float x, float y, float z);
    public IPartWrapper setParent(IModelPart parent);

    @FunctionalInterface
    public interface DataPartSelector
    {
        ModelPartTransform selectPart(BipedEntityData<?> data);
    }

    @FunctionalInterface
    public interface ModelPartSetter
    {
        void replacePart(ModelBiped model, ModelRenderer part);
    }
}
