package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public interface IPartWrapper
{
    public void apply(ArmorWrapper armorWrapper);
    public void deapply(ArmorWrapper armorWrapper);
    public void syncUp(EntityData<?> data);

    public IPartWrapper offsetInner(float x, float y, float z);
    public IPartWrapper setParent(IModelPart parent);

    /**
     * Moves GL into the frame the vanilla part's boxes are drawn in, as posed: a point of the part
     * (in its vanilla coordinates, times {@code scale}) is then drawn where the animated part has it.
     * For a part cut in two, the frame of the segment above {@link #getCutPlane}.
     */
    public void applyTransform(float scale);

    /** Moves GL into the frame of the segment below {@link #getCutPlane}; a part in one piece has only {@link #applyTransform}'s. */
    public default void applyLowerTransform(float scale)
    {
        applyTransform(scale);
    }

    /** Where the part is cut in two (the elbow, the knee), along its vanilla y; {@code NaN} for a part in one piece. */
    public default float getCutPlane()
    {
        return Float.NaN;
    }

    @FunctionalInterface
    public interface DataPartSelector
    {
        IModelPart selectPart(EntityData<?> data);
    }

    @FunctionalInterface
    public interface ModelPartSetter
    {
        void replacePart(ModelBiped model, ModelRenderer part);
    }
}
