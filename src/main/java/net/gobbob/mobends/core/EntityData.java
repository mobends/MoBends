package net.gobbob.mobends.core;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.client.model.IBendsModel;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.core.util.SmoothOrientation;
import net.gobbob.mobends.core.util.SmoothVector3f;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class EntityData<T extends EntityData, E extends Entity> implements IBendsModel
{
	protected int entityID;
	protected E entity;
	protected Controller<T> controller;

	protected double positionX = 0.0D;
	protected double positionY = 0.0D;
	protected double positionZ = 0.0D;
	protected double prevMotionX = 0.0D;
	protected double prevMotionY = 0.0D;
	protected double prevMotionZ = 0.0D;
	protected double motionX = 0.0D;
	protected double motionY = 0.0D;
	protected double motionZ = 0.0D;
	protected boolean onGround = true;
	protected HashMap<String, Object> nameToPartMap;

	public SmoothVector3f renderOffset;
    public SmoothOrientation renderRotation;
    public SmoothOrientation centerRotation;
	
	public EntityData(E entity)
	{
		this.entity = entity;
		if (this.entity != null)
		{
			this.entityID = entity.getEntityId();
		}
		this.motionX = this.prevMotionX = 0.0D;
		this.motionY = this.prevMotionY = 1.0D;
		this.motionZ = this.prevMotionZ = 0.0D;
		
		this.initModelPose();
	}

	public void initModelPose()
	{
		this.renderOffset = new SmoothVector3f();
		this.renderRotation = new SmoothOrientation();
		this.centerRotation = new SmoothOrientation();
		
		this.nameToPartMap = new HashMap<String, Object>();
		this.nameToPartMap.put("renderRotation", renderRotation);
		this.nameToPartMap.put("centerRotation", centerRotation);
	}

	/*
	 * Updates all the model's parts to be in their next frame. Called in
	 * EntityData.update();
	 */
	public void updateParts(float ticksPerFrame)
	{
		this.renderOffset.update(ticksPerFrame);
		this.renderRotation.update(ticksPerFrame);
		this.centerRotation.update(ticksPerFrame);
	}

	public boolean canBeUpdated()
	{
		return !(Minecraft.getMinecraft().world.isRemote && Minecraft.getMinecraft().isGamePaused());
	}

	public boolean calcOnGround()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if (entity == null)
			return false;

		List<AxisAlignedBB> list = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0, -0.025f, 0));
		return list.size() > 0;
	}
	
	/*
	 * Used by the Previewers to simulate the entities
	 * being on ground.
	 */
	public void forceOnGround(boolean flag)
	{
		this.onGround = flag;
	}
	
	public boolean calcCollidedHorizontally()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if (entity == null)
			return false;

		List<AxisAlignedBB> list = entity.world.getCollisionBoxes(entity,
				entity.getEntityBoundingBox().offset(this.motionX, 0, this.motionZ));
		return list.size() > 0;
	}

	
	public double getPositionX() { return this.positionX; }
	public double getPositionY() { return this.positionY; }
	public double getPositionZ() { return this.positionZ; }
	public double getMotionX() { return this.motionX; }
	public double getMotionY() { return this.motionY; }
	public double getMotionZ() { return this.motionZ; }
	public double getPrevMotionX() { return this.prevMotionX; }
	public double getPrevMotionY() { return this.prevMotionY; }
	public double getPrevMotionZ() { return this.prevMotionZ; }
	public double getInterpolatedMotionX() { return this.prevMotionX + (this.motionX - this.prevMotionX) * DataUpdateHandler.partialTicks; }
	public double getInterpolatedMotionY() { return this.prevMotionY + (this.motionY - this.prevMotionY) * DataUpdateHandler.partialTicks; }
	public double getInterpolatedMotionZ() { return this.prevMotionZ + (this.motionZ - this.prevMotionZ) * DataUpdateHandler.partialTicks; }
	
	public boolean isOnGround()
	{
		return this.onGround;
	}
	
	public boolean isStillHorizontally()
	{
		return motionX == 0.0D && motionZ == 0.0D;
	}

	public Controller<T> getController()
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
	}

	public E getEntity()
	{
		return this.entity;
	}

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

		double x = this.motionX;
		double z = this.motionZ;
		if (x * x + z * z == 0)
			return 0;
		float worldMoveAngle = (float) (Math.atan2(x, z) / Math.PI * 180.0f);

		return worldMoveAngle - lookAngle;
	}
	
	public double getForwardMomentum()
	{
		double motionLen = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		
		if (motionLen == 0)
			return 0;
		
		double mx = motionX;
		double mz = motionZ;
		
		Vec3d lookVec = entity.getLookVec();
		Vec3d vec = new Vec3d(lookVec.x, 0, lookVec.z);
		if (vec.lengthSquared() == 0)
			return 0;
		vec = vec.normalize();
		double lx = vec.x;
		double lz = vec.z;
		
		return lx * mx + lz * mz;
	}
	
	public double getSidewaysMomentum()
	{
		double motionLen = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
		
		if (motionLen == 0)
			return 0;
		
		double mx = motionX;
		double mz = motionZ;
		
		Vec3d rightVec = entity.getLookVec().rotateYaw(-GUtil.PI / 2.0F);
		Vec3d vec = new Vec3d(rightVec.x, 0, rightVec.z);
		if (vec.lengthSquared() == 0)
			return 0;
		vec = vec.normalize();
		double lx = vec.x;
		double lz = vec.z;
		
		return lx * mx + lz * mz;
	}

	public boolean isStrafing()
	{
		float angle = this.getMovementAngle();
		float threshold = 30.0f;
		return (angle >= threshold && angle <= 180.0f - threshold)
				|| (angle >= -180.0f + threshold && angle <= -threshold);
	}
	
	/**
	 * @return True, if the player is sufficiently underwater.
	 */
	public boolean isUnderwater()
	{
		int blockX = (int) this.entity.posX;
		int blockY = (int) this.entity.posY + 2;
		int blockZ = (int) this.entity.posZ;
		IBlockState state = Minecraft.getMinecraft().world.getBlockState(new BlockPos(blockX, blockY, blockZ));
		return this.entity.isInWater() && state.getBlock() instanceof BlockStaticLiquid;
	}

	public double getPrevMotionMagnitude()
	{
		return Math.sqrt(this.prevMotionX * this.prevMotionX + this.prevMotionY * this.prevMotionY + this.prevMotionZ * this.prevMotionZ);
	}
	
	public double getMotionMagnitude()
	{
		return Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
	}
	
	public double getInterpolatedMotionMagnitude()
	{
		final double magnitude = this.getMotionMagnitude();
		final double prevMagnitude = this.getPrevMotionMagnitude();
		return prevMagnitude + (magnitude - prevMagnitude) * DataUpdateHandler.partialTicks;
	}
	
	public double getXZMotionMagnitude() {
		return Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
	}
	
	public double getPrevXZMotionMagnitude() {
		return Math.sqrt(this.prevMotionX * this.prevMotionX + this.prevMotionZ * this.prevMotionZ);
	}
	
	public double getInterpolatedXZMotionMagnitude()
	{
		final double magnitude = this.getXZMotionMagnitude();
		final double prevMagnitude = this.getPrevXZMotionMagnitude();
		return prevMagnitude + (magnitude - prevMagnitude) * DataUpdateHandler.partialTicks;
	}

	public void updateClient(Entity entity)
	{
		this.prevMotionX = this.motionX;
		this.prevMotionY = this.motionY;
		this.prevMotionZ = this.motionZ;

		this.motionX = entity.posX - this.positionX;
		this.motionY = entity.posY - this.positionY;
		this.motionZ = entity.posZ - this.positionZ;

		this.positionX = entity.posX;
		this.positionY = entity.posY;
		this.positionZ = entity.posZ;
	}
	
	@Override
	public Object getPartForName(String name)
	{
		return nameToPartMap.get(name);
	}
	
	public void onTicksRestart() {}
}
