package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.EnumHandSide;

public class ShieldAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "shield" };

    protected final EnumHandSide actionHand;

    protected float bringUpAnimation;

    public ShieldAnimationBit(EnumHandSide handSide)
    {
        this.actionHand = handSide;
    }

    @Override
    public String[] getActions(BipedEntityData<?> data)
    {
        return ACTIONS;
    }

    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        bringUpAnimation = 0F;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final boolean mainHandSwitch = this.actionHand == EnumHandSide.RIGHT;
        // Main Hand Direction Multiplier - it helps switch animation sides depending on
        // what is your main hand.
        final float handDirMtp = mainHandSwitch ? 1 : -1;
        final ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;

        if (bringUpAnimation < 1F)
        {
            bringUpAnimation += DataUpdateHandler.ticksPerFrame * 0.7F;
            bringUpAnimation = Math.min(bringUpAnimation, 1F);
        }

        mainArm.rotation.orientX(bringUpAnimation * 0.0F)
                        .rotateY(-45.0F * bringUpAnimation * handDirMtp);
                        //.rotateZ(45.0F * bringUpAnimation * handDirMtp);

        mainForeArm.rotation.orientX(bringUpAnimation * -45.0F);
    }

}
