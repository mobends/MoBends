package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.renderer.ArrowTrail;
import net.gobbob.mobends.configuration.SettingsManager;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderBendsArrow<T extends EntityArrow> extends RenderArrow<T>
{
    public RenderBendsArrow(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}
    
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
    	if(SettingsManager.ARROW_TRAILS.isEnabled())
    		ArrowTrail.renderTrail(entity, x, y, z, partialTicks);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}