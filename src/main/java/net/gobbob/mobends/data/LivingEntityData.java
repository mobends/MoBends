package net.gobbob.mobends.data;

import java.util.HashMap;

import org.lwjgl.util.vector.Quaternion;

import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EntityRenderHandler;
import net.gobbob.mobends.util.SmoothOrientation;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class LivingEntityData extends EntityData
{
	protected float ticksAfterLiftoff;
    protected float ticksAfterTouchdown;
	protected float ticksAfterAttack;
	protected float ticksAfterThrowup;
	protected float climbingCycle = 0F;
	protected boolean alreadyPunched = false;
	protected boolean climbing = false;
	protected float limbSwing = 0F;
	protected float limbSwingAmount = 0F;
	protected float headYaw = 0F;
	protected float headPitch = 0F;
	
	public SmoothVector3f renderOffset;
    public SmoothOrientation renderRotation;
	
	public LivingEntityData(Entity entity)
	{
		super(entity);
		
		// Setting high values for ticksAfter* variables
		// to avoid premature animation triggers.
		// (like the automatic attack stance on creation)
		this.ticksAfterLiftoff = 100F;
		this.ticksAfterTouchdown = 100F;
		this.ticksAfterAttack = 100F;
		this.ticksAfterThrowup = 100F;
	}
	
	@Override
	public void initModelPose()
	{
		this.renderOffset = new SmoothVector3f();
		this.renderRotation = new SmoothOrientation();
		
		this.nameToPartMap = new HashMap<String, Object>();
		this.nameToPartMap.put("renderRotation", renderRotation);
	}
	
	@Override
	public void updateParts(float ticksPerFrame)
	{
		this.renderOffset.update(ticksPerFrame);
		this.renderRotation.update(ticksPerFrame);
	}
	
	public void setClimbing(boolean flag)
	{
		this.climbing = flag;
	}
	
	public void setLimbSwing(float limbSwing)
	{
		this.limbSwing = limbSwing;
	}
	
	public void setLimbSwingAmount(float limbSwingAmount)
	{
		this.limbSwingAmount = limbSwingAmount;
	}
	
	public void setHeadYaw(float headYaw)
	{
		this.headYaw = headYaw;
	}
	
	public void setHeadPitch(float headPitch)
	{
		this.headPitch = headPitch;
	}
	
	public float getClimbingCycle() { return this.climbingCycle; }
	public float getTicksAfterLiftoff() { return this.ticksAfterLiftoff; }
	public float getTicksAfterTouchdown() { return this.ticksAfterTouchdown; }
	public float getTicksAfterAttack() { return this.ticksAfterAttack; }
	public float getLimbSwing() { return this.limbSwing; }
	public float getLimbSwingAmount() { return this.limbSwingAmount; }
	public float getHeadYaw() { return this.headYaw; }
	public float getHeadPitch() { return this.headPitch; }
	public boolean isClimbing() { return this.climbing; }
	
	@Override
	public void update(float partialTicks)
	{
		super.update(partialTicks);
		
		if(this.calcOnGround() & !this.onGround)
		{
			this.onTouchdown();
			this.onGround = true;
		}
		if((!this.calcOnGround() & this.onGround) | (this.previousMotion.y <= 0 && this.motion.y - this.previousMotion.y > 0.4f && this.ticksAfterLiftoff > 2f))
		{
			this.onLiftoff();
			this.onGround = false;
		}
		
		if(this.calcClimbing())
		{
			this.climbingCycle += DataUpdateHandler.ticksPerFrame*this.motion.y*2.6f;
			this.climbing = true;
		}
		else
		{
			this.climbing = false;
		}
			
		
		if(this.entity instanceof EntityLivingBase) {
			if(((EntityLivingBase)this.entity).swingProgress > 0)
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
		}
		
		if(this.previousMotion.y <= 0 && this.motion.y > 0)
		{
			this.onThrowup();
		}
		
		if(!this.isOnGround()) this.ticksAfterLiftoff += DataUpdateHandler.ticksPerFrame;
		if(this.isOnGround()) this.ticksAfterTouchdown += DataUpdateHandler.ticksPerFrame;
		this.ticksAfterAttack += DataUpdateHandler.ticksPerFrame;
		this.ticksAfterThrowup += DataUpdateHandler.ticksPerFrame;
	}
	
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
		this.ticksAfterAttack = 0.0F;
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
		
    	float clientY = (float) (entity.posY + (entity.posY-entity.prevPosY) * EntityRenderHandler.partialTicks);
    	
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
}
