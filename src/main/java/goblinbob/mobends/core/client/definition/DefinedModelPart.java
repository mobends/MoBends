package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;

/** A {@link ModelPart} with a constant rest rotation applied before the animated one. */
public class DefinedModelPart extends ModelPart
{

    private Quaternion restRotation;

    public DefinedModelPart(ModelBase model)
    {
        super(model, false, 0, 0);
    }

    public DefinedModelPart setRestRotation(Quaternion restRotation)
    {
        this.restRotation = restRotation;
        return this;
    }

    @Override
    public void applyLocalTransform(float scale)
    {
        if (restRotation == null)
        {
            super.applyLocalTransform(scale);
            return;
        }
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            GlStateManager.translate(this.position.x * scale * offsetScale, this.position.y * scale * offsetScale, this.position.z * scale * offsetScale);
        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            GlStateManager.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);
        GlHelper.rotate(restRotation);
        GlHelper.rotate(rotation.getSmooth());
        if (this.scale.x != 0.0F || this.scale.y != 0.0F || this.scale.z != 0.0F)
            GlStateManager.scale(this.scale.x, this.scale.y, this.scale.z);
    }

}
