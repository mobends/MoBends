package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.kumo.driver.IDriverFunctionProvider;
import goblinbob.mobends.core.mutation.PuppeteerException;
import goblinbob.mobends.forge.BenderResources;
import goblinbob.mobends.forge.EntityData;
import goblinbob.mobends.forge.ForgeMutationContext;
import goblinbob.mobends.forge.config.IClientConfigProvider;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{
    IEntityBenderProvider<ForgeMutationContext, BenderResources> benderProvider;
    IDriverFunctionProvider<EntityData> driverFunctionProvider;
    IClientConfigProvider clientConfigProvider;

    public RenderHandler(
            IEntityBenderProvider<ForgeMutationContext, BenderResources> benderProvider,
            IDriverFunctionProvider<EntityData> driverFunctionProvider,
            IClientConfigProvider clientConfigProvider
    )
    {
        this.benderProvider = benderProvider;
        this.driverFunctionProvider = driverFunctionProvider;
        this.clientConfigProvider = clientConfigProvider;
    }

    @SubscribeEvent
    public void beforeLivingRendered(RenderLivingEvent.Pre<?, ?> event)
    {
        final LivingEntity living = event.getEntity();

        EntityType<?> type = living.getType();
        ResourceLocation registryName = type.getRegistryName();

        final EntityBender<ForgeMutationContext, BenderResources> bender = this.benderProvider.getBenderForEntity(living);
        if (bender == null)
            return;

        event.getMatrixStack().pushPose();

        final LivingRenderer<?, ?> renderer = event.getRenderer();
        final float pt = event.getPartialRenderTick();
        float ticksPassed = living.tickCount + pt;
        ForgeMutationContext context = new ForgeMutationContext(this.driverFunctionProvider, living, renderer, pt, ticksPassed);

        try
        {
            bender.beforeRender(context, !clientConfigProvider.isTargetDisabled(registryName));
        }
        catch (InvalidMutationException | PuppeteerException e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void afterLivingRendered(RenderLivingEvent.Post<?, ?> event)
    {
        final LivingEntity living = event.getEntity();

        final EntityBender<ForgeMutationContext, BenderResources> bender = benderProvider.getBenderForEntity(living);
        if (bender == null)
            return;

        event.getMatrixStack().popPose();
    }
}
