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

		EnumHandSide primaryHand;
		float ticksExisted = 0.0F;

		public TrailPart(EnumHandSide primaryHand)
		{
			this.body = new ModelPartTransform();
			this.arm = new ModelPartTransform();
			this.foreArm = new ModelPartTransform();
			this.primaryHand = primaryHand;
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

			Vector3f[] point = new Vector3f[] {
					new Vector3f(0, 0, -8 + 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0)),
					new Vector3f(0, 0, -8 - 8 * alpha + (part.primaryHand == EnumHandSide.LEFT ? -8 : 0)), };

			GUtil.rotateX(point, part.itemRotation.getX());
			GUtil.rotateY(point, part.itemRotation.getY());
			GUtil.rotateZ(point, part.itemRotation.getZ());

			GUtil.translate(point, new Vector3f(-1, -6, 0));
			GUtil.rotateX(point, part.foreArm.rotation.getX());
			GUtil.rotateY(point, part.foreArm.rotation.getY());
			GUtil.rotateZ(point, part.foreArm.rotation.getZ());

			GUtil.rotateX(point, part.foreArm.preRotation.getX());
			GUtil.rotateY(point, part.foreArm.preRotation.getY());
			GUtil.rotateZ(point, -part.foreArm.preRotation.getZ());

			GUtil.translate(point, new Vector3f(0, -6 + 2, 0));
			GUtil.rotateX(point, part.arm.rotation.getX());
			GUtil.rotateY(point, part.arm.rotation.getY());
			GUtil.rotateZ(point, part.arm.rotation.getZ());

			GUtil.rotateX(point, part.arm.preRotation.getX());
			GUtil.rotateY(point, part.arm.preRotation.getY());
			GUtil.rotateZ(point, -part.arm.preRotation.getZ());

			GUtil.translate(point, new Vector3f(-5, 10, 0));
			GUtil.rotateX(point, part.body.rotation.getX());
			GUtil.rotateY(point, part.body.rotation.getY());
			GUtil.rotateZ(point, part.body.rotation.getZ());

			GUtil.rotateX(point, part.body.preRotation.getX());
			GUtil.rotateY(point, part.body.preRotation.getY());
			GUtil.rotateZ(point, part.body.preRotation.getZ());
			GUtil.translate(point, new Vector3f(0, 12, 0));

			GUtil.rotateX(point, part.renderRotation.getX());
			GUtil.rotateY(point, part.renderRotation.getY());
			GUtil.translate(point, part.renderOffset);

			if (i > 0)
			{
				// Closing up the previous strand
				GlStateManager.glVertex3f(point[1].x, point[1].y, point[1].z);
				GlStateManager.glVertex3f(point[0].x, point[0].y, point[0].z);
			}
			
			GlStateManager.glVertex3f(point[0].x, point[0].y, point[0].z);
			GlStateManager.glVertex3f(point[1].x, point[1].y, point[1].z);

			if (i == this.trailPartList.size() - 1)
			{
				// Closing the trail
				GlStateManager.glVertex3f(point[1].x, point[1].y, point[1].z);
				GlStateManager.glVertex3f(point[0].x, point[0].y, point[0].z);
			}
		}
		GlStateManager.glEnd();
		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.enableLighting();
	}

	public void add(BipedEntityData entityData)
	{
		Entity entity = entityData.getEntity();
		if (entity instanceof EntityLivingBase)
		{
			EnumHandSide primaryHand = ((EntityLivingBase) entity).getPrimaryHand();
			TrailPart newPart = new TrailPart(primaryHand);
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

	public void update(float ticksPerFrame)
	{
		for (int i = 0; i < this.trailPartList.size(); i++)
		{
			TrailPart trailPart = this.trailPartList.get(i);
			trailPart.ticksExisted += ticksPerFrame;
			if (trailPart.ticksExisted > 20)
			{
				this.trailPartList.remove(trailPart);
			}
		}
	}
}
