package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class CapeAnimationBit extends AnimationBit<PlayerData>
{

    @Override
    public String[] getActions(PlayerData entityData)
    {
        return null;
    }

    @Override
    public void perform(PlayerData data)
    {
        final AbstractClientPlayer player = data.getEntity();

        data.cape.rotation.orientX(0.0F);

        final double partialTicks = DataUpdateHandler.partialTicks;
        double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
        double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
        double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
        double f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = Math.sin(f * 0.017453292);
        double d4 = -Math.cos(f * 0.017453292);
        double f1 = d1 * 10.0;
        f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }

        double f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        f1 = f1 + Math.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

        if (player.isSneaking())
        {
            f1 += 25.0F;
        }

        if (data.isFlying() && player.isSprinting())
        {
            data.cape.rotation.setSmoothness(0.5F).orientX(0.0F);

            data.setCapeWaveSpeed(4.0F);
        }
        else
        {
            data.cape.rotation.setSmoothness(0.5F).orientX((float) (6.0F + f2 / 2.0F + f1));
            data.cape.rotation.rotateZ(f3 / 2.0F);
            data.cape.rotation.rotateY(-f3 / 2.0F);

            data.setCapeWaveSpeed(1.0F);
        }
    }

}
