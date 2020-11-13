package goblinbob.mobends.standard.main;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{

    public RenderHandler()
    {
    }

    @SubscribeEvent
    public void beforeLivingRendered(RenderLivingEvent.Pre<?, ?> event)
    {
//        final LivingEntity living = event.getEntity();
//        final EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(living);

//        if (entityBender == null)
//            return;

//        final LivingRenderer<?, ?> renderer = event.getRenderer();
//        final float pt = event.getPartialRenderTick();

        GlStateManager.pushMatrix();
    }

    @SubscribeEvent
    public void afterLivingRendered(RenderLivingEvent.Post<?, ?> event)
    {
        GlStateManager.popMatrix();
    }

}
