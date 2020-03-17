package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.SpiderData;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.animation.controller.SpiderController;
import net.minecraft.util.math.MathHelper;

public abstract class SpiderAnimationBitBase extends AnimationBit<SpiderData>
{

    protected float startTransition = 0.0F;

    protected void animateMovingLimb(SpiderData data, float groundLevel, float limbSwing, int index, float minDist, float maxDist, float minRot, float maxRot)
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
