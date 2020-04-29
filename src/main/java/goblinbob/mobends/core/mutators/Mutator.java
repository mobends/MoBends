package goblinbob.mobends.core.mutators;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.pack.BendsPackPerformer;
import goblinbob.mobends.core.pack.variable.BendsVariableRegistry;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.List;

public abstract class Mutator<D extends LivingEntityData<E>, E extends EntityLivingBase, M extends ModelBase>
{

    protected M vanillaModel;
    protected float headYaw;
    protected float headPitch;
    protected float limbSwing;
    protected float limbSwingAmount;

    private IEntityDataFactory<E> dataFactory;
    protected List<LayerRenderer<?>> layerRenderers;

    public Mutator(IEntityDataFactory<E> dataFactory)
    {
        this.dataFactory = dataFactory;
    }

    /**
     * Used to fetch private data from the original
     * renderer.
     */
    public void fetchFields(RenderLivingBase<? extends E> renderer)
    {
        // Getting the layer renderers
        this.layerRenderers = (List<LayerRenderer<?>>) ((Object) renderer.layerRenderers); // Type safety hack...
    }

    public abstract void storeVanillaModel(M model);

    /**
     * Sets the model parameter back to it's vanilla
     * state. Used to demutate the model.
     */
    public abstract void applyVanillaModel(M model);

    /**
     * Swaps out the vanilla layers for their custom counterparts,
     * and if it's a vanilla model, it stores the vanilla layers
     * for future mutation reversal.
     */
    public abstract void swapLayer(RenderLivingBase<? extends E> renderer, int index, boolean isModelVanilla);

    /**
     * Swaps the custom layers back with the vanilla layers.
     * Used to demutate the model.
     */
    public abstract void deswapLayer(RenderLivingBase<? extends E> renderer, int index);

    /**
     * Creates all the custom parts you need! It swaps all the
     * original parts with newly created custom parts.
     */
    public abstract boolean createParts(M original, float scaleFactor);

    public boolean mutate(RenderLivingBase<? extends E> renderer)
    {
        if (renderer.getMainModel() == null || this.shouldModelBeSkipped(renderer.getMainModel()))
            return false;

        this.fetchFields(renderer);

        M model = (M) renderer.getMainModel();
        float scaleFactor = 0F;

        boolean isModelVanilla = this.isModelVanilla(model);
        if (isModelVanilla)
        {
            // If this model wasn't mutated before, save it as the vanilla model.
            this.storeVanillaModel(model);
        }

        this.createParts(model, scaleFactor);

        // Swapping layers
        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                swapLayer(renderer, i, isModelVanilla);
            }
        }

        return true;
    }

    /**
     * Performs the steps needed to demutate the model.
     */
    public void demutate(RenderLivingBase<? extends E> renderer)
    {
        if (this.shouldModelBeSkipped(renderer.getMainModel()))
            return;

        M model = (M) renderer.getMainModel();

        this.applyVanillaModel(model);

        if (this.layerRenderers != null)
        {
            for (int i = 0; i < layerRenderers.size(); ++i)
            {
                this.deswapLayer(renderer, i);
            }
        }
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
    }

    public void performAnimations(D data, String animatedEntityKey, RenderLivingBase<? extends E> renderer, float partialTicks)
    {
        data.headYaw.set(this.headYaw);
        data.headPitch.set(this.headPitch);
        data.limbSwing.set(this.limbSwing);
        data.limbSwingAmount.set(this.limbSwingAmount);

        BendsVariableRegistry.instance.provideTemporaryData(data);

        // noinspection unchecked
        final IAnimationController<D> controller = (IAnimationController<D>) data.getController();
        final Collection<String> actions = controller.perform(data);

        SmoothVector3f lastGlobalOffset = new SmoothVector3f(data.globalOffset);
        SmoothVector3f lastLocalOffset = new SmoothVector3f(data.localOffset);
        if (NetworkConfiguration.instance.areBendsPacksAllowed())
        {
            BendsPackPerformer.INSTANCE.performCurrentPack(data, animatedEntityKey, actions);

            if (NetworkConfiguration.instance.isMovementLimited())
            {
                // Limit movement
                data.globalOffset.limitDistanceTo(lastGlobalOffset, 10F);
                data.localOffset.limitDistanceTo(lastLocalOffset, 10F);
            }
        }
    }

    public abstract void syncUpWithData(D data);

    public D getData(E entity)
    {
        return EntityDatabase.instance.get(entity);
    }

    public D getOrMakeData(E entity)
    {
        return EntityDatabase.instance.getOrMake(dataFactory, entity);
    }

    /**
     * True, if this renderer wasn't mutated before.
     */
    public abstract boolean isModelVanilla(M model);

    /**
     * Returns true, if this model should skip the mutation process.
     */
    public abstract boolean shouldModelBeSkipped(ModelBase model);

    /**
     * Called right after this mutator has been refreshed.
     */
    public void postRefresh()
    {
        // No default behaviour
    }

}
