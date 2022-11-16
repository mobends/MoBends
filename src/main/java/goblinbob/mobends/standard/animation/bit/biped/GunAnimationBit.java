package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class GunAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "bow" };

    protected EnumHandSide actionHand = EnumHandSide.RIGHT;

    @Override
    public String[] getActions(BipedEntityData<?> entityData)
    {
        return ACTIONS;
    }

    public void setActionHand(EnumHandSide handSide)
    {
        this.actionHand = handSide;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        data.localOffset.slideToZero(0.3F);

        final EntityLivingBase living = data.getEntity();
        final float headPitch = data.headPitch.get();
        final float headYaw = data.headYaw.get();

        boolean mainHandSwitch = this.actionHand == EnumHandSide.RIGHT;
        // Main Hand Direction Multiplier - it helps switch animation sides depending on
        // what is your main hand.
        float handDirMtp = mainHandSwitch ? 1 : -1;
        ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
        ModelPartTransform offArm = mainHandSwitch ? data.leftArm : data.rightArm;
        ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
        ModelPartTransform offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

        int aimedBowDuration = living != null ? Math.min(living.getItemInUseMaxCount(), 15) : 0;

        float bodyTwistY = (((aimedBowDuration - 10) / 5.0f) * -25) * handDirMtp;
        float var2 = (aimedBowDuration / 10.0f);
        float var5 = headPitch - 90F;
        var5 = Math.max(var5, -160);
        //if(living.getHeldItem(EnumHand.MAIN_HAND).getItem().getCreatorModId() == "");

        float bodyRotationY = -bodyTwistY + headYaw;
        if (data.isClimbing())
        {
            float climbingRotation = data.getClimbingRotation();
            float renderRotationY = MathHelper.wrapDegrees(living.rotationYaw - headYaw - climbingRotation);
            bodyRotationY = MathHelper.wrapDegrees(headYaw + renderRotationY);

            data.head.rotation.setSmoothness(0.5F).orientX(headPitch);
        }
        else
        {
            data.head.rotation.setSmoothness(0.5F).orientX(headPitch)
                    .rotateY(headYaw - bodyRotationY);
        }

        data.body.rotation.setSmoothness(.8F).orientY(bodyRotationY);

        mainArm.rotation.setSmoothness(.8F).orientX(headPitch - 90F)
                .rotateY(bodyTwistY);
        offArm.rotation.setSmoothness(1F).orientY(handDirMtp).orientX(headPitch - 90F)
                .rotateY(bodyTwistY);

        mainForeArm.rotation.setSmoothness(1F).orientX(0);
        offForeArm.rotation.orientX(var2 * -30F);
    }
}

