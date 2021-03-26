package goblinbob.mobends.forge.client.event;

import com.mojang.blaze3d.platform.GlStateManager;
import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.core.exceptions.InvalidMutationException;
import goblinbob.mobends.core.kumo.driver.IDriverFunctionProvider;
import goblinbob.mobends.core.mutation.PuppeteerException;
import goblinbob.mobends.forge.EntityData;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{
    IEntityBenderProvider<ForgeMutationContext> benderProvider;
    IDriverFunctionProvider<EntityData> driverFunctionProvider;

    public RenderHandler(IEntityBenderProvider<ForgeMutationContext> benderProvider, IDriverFunctionProvider<EntityData> driverFunctionProvider)
    {
        this.benderProvider = benderProvider;
        this.driverFunctionProvider = driverFunctionProvider;
    }

    @SubscribeEvent
    public void beforeLivingRendered(RenderLivingEvent.Pre<?, ?> event)
    {
        final LivingEntity living = event.getEntity();

        final EntityBender<ForgeMutationContext> bender = this.benderProvider.getBenderForEntity(living);
        if (bender == null)
            return;

        GlStateManager.pushMatrix();

        final LivingRenderer<?, ?> renderer = event.getRenderer();
        final float pt = event.getPartialRenderTick();
        float ticksPassed = living.ticksExisted + pt;
        ForgeMutationContext context = new ForgeMutationContext(this.driverFunctionProvider, living, renderer, pt, ticksPassed);

        try
        {
            bender.beforeRender(context);
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

        final EntityBender<ForgeMutationContext> bender = benderProvider.getBenderForEntity(living);
        if (bender == null)
            return;

        GlStateManager.popMatrix();
    }
}
