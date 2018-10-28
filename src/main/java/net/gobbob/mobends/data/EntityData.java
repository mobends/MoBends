package net.gobbob.mobends.data;

import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animation.controller.Controller;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.util.SmoothOrientation;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class EntityData implements IBendsModel
{
	protected int entityID;
	protected Entity entity;
	protected Controller controller;

	protected Vector3f position = new Vector3f();
	protected Vector3f previousMotion = new Vector3f();
	protected Vector3f motion = new Vector3f();
	protected boolean onGround = true;
	protected HashMap<String, Object> nameToPartMap;

	public SmoothVector3f renderOffset;
    public SmoothOrientation renderRotation;
	
	public EntityData(Entity entity)
	{
		this.entity = entity;
		if (this.entity != null)
		{
			this.entityID = entity.getEntityId();
		}
		this.motion.set(0F, 1F, 0F);
		this.previousMotion.set(this.motion);
		
		this.initModelPose();
	}

	public void initModelPose()
	{
		this.renderOffset = new SmoothVector3f();
		this.renderRotation = new SmoothOrientation();
		
		this.nameToPartMap = new HashMap<String, Object>();
		this.nameToPartMap.put("renderRotation", renderRotation);
	}

	/*
	 * Updates all the model's parts to be in their next frame. Called in
	 * EntityData.update();
	 */
	public void updateParts(float ticksPerFrame)
	{
		this.renderOffset.update(ticksPerFrame);
		this.renderRotation.update(ticksPerFrame);
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
				entity.getEntityBoundingBox().offset(this.motion.x, 0, this.motion.z));
		return list.size() > 0;
	}

	public Vector3f getPosition()
	{
		return this.position;
	}
	
	public Vector3f getPreviousMotion()
	{
		return this.previousMotion;
	}
	
	public Vector3f getMotion()
	{
		return this.motion;
	}
	
	public boolean isOnGround()
	{
		return this.onGround;
	}
	
	public boolean isStillHorizontally()
	{
		return motion.x == 0 && motion.z == 0;
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

	public float getMotionMagnitude()
	{
		return this.motion.length();
	}

	public void updateClient(Entity entity)
	{
		this.previousMotion.set(this.motion);

		this.motion.x = (float) entity.posX - this.position.x;
		this.motion.y = (float) entity.posY - this.position.y;
		this.motion.z = (float) entity.posZ - this.position.z;

		this.position.set((float) entity.posX, (float) entity.posY, (float) entity.posZ);
	}
	
	@Override
	public Object getPartForName(String name)
	{
		return nameToPartMap.get(name);
	}
}
