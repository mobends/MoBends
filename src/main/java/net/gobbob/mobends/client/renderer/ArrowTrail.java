package net.gobbob.mobends.client.renderer;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.Vec3d;

public class ArrowTrail
{
	int id;
	TrailNode[] nodes;

	float spawnCooldown = 0;

	public ArrowTrail(int id)
	{
		this.id = id;
		this.spawnCooldown = MAX_SPAWN_COOLDOWN;
	}

	public void doRender(EntityArrow entity, double x, double y, double z, float partialTicks)
	{
		Vec3d forward = entity.getForward();
		forward = new Vec3d(-forward.x, -forward.y, forward.z);
		Vec3d up = Vec3d.fromPitchYaw(entity.rotationPitch + 90.0f, entity.rotationYaw);
		up = new Vec3d(-up.x, -up.y, up.z);
		Vec3d right = forward.crossProduct(up);

		if (nodes == null)
		{
			this.nodes = new TrailNode[MAX_LENGTH];
			resetNodes(entity.posX, entity.posY, entity.posZ, up, right);
		}

		if (this.spawnCooldown > 40)
		{
			this.spawnCooldown = 0;
			resetNodes(entity.posX, entity.posY, entity.posZ, up, right);
		}

		while (this.spawnCooldown >= MAX_SPAWN_COOLDOWN)
		{
			for (int i = MAX_LENGTH - 1; i > 0; i--)
			{
				nodes[i].moveTo(nodes[i - 1]);
			}
			nodes[0].moveTo(entity.posX, entity.posY, entity.posZ, up, right);
			this.spawnCooldown -= MAX_SPAWN_COOLDOWN;
		}

		renderNodes(entity, partialTicks);
	}

	public void resetNodes(double x, double y, double z, Vec3d up, Vec3d right)
	{
		for (int i = 0; i < MAX_LENGTH; i++)
			this.nodes[i] = new TrailNode(x, y, z, up, right);
	}

