package net.gobbob.mobends.data;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class EntityData
{
	protected int entityID;
	protected Entity entity;
	protected Controller controller;

	public Vector3f position = new Vector3f();
	public Vector3f motion_prev = new Vector3f();
	public Vector3f motion = new Vector3f();

	protected boolean updatedThisFrame = false;

	protected boolean onGround = true;

	public EntityData(Entity entity)
	{
		this.entity = entity;
		if (this.entity != null)
		{
			this.entityID = entity.getEntityId();
		}

		this.initModelPose();
	}

	public abstract void initModelPose();

	/*
	 * Updates all the model's parts to be in their next frame. Called in
	 * EntityData.update();
	 */
	public abstract void updateParts(float ticksPerFrame);

	public boolean canBeUpdated()
	{
		return !updatedThisFrame
				&& !(Minecraft.getMinecraft().world.isRemote && Minecraft.getMinecraft().isGamePaused());
	}

	public boolean calcOnGround()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if (entity == null)
			return false;

		AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();

		double var1 = this.position.y + this.motion.y;

		List list = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0, -0.025f, 0));

		return list.size() > 0;
	}

	public boolean calcCollidedHorizontally()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if (entity == null)
			return false;

		AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();

		List list = entity.world.getCollisionBoxes(entity,
				entity.getEntityBoundingBox().offset(this.motion.x, 0, this.motion.z));

		return list.size() > 0;
	}

	public boolean isOnGround()
	{
		return this.onGround;
	}

	public Controller getController()
	{
		return this.controller;
	}

	/*
	 * Called during the render tick in EntityDatabase.updateRender()
	 */
	public void update(float partialTicks)
	{
		if (this.entity == null)
			return;

		this.updateParts(DataUpdateHandler.ticksPerFrame);

		updatedThisFrame = false;
	}

	public Entity getEntity()
	{
		return this.entity;
	}

	public abstract void onTicksRestart();

	public float getLookAngle()
	{
		EntityLivingBase entity = (EntityLivingBase) getEntity();
		Vec3d vec3 = entity.getLookVec();
		double x = vec3.x;
		double z = vec3.z;
		if (x * x + z * z == 0)
		{
			return 0;
		}
		return (float) (Math.atan2(x, z) / Math.PI * 180.0f);
	}

	public float getMovementAngle()
	{
		float lookAngle = this.getLookAngle();

		double x = this.motion.x;
		double z = this.motion.z;
		if (x * x + z * z == 0)
			return 0;
		float worldMoveAngle = (float) (Math.atan2(x, z) / Math.PI * 180.0f);

		return worldMoveAngle - lookAngle;
	}

	public boolean isStrafing()
	{
		float angle = this.getMovementAngle();
		float threshold = 30.0f;
		return (angle >= threshold && angle <= 180.0f - threshold)
				|| (angle >= -180.0f + threshold && angle <= -threshold);
	}

	public float getMotionMagnitude()
	{
		return this.motion.length();
	}
}
