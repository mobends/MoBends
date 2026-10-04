package goblinbob.mobends.core.mutators;

import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public abstract class Mutator<D extends LivingEntityData<E>, E extends EntityLivingBase, M extends ModelBase>
{

    protected float headYaw;
    protected float headPitch;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float swingProgress;

    protected List<LayerRenderer<?>> layerRenderers;

    /**
     * Used to fetch private data from the original
     * renderer.
     */
    public void fetchFields(RenderLivingBase<? extends E> renderer)
    {
        // Getting the layer renderers
        this.layerRenderers = (List<LayerRenderer<?>>) ((Object) renderer.layerRenderers); // Type safety hack...
    }

    /**
     * Swaps out a vanilla layer for its custom counterpart. The renderer's vanilla state is kept
     * by {@code RendererState}, which puts it back when the entity is drawn vanilla.
     */
    public abstract void swapLayer(RenderLivingBase<? extends E> renderer, int index);

    /** Replaces the model's parts with newly created custom parts. */
    public abstract boolean createParts(M original);

    public boolean mutate(RenderLivingBase<? extends E> renderer)
    {
        if (renderer.getMainModel() == null || this.shouldModelBeSkipped(renderer.getMainModel()))
            return false;

        this.fetchFields(renderer);

        M model = (M) renderer.getMainModel();
        this.createParts(model);

        // Swapping layers
        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                swapLayer(renderer, i);
            }
        }

        return true;
    }

    public void updateModel(E entity, RenderLivingBase<? extends E> renderer, float partialTicks)
    {
        boolean shouldSit = entity.isRiding()
                && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f = GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float yaw = f1 - f;

        if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
            f = GUtil.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset,
                    partialTicks);
            yaw = f1 - f;
            float f3 = MathHelper.wrapDegrees(yaw);

            if (f3 < -85.0F)
                f3 = -85.0F;
            if (f3 >= 85.0F)
                f3 = 85.0F;

            f = f1 - f3;

            if (f3 * f3 > 2500.0F)
                f += f3 * 0.2F;

            yaw = f1 - f;
        }

        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!entity.isRiding())
        {
            f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
            f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
            yaw = f1 - f;
        }

        this.headYaw = yaw;
        this.headPitch = pitch;
        this.limbSwing = f6;
        this.limbSwingAmount = f5;
        this.swingProgress = entity.getSwingProgress(partialTicks);
    }

    public void performAnimations(D data)
    {
        data.headYaw = MathHelper.wrapDegrees(this.headYaw);
        data.headPitch = MathHelper.wrapDegrees(this.headPitch);
        data.limbSwing = this.limbSwing;
        data.limbSwingAmount = this.limbSwingAmount;
        data.swingProgress = this.swingProgress;
        data.animate();
    }

    public abstract void syncUpWithData(D data);

    /**
     * Called before the first-person hand is drawn through the renderer's model, so the mutator can
     * put it at rest; the next sync with the entity's data undoes it.
     */
    public void poseForFirstPersonView()
    {
    }

    public D getData(E entity)
    {
        return EntityDatabase.instance.get(entity);
    }

    /**
     * Returns true, if this model should skip the mutation process.
     */
    public abstract boolean shouldModelBeSkipped(ModelBase model);

}
