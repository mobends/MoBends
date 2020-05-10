package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.SquidData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * This is an animation controller for a squid instance. It's a part of the EntityData structure.
 *
 * @author Iwo Plaza
 */
public class SquidController implements IAnimationController<SquidData>
{

    @Override
    @Nullable
    public Collection<String> perform(SquidData data)
    {
        EntitySquid squid = data.getEntity();

        float squidRotation = squid.prevSquidRotation + (squid.squidRotation - squid.prevSquidRotation) * DataUpdateHandler.partialTicks + 1.1F;
        float f = squidRotation / GUtil.PI;
        f = Math.max(0.0F, f);
        float baseTentacleAngle = 0.0F;
        if (squid.prevSquidRotation < GUtil.PI)
        {
            baseTentacleAngle = MathHelper.sin(f * f * (float) Math.PI) * 60.0f;
        }

        for (int i = 0; i < data.squidTentacles.length; ++i)
        {

            double d0 = (double) i * -360.0D / (double) data.squidTentacles.length + 90.0D;
            data.squidTentacles[i][0].rotation.setSmoothness(0.1F).orientX(baseTentacleAngle).rotateY((float) d0);

            float f2 = squidRotation / (GUtil.PI * 2);
            f2 = Math.max(0.0F, f2);
            for (int j = 1; j < SquidData.TENTACLE_SECTIONS; ++j)
            {
                float tentacleAngle = 0;
                if (squid.squidRotation < GUtil.PI)
                {
                    tentacleAngle = MathHelper.sin(f2 * GUtil.PI * 2 + j * 0.1F) * 10.0F;
                }
                data.squidTentacles[i][j].rotation.setSmoothness(0.1F).orientX(-tentacleAngle);
            }
        }

        return null;
    }

}
