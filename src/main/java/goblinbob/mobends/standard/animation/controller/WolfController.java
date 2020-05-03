package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.SquidData;
import goblinbob.mobends.standard.data.WolfData;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This is an animation controller for a wolf instance. It's a part of the
 * EntityData structure.
 *
 * @author Iwo Plaza
 *
 */
public class WolfController implements IAnimationController<WolfData>
{

    protected HardAnimationLayer<SquidData> layerBase;

    @Override
    @Nullable
    public Collection<String> perform(WolfData data)
    {
        EntityWolf wolf = data.getEntity();

        data.body.rotation.setSmoothness(1.0f).orientX(90.0F);

        final float limbSwing = data.limbSwing.get();
        final float limbSwingAmount = data.limbSwingAmount.get();

        final float TO_DEG = 180.0F / GUtil.PI;

        if (wolf.isSitting())
        {
            //wolfMane.setRotationPoint(-1.0F, 16.0F, -3.0F);
            data.body.rotation.orientX(45.0F);
            data.mane.rotation.orientX(72.0F);
            //data.wolfLeg1.rotation.orientX(270.0F);
            //data.wolfLeg2.rotation.orientX(270.0F);
            data.leg3.rotation.orientX(-0.468F * TO_DEG);
            data.leg4.rotation.orientX(-0.468F * TO_DEG);
        }
        else
        {
            data.body.rotation.orientX(90.0F);
            data.mane.rotation.orientX(90.0F);
            data.leg1.rotation.orientX(MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * TO_DEG);
            data.leg2.rotation.orientX(MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * TO_DEG);
            //data.wolfLeg3.rotation.orientX(MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount * TO_DEG);
            //data.wolfLeg4.rotation.orientX(MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * TO_DEG);
            data.leg3.rotation.orientX(0 * TO_DEG);
            data.leg4.rotation.orientX(0 * TO_DEG);
        }

        data.mane.rotation.orientX(0.0F);
        data.body.rotation.orientX(0);
        data.tail.rotation.orientX(180.0F);
        //data.wolfBody.rotation.orientX(DataUpdateHandler.getTicks() * 10.0F);

        return null;
    }

}
