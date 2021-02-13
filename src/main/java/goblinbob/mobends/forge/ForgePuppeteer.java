package goblinbob.mobends.forge;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IDisposable;
import goblinbob.mobends.core.IEntityDataRepository;
import goblinbob.mobends.core.IPuppeteer;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.exceptions.UnknownPropertyException;
import goblinbob.mobends.core.mutation.PuppeteerException;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ForgePuppeteer<D extends EntityData, E extends LivingEntity, M extends EntityModel<E>> implements IPuppeteer<ForgeMutationContext>, IDisposable
{
    private final EntityBender<ForgeMutationContext> bender;
    private final LivingRenderer<E, M> renderer;
    private final M model;
    private final IEntityDataRepository<ForgeMutationContext> dataRepository;

    protected List<LayerRenderer<E, M>> layerRenderers;
    private Mutator mutator;

    private ForgePuppeteer(EntityBender<ForgeMutationContext> bender, LivingRenderer<E, M> renderer, IEntityDataRepository<ForgeMutationContext> dataRepository)
    {
        this.bender = bender;
        this.renderer = renderer;
        this.model = renderer.getEntityModel();
        this.dataRepository = dataRepository;

        this.mutator = new Mutator(bender, model);
    }

    @Override
    public void dispose()
    {
        this.mutator.demutate();
    }

    @Override
    public void perform(ForgeMutationContext context) throws PuppeteerException
    {
        LivingEntity entity = context.getEntity();
        float partialTicks = context.getPartialTicks();

        //noinspection unchecked
        D data = (D) this.dataRepository.getOrMakeData(context);
        //noinspection unchecked
        this.updateModel((E) entity, data, partialTicks);
        this.performAnimations(data, partialTicks);
        this.syncUpWithData(data);
    }

    private void updateModel(E entity, D data, float partialTicks) throws PuppeteerException
    {
        // The code below is a mirror of what in the LivingRenderer's render method.

        boolean shouldSit = entity.isPassenger() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
        float f = GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
        float f1 = GUtil.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
        float yaw = f1 - f;

        if (shouldSit && entity.getRidingEntity() instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity) entity.getRidingEntity();
            f = GUtil.interpolateRotation(livingentity.prevRenderYawOffset, livingentity.renderYawOffset,
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

        float pitch = MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch);

        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!shouldSit && entity.isAlive())
        {
            f5 = MathHelper.lerp(partialTicks, entity.prevLimbSwingAmount, entity.limbSwingAmount);
            f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

            if (entity.isChild())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
        }

        try
        {
            data.setProperty(BasePropertyKeys.HEAD_YAW, yaw);
            data.setProperty(BasePropertyKeys.HEAD_PITCH, pitch);
            data.setProperty(BasePropertyKeys.LIMB_SWING, f6);
            data.setProperty(BasePropertyKeys.LIMB_SWING_AMOUNT, f5);
            data.setProperty(BasePropertyKeys.SWING_PROGRESS, entity.getSwingProgress(partialTicks));
        }
        catch (UnknownPropertyException e)
        {
            throw new PuppeteerException(e.getMessage(), e);
        }
    }

    private void mutate() throws InvalidMutationException
    {
        this.mutator.mutate();
    }

    private void performAnimations(D data, float partialTicks)
    {
        // TODO Perform animations!
        
    }

    /**
     * Applying the virtual animation data to the actual entity.
     *
     * @param data
     */
    private void syncUpWithData(EntityData data)
    {
        // TODO Implement this
    }

    /**
     * Returns true, if this model should skip the mutation process.
     */
    public boolean shouldModelBeSkipped()
    {
        return false;
    }

    public static <D extends EntityData, E extends LivingEntity, M extends EntityModel<E>> ForgePuppeteer<D, E, M> create(ForgeMutationContext context, EntityBender<ForgeMutationContext> bender, IEntityDataRepository<ForgeMutationContext> dataRepository) throws InvalidMutationException
    {
        //noinspection unchecked
        ForgePuppeteer<D, E, M> puppeteer = new ForgePuppeteer<>(bender, (LivingRenderer<E, M>) context.getRenderer(), dataRepository);

        if (puppeteer.shouldModelBeSkipped())
        {
            return null;
        }

        puppeteer.mutate();

        return puppeteer;
    }

}
