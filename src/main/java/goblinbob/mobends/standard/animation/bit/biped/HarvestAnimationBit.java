package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public class HarvestAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    private static final String[] ACTIONS = new String[] { "harvest" };

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

    public HarvestAnimationBit() {}

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final float headPitch = data.headPitch.get();
        final float headYaw = data.headYaw.get();

        boolean mainHandSwitch = actionHand == EnumHandSide.RIGHT;
        // Side Multiplier - it helps switch animation sides depending on
        // what is your main hand.
        float sideMultiplier = actionHand == EnumHandSide.RIGHT ? 1.0F : -1.0F;
        ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
        ModelPartTransform offArm = mainHandSwitch ? data.leftArm : data.rightArm;
        ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
        ModelPartTransform offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

        data.localOffset.slideToZero(0.3F);
        data.centerRotation.setSmoothness(.3F).orientZero();

        float swingProgress = data.swingProgress.get();
        final float bodyYaw = MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)) * 30.0F * sideMultiplier;
        data.body.rotation.setSmoothness(0.8F).orientY(bodyYaw);

        float bodyPitch = 0;
        if (data.getEntity().isSneaking())
        {
            data.body.rotation.rotateX(20.0F);
            bodyPitch = 20.0F;
        }

        data.head.rotation.setSmoothness(0.8F).orientX(headPitch - bodyPitch)
                .rotateY(headYaw - bodyYaw);

        mainArm.rotation.orientInstantX(MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)) * 50.0F - 30.0F);
        mainArm.rotation.localRotateZ(MathHelper.cos(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)) * -20.0F + 10.0F).finish();
    }

}
