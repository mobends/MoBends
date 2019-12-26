package net.gobbob.mobends.standard.animation.bit.biped;

import net.gobbob.mobends.core.animation.bit.KeyframeAnimationBit;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraft.util.ResourceLocation;

public class BreakingAnimationBit extends KeyframeAnimationBit<BipedEntityData<?>>
{

    private static final String[] ACTIONS = new String[] { "breaking" };
    private static final ResourceLocation HARVEST_ANIMATION = new ResourceLocation(ModStatics.MODID, "animations/player_anim_harvest.json");

    @Override
    public String[] getActions(BipedEntityData<?> entityData)
    {
        return ACTIONS;
    }

    public BreakingAnimationBit(float animationSpeed)
    {
        super(HARVEST_ANIMATION, animationSpeed);
        this.performedAnimation.mirrorRotationYZ("body");
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        super.perform(data);

        data.localOffset.slideToZero(0.3F);
        data.centerRotation.setSmoothness(.3F).orientZero();
        data.body.rotation.rotateX(data.headPitch.get() * 0.5F).finish();
        data.head.rotation.rotateX(-data.headPitch.get() * 0.5F);
    }

}
