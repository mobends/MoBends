package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.PoseMath;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * The player's cape: the vanilla chasing-position physics of {@code CapeAnimationBit}, as a
 * driver ({@code "driver": "mobends:cape"}). Writes the cape rotation and the cape wave speed.
 */
public class CapeDriver implements IPoseItem
{

    private final int slot;
    private final Quaternion rotation = new Quaternion();
    private final Quaternion temp = new Quaternion();

    public CapeDriver(int slot)
    {
        this.slot = slot;
    }

    public static IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, Template template) throws MalformedKumoTemplateException
    {
        return new CapeDriver(skeleton.indexOf(template.bone == null ? "cape" : template.bone));
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException
    {
        if (!(context.getSubject() instanceof PlayerData))
        {
            return;
        }
        PlayerData data = (PlayerData) context.getSubject();
        AbstractClientPlayer player = data.getEntity();

        final double partialTicks = DataUpdateHandler.partialTicks;
        double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * partialTicks);
        double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * partialTicks);
        double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
        double f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = Math.sin(f * 0.017453292);
        double d4 = -Math.cos(f * 0.017453292);
        double f1 = d1 * 10.0;
        f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

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
            PoseMath.axisAngleDegrees(1, 0, 0, 0.0F, rotation);
            data.setCapeWaveSpeed(4.0F);
        }
        else
        {
            // orientX(6 + f2/2 + f1).rotateZ(f3/2).rotateY(-f3/2)
            PoseMath.axisAngleDegrees(1, 0, 0, (float) (6.0F + f2 / 2.0F + f1), rotation);
            PoseMath.axisAngleDegrees(0, 0, 1, f3 / 2.0F, temp);
            Quaternion.mul(temp, rotation, rotation);
            PoseMath.axisAngleDegrees(0, 1, 0, -f3 / 2.0F, temp);
            Quaternion.mul(temp, rotation, rotation);
            data.setCapeWaveSpeed(1.0F);
        }

        pose.composeRotation(slot, rotation, Pose.Space.OVERRIDE);
        pose.get(slot).smoothness = 0.5F;
    }

    @Override
    public boolean isFinished(float elapsedTicks)
    {
        return false;
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

    public static class Template extends DriverItemTemplate
    {
        public String bone;
    }

}
