package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.animation.controller.SpiderController;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;

public class SpiderMoveAnimationBit extends AnimationBit<SpiderData>
{

    protected static final float KNEEL_DURATION = 10F;
    protected float startTransition = 0;

    @Override
    public String[] getActions(SpiderData data)
    {
        return new String[] { "move" };
    }

    @Override
    public void onPlay(SpiderData data)
    {
        this.startTransition = 0;
    }

    @Override
    public void perform(SpiderData data)
    {
        final float ticks = DataUpdateHandler.getTicks();

        final float headYaw = data.headYaw.get();
        final float headPitch = data.headPitch.get();
        final float limbSwing = data.limbSwing.get() * 0.6662F;

        float groundLevel = MathHelper.sin(ticks * 0.6F) * 1.2F;
        final float touchdown = Math.min(data.getTicksAfterTouchdown() / KNEEL_DURATION, 1.0F);

        if (startTransition < 1.0F)
            startTransition += DataUpdateHandler.ticksPerFrame * 0.1F;

        if (touchdown < 1.0F)
        {
            float touchdownInv = 1.0F - touchdown;
            groundLevel += Math.sin((touchdown * 1.2F - 0.2F) * Math.PI * 2) * 3.0F * touchdownInv;
        }

        data.spiderHead.rotation.orientInstantX(headPitch);
        data.spiderHead.rotation.rotateY(headYaw).finish();

        final double bodyX = Math.sin(ticks * 0.2F) * 0.4;
        final double bodyZ = Math.cos(ticks * 0.2F) * 0.4;

        // Back limbs
        animateLimb(data, groundLevel, limbSwing + .0F, 0, 20.0F, 10F, -80, -50);
        animateLimb(data, groundLevel, limbSwing + .3F, 1, 20.0F, 10F, -80, -50);

        // Back-middle limbs
        animateLimb(data, groundLevel, limbSwing + .3F, 2, 15F, 15.0F, -30F, 10.0F);
        animateLimb(data, groundLevel, limbSwing + .0F, 3, 15F, 15.0F, -30F, 10.0F);

        // Front-middle limbs
        animateLimb(data, groundLevel, limbSwing + .4F, 4, 7F, 15.0F, 20, 50.0F);
        animateLimb(data, groundLevel, limbSwing + .7F, 5, 7F, 15.0F, 20, 50.0F);

        // Front limbs
        animateLimb(data, groundLevel, limbSwing + .7F, 6, 10F, 20.0F, 60, 80.0F);
        animateLimb(data, groundLevel, limbSwing + .4F, 7, 10F, 20.0F, 60, 80.0F);

        data.renderOffset.set((float) bodyX, (float) -groundLevel, (float) -bodyZ);
        data.renderRotation.orientZero();
    }

    private void animateLimb(SpiderData data, float groundLevel, float limbSwing, int index, float minDist, float maxDist, float minRot, float maxRot)
    {
        final boolean odd = index % 2 == 1;
        final float offset = (index + 1) / 2 % 2 == 0 ? GUtil.PI : 0;
        float smoothness = 1F;
        float sideRotation = minRot + (MathHelper.sin(limbSwing + offset) * .5F + .5F) * (maxRot - minRot);
        float dist = minDist + (MathHelper.sin(limbSwing + offset) * .5F + .5F) * (maxDist - minDist);
        groundLevel += -7 + Math.max(0, MathHelper.cos(limbSwing + offset)) * 4;

        SpiderData.Limb limb = data.limbs[index];
        limb.upperPart.rotation.setSmoothness(smoothness).orientY(odd ? sideRotation : -sideRotation);

        if (startTransition >= 1.0F)
        {
            SpiderController.putLimbOnGround(limb.upperPart.rotation, limb.lowerPart.rotation, odd, dist, groundLevel);
        }
        else
        {
            SpiderController.putLimbOnGround(limb.upperPart.rotation, limb.lowerPart.rotation, odd, dist, groundLevel, startTransition);
        }

        limb.setAngleAndDistance(odd ? sideRotation / 180F * GUtil.PI : GUtil.PI - sideRotation / 180F * GUtil.PI, dist * 0.0625F);
    }

}
