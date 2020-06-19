package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.math.MathHelper;

public class ElytraAnimationBit extends AnimationBit<PlayerData>
{

    private static final String[] ACTIONS = new String[] { "elytra" };
    private static final float PI = (float) Math.PI;

    @Override
    public String[] getActions(PlayerData entityData)
    {
        return ACTIONS;
    }

    @Override
    public void perform(PlayerData data)
    {
        final AbstractClientPlayer player = data.getEntity();

        final double magnitude = data.getInterpolatedMotionMagnitude();

        float headPitch = MathHelper.wrapDegrees(data.headPitch.get());
        float headYaw = MathHelper.wrapDegrees(data.headYaw.get());
        float headYawAbs = MathHelper.abs(headYaw);

        float speedFactor = MathHelper.clamp((float) magnitude, 0.0F, 0.2F) / 0.2F;

        // Full Speed

        data.head.rotation.setSmoothness(1.0F).orientY(headYaw).rotateX(-90.0F);
        data.body.rotation.setSmoothness(0.7F).orientX(0);
        data.leftArm.rotation.setSmoothness(0.7F).orientX(0).rotateZ(-60F + 55F * speedFactor - headYawAbs * 0.5F);
        data.rightArm.rotation.setSmoothness(0.7F).orientX(0).rotateZ(60F - 55F * speedFactor + headYawAbs * 0.5F);
        data.leftForeArm.rotation.setSmoothness(.7F).orientZero();
        data.rightForeArm.rotation.setSmoothness(.7F).orientZero();
        data.leftLeg.rotation.setSmoothness(0.7F).orientZ(-5.0F);
        data.rightLeg.rotation.setSmoothness(0.7F).orientZ(5.0F);
        data.leftForeLeg.rotation.setSmoothness(0.7F).orientX(0.0F);
        data.rightForeLeg.rotation.setSmoothness(0.7F).orientX(0.0F);

        data.centerRotation.setSmoothness(1.0F).orientZero();
        data.renderRotation.setSmoothness(.7F).orientX(0);
        data.globalOffset.slideToZero(.7F);
    }

}
