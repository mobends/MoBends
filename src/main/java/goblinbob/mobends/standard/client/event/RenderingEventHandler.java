package goblinbob.mobends.standard.client.event;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.RendererState;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
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

        AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;

        RenderPlayer renderPlayer = (RenderPlayer) mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(player);
        EntityBender<AbstractClientPlayer> bender = EntityBenderRegistry.instance.getForEntity(player);

        LivingEntityData<?> data = EntityDatabase.instance.get(player);
        if (bender == null || !bender.isAnimated() || (data != null && data.wantsVanilla()))
        {
            // The hand is drawn from the renderer's model, which may hold another player's mutation (or vanilla is asked for).
            RendererState.restoreVanilla(renderPlayer);
            return;
        }

        // Stays in place for the hand; the next render of the renderer puts the right model back.
        Mutator<?, ?, ?> mutator = bender.attachMutation(renderPlayer);
        if (mutator != null)
            mutator.poseForFirstPersonView();
    }

}
