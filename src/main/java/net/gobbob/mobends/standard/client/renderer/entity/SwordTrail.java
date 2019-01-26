package net.gobbob.mobends.standard.client.renderer.entity;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.core.client.model.ModelPartTransform;
import net.gobbob.mobends.core.math.Quaternion;
import net.gobbob.mobends.core.math.QuaternionUtils;
import net.gobbob.mobends.core.math.vector.Vec3f;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;

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
		public Quaternion renderRotation = new Quaternion();
		public Vec3f renderOffset = new Vec3f();
		public Quaternion itemRotation = new Quaternion();
		public Vec3f position = new Vec3f();
		
		EnumHandSide primaryHand;
		float velocityX, velocityY, velocityZ;
		float ticksExisted = 0F;

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
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
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

			Vec3f[] points = new Vec3f[] {
				new Vec3f(0, 0, -8 + 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0)),
				new Vec3f(0, 0, -8 - 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0))
			};
			 
			/*
			 * Test cube, to test tracking
			 * Vector3[] points = new Vector3[] {
					new Vector3(-1, -1, -1),
					new Vector3(-1, -1, 1),
					new Vector3(-1, 1, 1),
					new Vector3(-1, 1, -1),
					new Vector3(1, -1, -1),
					new Vector3(1, -1, 1),
					new Vector3(1, 1, 1),
					new Vector3(1, 1, -1)
			};*/
			
			GUtil.translate(points, 0, 0, 16);
			GUtil.rotate(points, part.itemRotation);
			GUtil.translate(points, -1, -6, 0);
			GUtil.rotate(points, part.foreArm.rotation.getSmooth());
			GUtil.translate(points, 0, -6 + 2, 0);
			GUtil.rotate(points, part.arm.rotation.getSmooth());
			GUtil.translate(points, part.arm.position.x, 10, 0);
			GUtil.rotate(points, part.body.rotation.getSmooth());
			GUtil.translate(points, 0, 12, 0);
			GUtil.rotate(points, part.renderRotation);
			GUtil.translate(points, part.renderOffset.x, part.renderOffset.y, part.renderOffset.z);

			for (Vec3f point : points)
			{
				point.x += part.position.x;
				point.y += part.position.y;
				point.z += part.position.z;
			}
			
			/*
			 * Drawing the test cube.
			GLHelper.vertex(points[0]);
			GLHelper.vertex(points[1]);
			GLHelper.vertex(points[2]);
			GLHelper.vertex(points[3]);
			GLHelper.vertex(points[4]);
			GLHelper.vertex(points[5]);
			GLHelper.vertex(points[6]);
			GLHelper.vertex(points[7]);
			*/
			
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
	
	public void add(BipedEntityData<?> entityData, float velocityX, float velocityY, float velocityZ)
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
				newPart.itemRotation.set(entityData.renderRightItemRotation.getSmooth());
			}
			else
			{
				newPart.arm.syncUp(entityData.leftArm);
				newPart.foreArm.syncUp(entityData.leftForeArm);
				newPart.itemRotation.set(entityData.renderLeftItemRotation.getSmooth());
				newPart.itemRotation = QuaternionUtils.rotate(newPart.itemRotation, 90F, 0F, 1F, 0F);
			}
			newPart.renderOffset.set(entityData.renderOffset.getX(),
									 entityData.renderOffset.getY(),
									 entityData.renderOffset.getZ());
			newPart.renderRotation.set(entityData.renderRotation.getSmooth());
			newPart.renderRotation.negate();
			this.trailPartList.add(newPart);
		}
	}
	
	public void add(BipedEntityData<?> entityData)
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
