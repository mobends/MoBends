package goblinbob.mobends.standard.client.event;

import goblinbob.mobends.core.client.RendererState;
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
    public void beforeHandRender(RenderHandEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        Entity viewEntity = mc.getRenderViewEntity();

        if (!(viewEntity instanceof AbstractClientPlayer))
            return;

        // Other mods (e.g. Vivecraft) can draw players with a renderer that isn't a RenderPlayer.
        Render<AbstractClientPlayer> render = mc.getRenderManager().getEntityRenderObject(viewEntity);
        if (!(render instanceof RenderLivingBase))
            return;

        // The first-person hand is never bent: vanilla draws it from the renderer's model, which may
        // still hold a mutation (the local player's, or another player's). The next render of the
        // renderer puts the right one back.
        RendererState.restoreVanilla((RenderLivingBase<?>) render);
    }

}
