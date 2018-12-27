package net.gobbob.mobends.standard.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderBendsTippedArrow extends RenderBendsArrow<EntityTippedArrow>
{
	public static final ResourceLocation RES_ARROW = new ResourceLocation("textures/entity/projectiles/arrow.png");
	public static final ResourceLocation RES_TIPPED_ARROW = new ResourceLocation(
			"textures/entity/projectiles/tipped_arrow.png");

	public RenderBendsTippedArrow(RenderManager manager)
	{
		super(manager);
	}

	protected ResourceLocation getEntityTexture(EntityTippedArrow entity)
	{
		return entity.getColor() > 0 ? RES_TIPPED_ARROW : RES_ARROW;
	}

	public static class Factory implements IRenderFactory
	{
		@Override
		public Render createRenderFor(RenderManager manager)
		{
			return new RenderBendsTippedArrow(manager);
		}
	}
}