	public void renderNodes(EntityArrow entity, float partialTicks)
	{
		Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
		if (viewEntity == null)
			return;
		Vec3d viewPos = new Vec3d(viewEntity.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * partialTicks,
				viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * partialTicks,
				viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * partialTicks);

		float r = 1;
		float g = 1;
		float b = 1;
		float a = 0.5f;
		GlStateManager.color(r, g, b, a);

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for (int i = 1; i < MAX_LENGTH; i++)
		{
			TrailNode node0 = nodes[i - 1];
			TrailNode node1 = nodes[i];

			Vec3d pos0 = new Vec3d(node0.x - viewPos.x, node0.y - viewPos.y, node0.z - viewPos.z);
			Vec3d pos1 = new Vec3d(node1.x - viewPos.x, node1.y - viewPos.y, node1.z - viewPos.z);
			float scale0 = ((float) (MAX_LENGTH - i)) / MAX_LENGTH * 0.1f;
			float scale1 = ((float) MAX_LENGTH - i - 1.0f) / MAX_LENGTH * 0.1f;
			if (i == 1)
			{
				scale1 = 0;
			}
			Vec3d up0 = node0.up, right0 = node0.right, up1 = node1.up, right1 = node1.right;

			vertexbuffer
					.pos(pos0.x + (-right0.x) * scale0, pos0.y + (-right0.y) * scale0, pos0.z + (-right0.z) * scale0)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer.pos(pos0.x + (right0.x) * scale0, pos0.y + (right0.y) * scale0, pos0.z + (right0.z) * scale0)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer.pos(pos1.x + (right1.x) * scale1, pos1.y + (right1.y) * scale1, pos1.z + (right1.z) * scale1)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer
					.pos(pos1.x + (-right1.x) * scale1, pos1.y + (-right1.y) * scale1, pos1.z + (-right1.z) * scale1)
					.tex(0.0D, 0.15625D).endVertex();

			vertexbuffer.pos(pos0.x + (-up0.x) * scale0, pos0.y + (-up0.y) * scale0, pos0.z + (-up0.z) * scale0)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer.pos(pos0.x + (up0.x) * scale0, pos0.y + (up0.y) * scale0, pos0.z + (up0.z) * scale0)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer.pos(pos1.x + (up1.x) * scale1, pos1.y + (up1.y) * scale1, pos1.z + (up1.z) * scale1)
					.tex(0.0D, 0.15625D).endVertex();
			vertexbuffer.pos(pos1.x + (-up1.x) * scale1, pos1.y + (-up1.y) * scale1, pos1.z + (-up1.z) * scale1)
					.tex(0.0D, 0.15625D).endVertex();
		}
		tessellator.draw();

		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	public void renderAxis(EntityArrow entity, double x, double y, double z)
	{
		Vec3d forward = entity.getForward();
		forward = new Vec3d(-forward.x, -forward.y, forward.z);
		Vec3d up = Vec3d.fromPitchYaw(entity.rotationPitch + 90.0f, entity.rotationYaw);
		up = new Vec3d(-up.x, -up.y, up.z);
		Vec3d right = forward.crossProduct(up);

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.depthFunc(GL11.GL_ALWAYS);
		GlStateManager.translate(x, y, z);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		GlStateManager.color(1, 0, 0, 1);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
		vertexbuffer.pos(right.x, right.y, right.z).tex(0.15625D, 0.15625D).endVertex();
		tessellator.draw();
		GlStateManager.color(0, 1, 0, 1);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
		vertexbuffer.pos(up.x, up.y, up.z).tex(0.15625D, 0.15625D).endVertex();
		tessellator.draw();
		GlStateManager.color(0, 0, 1, 1);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX);
		vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
		vertexbuffer.pos(forward.x, forward.y, forward.z).tex(0.15625D, 0.15625D).endVertex();
		tessellator.draw();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.enableCull();
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	private boolean shouldBeRemoved()
	{
		if (Minecraft.getMinecraft().world == null)
			return true;
		if (Minecraft.getMinecraft().world.getEntityByID(id) == null)
			return true;
		return false;
	}

	static class TrailNode
	{
		double x;
		double y;
		double z;

		Vec3d up, right;

		TrailNode(double x, double y, double z, Vec3d up, Vec3d right)
		{
			this.moveTo(x, y, z, up, right);
		}

		public void moveTo(TrailNode trailNode)
		{
			this.moveTo(trailNode.x, trailNode.y, trailNode.z, trailNode.up, trailNode.right);
		}

		public void moveTo(double x, double y, double z, Vec3d up, Vec3d right)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.up = up;
			this.right = right;
		}
	}

	static HashMap<Integer, ArrowTrail> trailMap = new HashMap<Integer, ArrowTrail>();
	public static final int MAX_LENGTH = 10;
	public static final float MAX_SPAWN_COOLDOWN = 1;
	public static long time, lastTime;

	static
	{
		time = System.nanoTime() / 1000;
		lastTime = System.nanoTime() / 1000;
	}

	public static ArrowTrail get(int id)
	{
		ArrowTrail trail;
		if (!trailMap.containsKey(id))
		{
			trail = new ArrowTrail(id);
			trailMap.put(id, trail);
		} else
		{
			trail = trailMap.get(id);
		}

		return trail;
	}

	public static void renderTrail(EntityArrow entity, double x, double y, double z, float partialTicks)
	{
		ArrowTrail trail = get(entity.getEntityId());
		trail.doRender(entity, x, y, z, partialTicks);
	}

	public static void cleanup()
	{
		Integer[] keySet = trailMap.keySet().toArray(new Integer[0]);
		for (int i = keySet.length - 1; i >= 0; i--)
		{
			if (trailMap.get(keySet[i]).shouldBeRemoved())
			{
				trailMap.remove(keySet[i]);
			}
		}
	}

	public static void onRenderTick()
	{
		for (ArrowTrail trail : trailMap.values())
		{
			trail.spawnCooldown += DataUpdateHandler.ticksPerFrame;
		}
	}
}
