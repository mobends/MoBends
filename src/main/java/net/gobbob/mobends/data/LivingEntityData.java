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
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class LivingEntityData<T extends LivingEntityData, E extends EntityLivingBase> extends EntityData<T, E>
{
	protected float ticksInAir;
    protected float ticksAfterTouchdown;
	protected float ticksAfterAttack;
	protected float ticksAfterThrowup;
	protected float ticksFalling;
	protected float climbingCycle = 0F;
	protected boolean alreadyPunched = false;
	protected boolean climbing = false;
	protected float limbSwing = 0F;
	protected float limbSwingAmount = 0F;
	protected float headYaw = 0F;
	protected float headPitch = 0F;
	
	public LivingEntityData(E entity)
	{
		super(entity);
		
		// Setting high values for ticksAfter* variables
		// to avoid premature animation triggers.
		// (like the automatic attack stance on creation)
		this.ticksInAir = 100F;
		this.ticksAfterTouchdown = 100F;
		this.ticksAfterAttack = 100F;
		this.ticksAfterThrowup = 100F;
		this.ticksFalling = 100F;
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
	public float getTicksInAir() { return this.ticksInAir; }
	public float getTicksAfterTouchdown() { return this.ticksAfterTouchdown; }
	public float getTicksAfterAttack() { return this.ticksAfterAttack; }
	public float getTicksFalling() { return this.ticksFalling; }
	public float getLimbSwing() { return this.limbSwing; }
	public float getLimbSwingAmount() { return this.limbSwingAmount; }
	public float getHeadYaw() { return this.headYaw; }
	public float getHeadPitch() { return this.headPitch; }
	public boolean isClimbing() { return this.climbing; }
	
	@Override
	public void update(float partialTicks)
	{
		super.update(partialTicks);
		
		if (this.calcOnGround() & !this.onGround)
		{
			this.onTouchdown();
			this.onGround = true;
		}
		
		if ((!this.calcOnGround() & this.onGround) | (this.prevMotionY <= 0 && this.motionY - this.prevMotionY > 0.4D && this.ticksInAir > 2.0F))
		{
			this.onLiftoff();
			this.onGround = false;
		}
		
		if (this.calcClimbing())
		{
			this.climbingCycle += DataUpdateHandler.ticksPerFrame * this.motionY * 2.6F;
			this.climbing = true;
		}
		else
		{
			this.climbing = false;
		}
			
		
		if(this.entity instanceof EntityLivingBase)
		{
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
		
		if(this.prevMotionY <= 0.0D && this.motionY > 0.0D)
		{
			this.onThrowup();
		}
		
		if (!this.isOnGround()) {
			this.ticksInAir += DataUpdateHandler.ticksPerFrame;
			if (this.motionY < 0.0D) {
				this.ticksFalling += DataUpdateHandler.ticksPerFrame;
			} else {
				this.ticksFalling = 0.0F;
			}
		}
		if (this.isOnGround()) this.ticksAfterTouchdown += DataUpdateHandler.ticksPerFrame;
		this.ticksAfterAttack += DataUpdateHandler.ticksPerFrame;
		this.ticksAfterThrowup += DataUpdateHandler.ticksPerFrame;
	}
	
	public void onTouchdown()
	{
		this.ticksAfterTouchdown = 0.0F;
		this.ticksFalling = 0.0F;
	}
	
	public void onLiftoff()
	{
		this.ticksInAir = 0.0F;
	}
	
	public void onThrowup()
	{
		this.ticksAfterThrowup = 0.0F;
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
		if(entity == null || entity.world == null) return false;
		
		BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
		
		IBlockState block = entity.world.getBlockState(position);
		IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
		IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));
		
		return entity.isOnLadder() && !this.isOnGround() && (block.getBlock() instanceof BlockLadder || blockBelow.getBlock() instanceof BlockLadder || blockBelow2.getBlock() instanceof BlockLadder);
	}
	
	public float getLedgeHeight()
	{
    	float clientY = (float) (entity.posY + (entity.posY-entity.prevPosY) * DataUpdateHandler.partialTicks);
    	
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
	
	public boolean isDrawingBow()
	{
        if (entity.getItemInUseCount() > 0)
        {
        	ItemStack itemstack = entity.getHeldItemMainhand();
            ItemStack itemstack1 = entity.getHeldItemOffhand();
	        if ((!itemstack.isEmpty() && itemstack.getItemUseAction() == EnumAction.BOW) ||
	        	(!itemstack1.isEmpty() && itemstack1.getItemUseAction() == EnumAction.BOW))
	        {
	        	return true;
	        }
		}
        return false;
	}
}
