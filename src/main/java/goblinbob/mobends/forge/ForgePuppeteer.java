package goblinbob.mobends.forge;

import goblinbob.mobends.core.*;
import goblinbob.mobends.core.data.PropertyStorage;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.exceptions.UnknownPropertyException;
import goblinbob.mobends.core.mutation.PuppeteerException;
import goblinbob.bendslib.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;

public class ForgePuppeteer<D extends EntityData, E extends LivingEntity, M extends EntityModel<E>> implements IPuppeteer<ForgeMutationContext>, IDisposable
{
    private final EntityBender<ForgeMutationContext, BenderResources> bender;
    private final LivingRenderer<E, M> renderer;
    private final M model;
    private final IEntityDataRepository<ForgeMutationContext, BenderResources> dataRepository;

    protected List<LayerRenderer<E, M>> layerRenderers;
    private Mutator mutator;

    private ForgePuppeteer(EntityBender<ForgeMutationContext, BenderResources> bender, LivingRenderer<E, M> renderer, IEntityDataRepository<ForgeMutationContext, BenderResources> dataRepository)
    {
        this.bender = bender;
        this.renderer = renderer;
        this.model = renderer.getModel();
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
        float ticksPassed = context.getTicksPassed();

        //noinspection unchecked
        D data = (D) this.dataRepository.getOrMakeData(context, bender);
        //noinspection unchecked
        this.updateModel((E) entity, data, partialTicks);
        this.performAnimations(data, ticksPassed);
        this.syncUpWithData(data);
    }

    @Override
    public void beforeRender(ForgeMutationContext context)
    {
        //noinspection unchecked
        D data = (D) this.dataRepository.getOrMakeData(context, bender);
        Entity entity = context.getEntity();
        float partialTicks = context.getPartialTicks();

        double entityX = entity.xOld + (entity.getX() - entity.xOld) * partialTicks;
        double entityY = entity.yOld + (entity.getY() - entity.yOld) * partialTicks;
        double entityZ = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks;

        Entity viewEntity = Minecraft.getInstance().getCameraEntity();
        double viewX = entityX, viewY = entityY, viewZ = entityZ;
        if (viewEntity != null)
        {
            // Checking in case of Main Menu or GUI rendering.
            viewX = viewEntity.xOld + (viewEntity.getX() - viewEntity.xOld) * partialTicks;
            viewY = viewEntity.yOld + (viewEntity.getY() - viewEntity.yOld) * partialTicks;
            viewZ = viewEntity.zOld + (viewEntity.getZ() - viewEntity.zOld) * partialTicks;
        }

//        GlStateManager.translated(entityX - viewX, entityY - viewY, entityZ - viewZ);
//        GlStateManager.rotatef(-GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0F, 1F, 0F);

//        this.renderLocalAccessories(entity, data, partialTicks);

//        float globalScale = entity.isChild() ? getChildScale() : 1;

//        GlStateManager.translate(data.globalOffset.getX() * scale * globalScale,
//                data.globalOffset.getY() * scale * globalScale,
//                data.globalOffset.getZ() * scale * globalScale);
//        GlStateManager.translate(0, entity.height / 2, 0);
//        TransformUtils.rotate(data.centerRotation.getSmooth());
//        GlStateManager.translate(0, -entity.height / 2, 0);
//        TransformUtils.rotate(data.renderRotation.getSmooth());
//
//        GlStateManager.translate(data.localOffset.getX() * scale * globalScale,
//                data.localOffset.getY() * scale * globalScale,
//                data.localOffset.getZ() * scale * globalScale);

//        this.transformLocally(entity, data, partialTicks);

//        GlStateManager.rotatef(GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), 0F, 1F, 0F);
//        GlStateManager.translated(viewX - entityX, viewY - entityY, viewZ - entityZ);
    }

    private void updateModel(E entity, D data, float partialTicks) throws PuppeteerException
    {
        // The code below is a mirror of what in the LivingRenderer's render method.

        boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        float f = MathHelper.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = MathHelper.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float yaw = f1 - f;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity)
        {
            LivingEntity livingentity = (LivingEntity) entity.getVehicle();
            f = GUtil.interpolateRotation(livingentity.yBodyRotO, livingentity.yBodyRot,
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

        float pitch = MathHelper.lerp(partialTicks, entity.xRotO, entity.xRot);

        float f5 = 0.0F;
        float f6 = 0.0F;

        if (!shouldSit && entity.isAlive())
        {
            f5 = MathHelper.lerp(partialTicks, entity.animationSpeedOld, entity.animationSpeed);
            f6 = entity.animationPosition - entity.animationSpeed * (1.0F - partialTicks);

            if (entity.isBaby())
                f6 *= 3.0F;
            if (f5 > 1.0F)
                f5 = 1.0F;
        }

        try
        {
            PropertyStorage storage = data.getPropertyStorage();
            storage.setProperty(BasePropertyKeys.LIFETIME, entity.tickCount + partialTicks);
            storage.setProperty(BasePropertyKeys.HEAD_YAW, yaw);
            storage.setProperty(BasePropertyKeys.HEAD_PITCH, pitch);
            storage.setProperty(BasePropertyKeys.LIMB_SWING, f6);
            storage.setProperty(BasePropertyKeys.LIMB_SWING_AMOUNT, f5);
            storage.setProperty(BasePropertyKeys.SWING_PROGRESS, entity.getAttackAnim(partialTicks));
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

    private void performAnimations(D data, float ticksPassed)
    {
        // TODO Perform animations!

        data.getAnimatorState().update(data, ticksPassed);
    }

    /**
     * Applying the virtual animation data to the actual entity model.
     *
     * @param data
     */
    private void syncUpWithData(EntityData data)
    {
        for (Map.Entry<String, IForgeModelPart> entry : mutator.getParts())
        {
            IModelPart src = data.getPartForName(entry.getKey());
            entry.getValue().syncUp(src);
        }
    }

    /**
     * Returns true, if this model should skip the mutation process.
     */
    public boolean shouldModelBeSkipped()
    {
        return false;
    }

    public static <D extends EntityData, E extends LivingEntity, M extends EntityModel<E>> ForgePuppeteer<D, E, M> create(ForgeMutationContext context, EntityBender<ForgeMutationContext, BenderResources> bender, IEntityDataRepository<ForgeMutationContext, BenderResources> dataRepository) throws InvalidMutationException
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
