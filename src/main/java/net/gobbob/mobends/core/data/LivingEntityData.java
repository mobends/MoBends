package net.gobbob.mobends.core.data;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class LivingEntityData<E extends EntityLivingBase> extends EntityData<E>
{

    protected float ticksInAir;
    protected float ticksAfterTouchdown;
    protected float ticksAfterAttack;
    protected float ticksFalling;
    protected float climbingCycle = 0F;
    protected boolean alreadyPunched = false;
    protected boolean climbing = false;

    public OverridableProperty<Float> limbSwing = new OverridableProperty<>(0F);
    public OverridableProperty<Float> limbSwingAmount = new OverridableProperty<>(0F);
    public OverridableProperty<Float> headYaw = new OverridableProperty<>(0F);
    public OverridableProperty<Float> headPitch = new OverridableProperty<>(0F);

    public LivingEntityData(E entity)
    {
        super(entity);

        // Setting high values for ticks* variables
        // to avoid premature animation triggers.
        // (like the automatic attack stance on creation)
        this.ticksInAir = 100F;
        this.ticksAfterTouchdown = 100F;
        this.ticksAfterAttack = 100F;
        this.ticksFalling = 100F;
    }

    public void setClimbing(boolean flag)
    {
        this.climbing = flag;
    }

    public float getClimbingCycle() { return this.climbingCycle; }

    public float getTicksInAir() { return this.ticksInAir; }

    public float getTicksAfterTouchdown() { return this.ticksAfterTouchdown; }

    public float getTicksAfterAttack() { return this.ticksAfterAttack; }

    public float getTicksFalling() { return this.ticksFalling; }

    public boolean isClimbing() { return this.climbing; }

    @Override
    public void updateClient()
    {
        super.updateClient();

        final boolean calcOnGroundResult = this.calcOnGround();
        if (calcOnGroundResult & !this.onGround)
        {
            this.onTouchdown();
            this.onGround = true;
        }

        if ((!calcOnGroundResult & this.onGround) | (this.prevMotionY <= 0 && this.motionY - this.prevMotionY > 0.4D && this.ticksInAir > 2.0F))
        {
            this.onLiftoff();
            this.onGround = false;
        }

        if (this.calcClimbing())
        {
            this.climbingCycle += this.motionY * 2.6F;
            this.climbing = true;
        }
        else
        {
            this.climbing = false;
        }

        if (this.entity.swingProgress > 0)
        {
            if (!this.alreadyPunched)
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

    @Override
    public void update(float partialTicks)
    {
        super.update(partialTicks);

        if (this.isOnGround())
        {
            this.ticksAfterTouchdown += DataUpdateHandler.ticksPerFrame;
        }
        else
        {
            this.ticksInAir += DataUpdateHandler.ticksPerFrame;

            if (this.motionY < 0.0D)
            {
                this.ticksFalling += DataUpdateHandler.ticksPerFrame;
            }
            else
            {
                this.ticksFalling = 0.0F;
            }
        }

        this.ticksAfterAttack += DataUpdateHandler.ticksPerFrame;
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

    public void onPunch()
    {
        this.ticksAfterAttack = 0.0F;
    }

    public float getClimbingRotation()
    {
        return getLadderFacing().getHorizontalAngle() + 180.0F;
    }

    public EnumFacing getLadderFacing()
    {
        BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position);
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));

        if (block.getBlock() instanceof BlockLadder) return block.getValue(BlockLadder.FACING);
        else if (blockBelow.getBlock() instanceof BlockLadder) return blockBelow.getValue(BlockLadder.FACING);
        else if (blockBelow2.getBlock() instanceof BlockLadder) return blockBelow2.getValue(BlockLadder.FACING);
        return EnumFacing.NORTH;
    }

    public boolean calcClimbing()
    {
        if (entity == null || entity.world == null)
            return false;

        BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position);
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));

        return entity.isOnLadder() && !this.isOnGround() && (block.getBlock() instanceof BlockLadder || blockBelow.getBlock() instanceof BlockLadder || blockBelow2.getBlock() instanceof BlockLadder);
    }

    public float getLedgeHeight()
    {
        float clientY = (float) (entity.posY + (entity.posY - entity.prevPosY) * DataUpdateHandler.partialTicks);

        final BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position.add(0, 2, 0));
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, 1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, 0, 0));
        if (!(block.getBlock() instanceof BlockLadder))
        {
            if (!(blockBelow.getBlock() instanceof BlockLadder))
            {
                if (!(blockBelow2.getBlock() instanceof BlockLadder))
                    return (clientY - (int) clientY) + 2;
                else
                    return (clientY - (int) clientY) + 1;
            }
            else
            {
                return clientY - (int) clientY;
            }
        }

        return -2.0F;
    }

    public boolean isDrawingBow()
    {
        if (entity.getItemInUseCount() > 0)
        {
            ItemStack mainItemStack = entity.getHeldItemMainhand();
            ItemStack offItemStack = entity.getHeldItemOffhand();
            if ((!mainItemStack.isEmpty() && mainItemStack.getItemUseAction() == EnumAction.BOW)
                    || (!offItemStack.isEmpty() && offItemStack.getItemUseAction() == EnumAction.BOW))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public E getEntity()
    {
        return this.entity;
    }

}
