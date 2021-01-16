package goblinbob.mobends.standard.main;

import com.mojang.blaze3d.platform.GlStateManager;
import goblinbob.mobends.core.EntityBender;
import goblinbob.mobends.core.IEntityBenderProvider;
import goblinbob.mobends.forge.ForgeMutationContext;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{

    IEntityBenderProvider<ForgeMutationContext> benderProvider;

    public RenderHandler(IEntityBenderProvider<ForgeMutationContext> benderProvider)
    {
        this.benderProvider = benderProvider;
    }

    @SubscribeEvent
    public void beforeLivingRendered(RenderLivingEvent.Pre<?, ?> event)
    {
        final LivingEntity living = event.getEntity();

        final EntityBender<ForgeMutationContext> bender = this.benderProvider.getBenderForEntity(living);
        if (bender == null)
            return;

        final LivingRenderer<?, ?> renderer = event.getRenderer();
        final float pt = event.getPartialRenderTick();

        GlStateManager.pushMatrix();

        ForgeMutationContext context = new ForgeMutationContext(living, renderer, pt);
        bender.applyMutation(context);
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
