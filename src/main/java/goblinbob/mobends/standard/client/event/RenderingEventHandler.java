package goblinbob.mobends.standard.client.event;

import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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

        if (!BenderHelper.isEntityAnimated(player))
        	return;

        RenderPlayer renderPlayer = (RenderPlayer) mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(player);
        PlayerMutator mutator = (PlayerMutator) BenderHelper.getMutatorForRenderer(AbstractClientPlayer.class, renderPlayer);
        if (mutator != null)
            mutator.poseForFirstPersonView();
    }

}
