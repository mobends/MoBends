package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.renderer.ArrowTrail;
import net.gobbob.mobends.settings.SettingManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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
    	if(SettingManager.ARROW_TRAILS.isEnabled())
    		ArrowTrail.renderTrail(entity, x, y, z, partialTicks);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}