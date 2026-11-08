package goblinbob.mobends.standard.client.event;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderingEventHandler
{

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void beforeHandRender(RenderHandEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewEntity = mc.getRenderViewEntity();

        if (!(viewEntity instanceof AbstractClientPlayer))
            return;

        AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;

        // Other mods (e.g. Vivecraft) can draw players with a renderer that isn't a RenderPlayer.
        Render<AbstractClientPlayer> render = mc.getRenderManager().getEntityRenderObject(player);
        if (!(render instanceof RenderLivingBase))
            return;

        RenderLivingBase<AbstractClientPlayer> renderer = (RenderLivingBase<AbstractClientPlayer>) render;
        EntityBender<AbstractClientPlayer> bender = EntityBenderRegistry.instance.getForEntity(player);

        LivingEntityData<?> data = EntityDatabase.instance.get(player);
        if (bender == null || !bender.isAnimated() || (data != null && data.wantsVanilla()))
        {
            // The hand is drawn from the renderer's model, which may hold another player's mutation (or vanilla is asked for).
            RendererState.restoreVanilla(renderer);
            return;
        }

        // Stays in place for the hand; the next render of the renderer puts the right model back.
        Mutator<?, ?, ?> mutator = bender.attachMutation(renderer);
        if (mutator != null)
            mutator.poseForFirstPersonView();
    }

}
