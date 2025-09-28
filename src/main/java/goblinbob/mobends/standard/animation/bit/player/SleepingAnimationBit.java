package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.util.math.MathHelper;

public class SleepingAnimationBit extends AnimationBit<PlayerData>
{

    private static final String[] ACTIONS = new String[] { "sleeping" };

    @Override
    public String[] getActions(PlayerData entityData)
    {
        return ACTIONS;
    }

    @Override
    public void perform(PlayerData data)
    {
        data.root.offset.slideToZero(0.3F);
        data.root.rotation.setSmoothness(.3F).orientZero();
        data.rightHeldItem.rotation.setSmoothness(.3F).orientZero();
        data.leftHeldItem.rotation.setSmoothness(.3F).orientZero();

        data.rightLeg.rotation.orient(0F, 1F, 0F, 0F);
        data.rightLeg.rotation.rotate(2F, 0F, 0F, 1F);
        data.rightLeg.rotation.rotate(5, 0F, 1F, 0F);
        data.leftLeg.rotation.orient(0F, 1F, 0F, 0F);
        data.leftLeg.rotation.rotate(-2F, 0F, 0F, 1F);
        data.leftLeg.rotation.rotate(-5, 0F, 1F, 0F);
        data.rightShin.rotation.orient(4F, 1F, 0F, 0F);
        data.leftShin.rotation.orient(4F, 1F, 0F, 0F);
        data.rightForearm.rotation.orient(-4.0F, 1F, 0F, 0F);
        data.leftForearm.rotation.orient(-4.0F, 1F, 0F, 0F);

        final float PI = (float) Math.PI;
        float phase = DataUpdateHandler.getTicks() / 10;
        data.head.rotation.setSmoothness(1.0F).orientX(((MathHelper.cos(phase) - 1) / 2) * -3);
        data.rightArm.rotation.setSmoothness(0.4F).orientX(0.0F)
                .rotateZ(2.5F);
        data.leftArm.rotation.setSmoothness(0.4F).orientX(0.0F)
                .rotateZ(-2.5F);
    }

}
