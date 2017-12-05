package net.gobbob.mobends.data;

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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class EntityData {
	public static final int PLAYER_DATA = 0;
	public static final int SKELETON_DATA = 1;
	public static final int SPIDER_DATA = 2;
	public static final int ZOMBIE_DATA = 3;
	
	public static EntityDatabase[] databases = {
		new EntityDatabase(DataPlayer.class),
		new EntityDatabase(Data_Skeleton.class),
		new EntityDatabase(Data_Spider.class),
		new EntityDatabase(Data_Zombie.class)
	};
	
	protected int entityID;
	protected String entityType;
	protected ModelBase model;
	protected Controller controller;
	
	public Vector3f position = new Vector3f();
	public Vector3f motion_prev = new Vector3f();
	public Vector3f motion = new Vector3f();
	
	protected boolean initialized = false;
	protected float ticks = 0.0f;
    protected boolean updatedThisFrame = false;
    protected float ticksAfterLiftoff = 0.0f;
    protected float ticksAfterTouchdown = 0.0f;
	protected float ticksAfterPunch = 0.0f;
	protected float ticksAfterThrowup = 0.0f;
	protected boolean alreadyPunched = false;
	protected float climbingCycle = 0.0f;
	
	protected boolean onGround = true;
	protected boolean climbing = false;
	protected float headYaw = 0.0f;
	protected float headPitch = 0.0f;
	
	public EntityData(int entityID)
	{
		this.entityID = entityID;
		if(Minecraft.getMinecraft().world.getEntityByID(entityID) != null)
		{
			this.entityType = Minecraft.getMinecraft().world.getEntityByID(entityID).getName();
		}
		else
			this.entityType = "NULL";
		
		this.model = null;
	}
	
	public static void add(int databaseId, EntityData data)
	{
		databases[databaseId].addEntry(data.entityID, data);
	}
	
	public static void addNew(int databaseId, int entityId)
	{
		databases[databaseId].newEntry(entityId);
	}
	
	public static void remove(int databaseId, int entityId)
	{
		databases[databaseId].removeEntry(entityId);
	}
	
	public static EntityData get(int databaseId, int entityId)
	{
		EntityData data = databases[databaseId].getEntry(entityId);
		if(data == null)
			data = databases[databaseId].newEntry(entityId);
		return data;
	}
	
	public boolean canBeUpdated()
	{
		return !updatedThisFrame && !(Minecraft.getMinecraft().world.isRemote && Minecraft.getMinecraft().isGamePaused());
	}
	
	public boolean calcOnGround()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if(entity == null)
			return false;
		
        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
        
        double var1 = this.position.y+this.motion.y;
        
        List list = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(0,-0.025f,0));
        
        return list.size() > 0;
	}
	
	public boolean calcCollidedHorizontally()
	{
		Entity entity = Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		if(entity == null)
			return false;
		
        AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
        
        List list = entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox().offset(this.motion.x, 0, this.motion.z));

        return list.size() > 0;
	}
	
	public boolean isOnGround() { return this.onGround; }
	public boolean isClimbing() { return this.climbing; }
	public float getTicks() { return this.ticks; }
	public float getTicksAfterPunch() { return this.ticksAfterPunch; }
	public float getHeadYaw() { return this.headYaw; }
	public float getHeadPitch() { return this.headPitch; }
	public Controller getController() { return this.controller; }
	public void setClimbing(boolean flag) {
		this.climbing = flag;
	}
	public void setHeadYaw(float headYaw) {
		this.headYaw = headYaw;
	}
	public void setHeadPitch(float headPitch) {
		this.headPitch = headPitch;
	}
	
	public void update(float partialTicks)
	{
		if(this.getEntity() == null)
			return;
		
		if(this.ticks > (Minecraft.getMinecraft().player.ticksExisted + partialTicks))
		{
			this.onTicksRestart();
			this.ticks = (Minecraft.getMinecraft().player.ticksExisted + partialTicks);
		}
		
		this.ticks = (Minecraft.getMinecraft().player.ticksExisted + partialTicks);
		
		updatedThisFrame = false;
		if(this.calcOnGround() & !this.onGround)
		{
			this.onTouchdown();
			this.onGround = true;
		}
		if((!this.calcOnGround() & this.onGround) | (this.motion_prev.y <= 0 && this.motion.y - this.motion_prev.y > 0.4f && this.ticksAfterLiftoff > 2f))
		{
			this.onLiftoff();
			this.onGround = false;
		}
		
		if(this.calcClimbing())
		{
			this.climbingCycle+=DataUpdateHandler.ticksPerFrame*this.motion.y*2.6f;
			this.climbing = true;
		}
		
		if(getEntity().swingProgress > 0)
		{
	        if(!this.alreadyPunched)
	        {
	        	this.onPunch();
	        	this.alreadyPunched = true;
	        }
		}
		else
		{
			this.alreadyPunched = false;
		}
		
		if(this.motion_prev.y <= 0 && this.motion.y > 0)
		{
			this.onThrowup();
		}
		
		if(!this.isOnGround()) this.ticksAfterLiftoff+=DataUpdateHandler.ticksPerFrame;
		if(this.isOnGround()) this.ticksAfterTouchdown+=DataUpdateHandler.ticksPerFrame;
		this.ticksAfterPunch+=DataUpdateHandler.ticksPerFrame;
		this.ticksAfterThrowup+=DataUpdateHandler.ticksPerFrame;
	}
	
	public EntityLivingBase getEntity()
	{
		if(Minecraft.getMinecraft().world.getEntityByID(this.entityID) instanceof EntityLivingBase)
			return (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(this.entityID);
		else
			return null;
	}
	
	public void onTicksRestart()
	{}
	
	public void onTouchdown()
	{
		this.ticksAfterTouchdown = 0.0f;
	}
	
	public void onLiftoff()
	{
		this.ticksAfterLiftoff = 0.0f;
	}
	
	public void onThrowup()
	{
		this.ticksAfterThrowup = 0.0f;
	}
	
	public void onPunch()
	{
		this.ticksAfterPunch = 0.0f;
	}
	
	public float getClimbingRotation()
	{
		EnumFacing facing = getLadderFacing();
		if(facing == EnumFacing.NORTH) return 0.0f;
		if(facing == EnumFacing.SOUTH) return 180.0f;
		if(facing == EnumFacing.WEST) return -90.0f;
		if(facing == EnumFacing.EAST) return 90.0f;
		return 0;
	}
	
	public EnumFacing getLadderFacing()
	{
		EntityLivingBase entity = (EntityLivingBase) getEntity();
		
		BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
		
		IBlockState block = entity.world.getBlockState(position);
		IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
		IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));
		
		if(block.getBlock() instanceof BlockLadder) return block.getValue(BlockLadder.FACING);
		else if(blockBelow.getBlock() instanceof BlockLadder) return blockBelow.getValue(BlockLadder.FACING);
		else if(blockBelow2.getBlock() instanceof BlockLadder) return blockBelow2.getValue(BlockLadder.FACING);
		return EnumFacing.NORTH;
	}
	
	public boolean calcClimbing()
	{
		EntityLivingBase entity = (EntityLivingBase) getEntity();
		
		if(entity == null || entity.world == null) return false;
		
		BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
		
		IBlockState block = entity.world.getBlockState(position);
		IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
		IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));
		
		return entity.isOnLadder() && !this.isOnGround() && (block.getBlock() instanceof BlockLadder || blockBelow.getBlock() instanceof BlockLadder || blockBelow2.getBlock() instanceof BlockLadder);
	}
	
	public float getLedgeHeight()
	{
		EntityLivingBase entity = (EntityLivingBase) getEntity();
		
    	float clientY = (float) (entity.posY + (entity.posY-entity.prevPosY)*EventHandlerRenderPlayer.partialTicks);
    	
    	BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
		
		IBlockState block = entity.world.getBlockState(position.add(0, 2, 0));
		IBlockState blockBelow = entity.world.getBlockState(position.add(0, 1, 0));
		IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, 0, 0));
    	if(!(block.getBlock() instanceof BlockLadder))
    	{
	    	if(!(blockBelow.getBlock() instanceof BlockLadder))
	    	{
	    		if(!(blockBelow2.getBlock() instanceof BlockLadder))
	    			return (float) (clientY-((int)clientY))+2;
	    		else
	    			return (float) (clientY-((int)clientY))+1;
	    	}
	    	else
	    		return (float) (clientY-((int)clientY));
    	}
    	
    	return -2.0f;
    }
	
	public float getLookAngle()
	{
		EntityLivingBase entity = (EntityLivingBase) getEntity();
		Vec3d vec3 = entity.getLookVec();
		double x = vec3.x;
		double z = vec3.z;
		if(x*x+z*z == 0){
			return 0;
		}
		return (float)(Math.atan2(x, z)/Math.PI*180.0f);
	}
	
	public float getMovementAngle()
	{
		float lookAngle = this.getLookAngle();
		
		double x = this.motion.x;
		double z = this.motion.z;
		if(x*x+z*z == 0)
			return 0;
		float worldMoveAngle = (float)(Math.atan2(x, z)/Math.PI*180.0f);
		
		return worldMoveAngle-lookAngle;
	}
	
	public boolean isStrafing()
	{
		float angle = this.getMovementAngle();
		float threshold = 30.0f;
		return (angle >= threshold && angle <= 180.0f-threshold) || 
			   (angle >= -180.0f+threshold && angle <= -threshold);
	}
	
	public float getMovementSpeed()
	{
		return this.motion.length();
	}
	
	public boolean isInitialized()
	{
		return this.initialized;
	}
	
	public void setInitialized(boolean flag)
	{
		this.initialized = flag;
	}
	
	public abstract void initModelPose();
}
