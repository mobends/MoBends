package net.gobbob.mobends.standard.client.renderer.entity;

import net.gobbob.mobends.standard.main.ModConfig;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderBendsArrow<T extends EntityArrow> extends RenderArrow<T>
{
    public RenderBendsArrow(RenderManager renderManagerIn) {
		super(renderManagerIn);
	}
    
    @Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
    	if(ModConfig.showArrowTrails)
    		ArrowTrail.renderTrail(entity, x, y, z, partialTicks);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}