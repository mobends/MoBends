package goblinbob.mobends.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import goblinbob.mobends.core.ModelPartTransform;

public class ForgeModelPartTransform extends ModelPartTransform implements IForgeModelPart
{
    protected IForgeModelPart parent;

    @Override
    public void applyLocalTransform(MatrixStack matrix, double scale)
    {
        if (this.position.x != 0.0F || this.position.y != 0.0F || this.position.z != 0.0F)
            matrix.translate(this.position.x * scale, this.position.y * scale, this.position.z * scale);
        if (this.offset.x != 0.0F || this.offset.y != 0.0F || this.offset.z != 0.0F)
            matrix.translate(this.offset.x * scale * offsetScale, this.offset.y * scale * offsetScale, this.offset.z * scale * offsetScale);

        net.minecraft.util.math.vector.Quaternion quat = new net.minecraft.util.math.vector.Quaternion(this.rotation.x, this.rotation.y, this.rotation.z, this.rotation.w);
        matrix.mulPose(quat);
        matrix.scale(this.scale.x, this.scale.y, this.scale.z);
    }

    @Override
    public IForgeModelPart getParent()
    {
        return parent;
    }

    @Override
    public void setParent(IForgeModelPart parent)
    {
        this.parent = parent;
    }

    @Override
    public void addChild(ModelPart parent)
    {
        throw new IllegalStateException("Cannot add a child to a ForgeModelPartTransform.");
    }
}
