package net.gobbob.mobends.standard.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsSpectralArrow extends RenderBendsArrow<EntitySpectralArrow>
{
	public static final ResourceLocation RES_SPECTRAL_ARROW = new ResourceLocation(
			"textures/entity/projectiles/spectral_arrow.png");

	public RenderBendsSpectralArrow(RenderManager manager)
	{
		super(manager);
	}

	protected ResourceLocation getEntityTexture(EntitySpectralArrow entity)
	{
		return RES_SPECTRAL_ARROW;
	}

	public static class Factory implements IRenderFactory
	{
		@Override
		public Render createRenderFor(RenderManager manager)
		{
			return new RenderBendsSpectralArrow(manager);
		}
	}
}
