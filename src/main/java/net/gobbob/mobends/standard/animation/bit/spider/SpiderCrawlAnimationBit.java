package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;

public class SpiderCrawlAnimationBit extends SpiderAnimationBitBase
{

    protected static final String[] ACTIONS = new String[] { "crawl" };

    @Override
    public String[] getActions(SpiderData entityData)
    {
        return ACTIONS;
    }

    @Override
    public void perform(SpiderData data)
    {
        final float ticks = DataUpdateHandler.getTicks();
        final float pt = DataUpdateHandler.partialTicks;
        final EntitySpider spider = data.getEntity();

        final float headYaw = data.headYaw.get();
        final float headPitch = data.headPitch.get();
        final float limbSwing = data.getCrawlProgress() * 5.0F;

        float groundLevel = MathHelper.sin(limbSwing * 0.6F) * 1.2F;

        if (startTransition < 1.0F)
            startTransition += DataUpdateHandler.ticksPerFrame * 0.1F;

        data.spiderHead.rotation.orientInstantX(headPitch);
        data.spiderHead.rotation.rotateY(headYaw).finish();

        final double bodyX = Math.sin(ticks * 0.2F) * 0.4;
        final double bodyZ = Math.cos(ticks * 0.2F) * 0.4;

        // Back limbs
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 0, 20.0F, 10F, -80, -50);
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 1, 20.0F, 10F, -80, -50);

        // Back-middle limbs
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 2, 15F, 15.0F, -30F, 10.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 3, 15F, 15.0F, -30F, 10.0F);

        // Front-middle limbs
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 4, 7F, 15.0F, 20, 50.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 5, 7F, 15.0F, 20, 50.0F);

        // Front limbs
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 6, 10F, 20.0F, 60, 80.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 7, 10F, 20.0F, 60, 80.0F);

        final float climbingRotation = data.getCrawlingRotation();
        final float yaw = spider.prevRotationYaw + (spider.rotationYaw - spider.prevRotationYaw) * pt;
        final float renderRotationY = MathHelper.wrapDegrees(yaw - climbingRotation);
        data.renderRotation.orientX(-90F);
        data.renderRotation.setSmoothness(.6F).rotateY(renderRotationY);

        data.localOffset.slideTo((float) 0, -10.0F, (float) 0, 0.5F);
        data.centerRotation.orientZero();
    }

}
