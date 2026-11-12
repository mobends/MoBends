package goblinbob.mobends.standard.client.renderer.entity;

import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Vanilla's arrow, with a trail behind it. */
@SideOnly(Side.CLIENT)
public class RenderBendsArrow extends RenderArrow
{

    public RenderBendsArrow(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
    public void doRender(EntityArrow entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (ModConfig.showArrowTrails)
            ArrowTrailManager.renderTrail(entity, x, y, z, partialTicks);

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

}
