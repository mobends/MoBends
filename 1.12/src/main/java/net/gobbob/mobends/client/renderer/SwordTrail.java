package net.gobbob.mobends.client.renderer;

import java.util.ArrayList;
import java.util.List;

import net.gobbob.mobends.client.ClientProxy;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.data.BipedEntityData;
import net.gobbob.mobends.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class SwordTrail
{
	public List<TrailPart> trailPartList = new ArrayList<TrailPart>();

	public void reset()
	{
		this.trailPartList.clear();
	}

	public class TrailPart
	{
		public ModelPartTransform body, arm, foreArm;
		public Vector3f renderRotation = new Vector3f();
		public Vector3f renderOffset = new Vector3f();
		public Vector3f itemRotation = new Vector3f();
		public Vector3f position = new Vector3f();
		
		EnumHandSide primaryHand;
		float velocityX, velocityY, velocityZ;
		float ticksExisted = 0.0F;

		public TrailPart(EnumHandSide primaryHand, float velocityX, float velocityY, float velocityZ)
		{
			this.body = new ModelPartTransform();
			this.arm = new ModelPartTransform();
			this.foreArm = new ModelPartTransform();
			this.primaryHand = primaryHand;
			this.velocityX = velocityX;
			this.velocityY = velocityY;
			this.velocityZ = velocityZ;
		}

		public void update(float ticksPerFrame)
		{
			this.ticksExisted += ticksPerFrame;
			this.position.x += this.velocityX * ticksPerFrame;
			this.position.y += this.velocityY * ticksPerFrame;
			this.position.z += this.velocityZ * ticksPerFrame;
		}
	}

	public void render()
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.TEXTURE_NULL);

		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.pushMatrix();
		
		GlStateManager.glBegin(GL11.GL_QUADS);
		for (int i = 0; i < this.trailPartList.size(); i++)
		{
			TrailPart part = this.trailPartList.get(i);

			float alpha = part.ticksExisted / 5.0f;
			alpha = Math.min(alpha, 1.0f);
			alpha = 1.0f - alpha;

			GlStateManager.color(1, 1, 1, alpha);

			Vector3f[] points = new Vector3f[] {
					new Vector3f(0, 0, -8 + 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0)),
					new Vector3f(0, 0, -8 - 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0)), };

			GUtil.rotateX(points, part.itemRotation.getX());
			GUtil.rotateY(points, part.itemRotation.getY());
			GUtil.rotateZ(points, part.itemRotation.getZ());

			GUtil.translate(points, new Vector3f(-1, -6, 0));
			GUtil.rotateX(points, part.foreArm.rotation.getX());
			GUtil.rotateY(points, part.foreArm.rotation.getY());
			GUtil.rotateZ(points, part.foreArm.rotation.getZ());

			GUtil.rotateX(points, part.foreArm.preRotation.getX());
			GUtil.rotateY(points, part.foreArm.preRotation.getY());
			GUtil.rotateZ(points, -part.foreArm.preRotation.getZ());

			GUtil.translate(points, new Vector3f(0, -6 + 2, 0));
			GUtil.rotateX(points, part.arm.rotation.getX());
			GUtil.rotateY(points, part.arm.rotation.getY());
			GUtil.rotateZ(points, part.arm.rotation.getZ());

			GUtil.rotateX(points, part.arm.preRotation.getX());
			GUtil.rotateY(points, part.arm.preRotation.getY());
			GUtil.rotateZ(points, -part.arm.preRotation.getZ());

			GUtil.translate(points, new Vector3f(-5, 10, 0));
			GUtil.rotateX(points, part.body.rotation.getX());
			GUtil.rotateY(points, part.body.rotation.getY());
			GUtil.rotateZ(points, part.body.rotation.getZ());

			GUtil.rotateX(points, part.body.preRotation.getX());
			GUtil.rotateY(points, part.body.preRotation.getY());
			GUtil.rotateZ(points, -part.body.preRotation.getZ());
			GUtil.translate(points, new Vector3f(0, 12, 0));

			GUtil.rotateX(points, part.renderRotation.getX());
			GUtil.rotateY(points, part.renderRotation.getY());
			GUtil.translate(points, part.renderOffset);

			for (Vector3f point : points)
			{
				point.x += part.position.x;
				point.y += part.position.y;
				point.z += part.position.z;
			}
			
			if (i > 0)
			{
				// Closing up the previous strand
				GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);
				GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
			}
			
			GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
			GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);

			if (i == this.trailPartList.size() - 1)
			{
				// Closing the trail
				GlStateManager.glVertex3f(points[1].x, points[1].y, points[1].z);
				GlStateManager.glVertex3f(points[0].x, points[0].y, points[0].z);
			}
		}
		GlStateManager.glEnd();
		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.enableLighting();
	}
	
	public void add(BipedEntityData entityData, float velocityX, float velocityY, float velocityZ)
	{
		Entity entity = entityData.getEntity();
		if (entity instanceof EntityLivingBase)
		{
			EnumHandSide primaryHand = ((EntityLivingBase) entity).getPrimaryHand();
			TrailPart newPart = new TrailPart(primaryHand, velocityX, velocityY, velocityZ);
			newPart.body.syncUp(entityData.body);
			if (primaryHand == EnumHandSide.RIGHT)
			{
				newPart.arm.syncUp(entityData.rightArm);
				newPart.foreArm.syncUp(entityData.rightForeArm);
				newPart.itemRotation.set(entityData.renderRightItemRotation.getX(),
										 entityData.renderRightItemRotation.getY(),
										 entityData.renderRightItemRotation.getZ());
			}
			else
			{
				newPart.arm.syncUp(entityData.leftArm);
				newPart.foreArm.syncUp(entityData.leftForeArm);
				newPart.itemRotation.set(entityData.renderLeftItemRotation.getX(),
										 entityData.renderLeftItemRotation.getY(),
										 entityData.renderLeftItemRotation.getZ());
			}
			newPart.renderOffset.set(entityData.renderOffset.getX(),
									 entityData.renderOffset.getY(),
									 entityData.renderOffset.getZ());
			newPart.renderRotation.set(entityData.renderRotation.getX(),
									   entityData.renderRotation.getY(),
									   entityData.renderRotation.getZ());
			this.trailPartList.add(newPart);
		}
	}
	
	public void add(BipedEntityData entityData)
	{
		this.add(entityData, 0, 0, 0);
	}

	public void update(float ticksPerFrame)
	{
		for (int i = 0; i < this.trailPartList.size(); i++)
		{
			TrailPart trailPart = this.trailPartList.get(i);
			trailPart.update(ticksPerFrame);
			if (trailPart.ticksExisted > 20)
			{
				this.trailPartList.remove(trailPart);
			}
		}
	}
}
