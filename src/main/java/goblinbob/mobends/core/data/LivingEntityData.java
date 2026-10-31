package goblinbob.mobends.core.data;


import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.GUtil;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public abstract class LivingEntityData<E extends EntityLivingBase> extends EntityData<E>
{

    protected float ticksInAir;
    protected float ticksAfterTouchdown;
    protected float ticksAfterAttack;
    /** See {@link #setAttackComboTicks}. */
    protected float attackComboTicks;
    protected float ticksFalling;
    protected float climbingCycle = 0F;
    protected boolean alreadyAttacked = false;
    protected boolean climbing = false;

    public float limbSwing;
    public float limbSwingAmount;
    public float swingProgress;
    public float headYaw;
    /**
     * The body yaw, interpolated, as the frame begins (see {@link #getBodyYaw}), and the frame it
     * was taken in.
     */
    private float bodyYaw;
    private int bodyYawFrame = -1;
    /** The head's yaw and pitch the animator last ran with (see {@link #getHeadYawSinceAnimated}). */
    private float animatedHeadYaw, animatedHeadPitch;
    public float headPitch;

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

    @Override
    protected void registerKumoBindings()
    {
        super.registerKumoBindings();

        // The arguments the renderer passes the model, captured by the mutator (the swing progress
        // interpolated by partialTicks, as vanilla's getSwingProgress does).
        registerVariable("entityLimbSwing", () -> limbSwing);
        registerVariable("entityLimbSwingAmount", () -> limbSwingAmount);
        registerVariable("entitySwingProgress", () -> swingProgress);
        registerVariable("entityHeadYaw", () -> headYaw);
        registerVariable("entityHeadPitch", () -> headPitch);
        registerVariable("entityTicksInAir", () -> ticksInAir);
        registerVariable("entityTicksAfterTouchdown", () -> ticksAfterTouchdown);
        registerVariable("entityTicksAfterAttack", () -> ticksAfterAttack);
        registerVariable("entityTicksFalling", () -> ticksFalling);
        registerVariable("entityClimbingCycle", () -> climbingCycle);
        registerVariable("entityHealth", () -> entity != null ? entity.getHealth() : 0);
        registerVariable("entityLedgeHeight", this::getLedgeHeight);
        registerVariable("entityClimbingRotation", this::getClimbingRotation);
        // Vanilla's names are backwards: getItemInUseMaxCount is the ticks used so far.
        registerVariable("entityItemUseTicks", () -> entity != null ? entity.getItemInUseMaxCount() : 0);
        registerVariable("entityItemUseTicksLeft", () -> entity != null ? entity.getItemInUseCount() : 0);
        registerVariable("entityTicksElytraFlying", () -> entity != null ? entity.getTicksElytraFlying() : 0);

        registerState("entityIsClimbing", this::isClimbing);
        registerState("entityIsDrawingBow", this::isDrawingBow);
        registerState("entityIsSwinging", () -> entity != null && entity.isSwingInProgress);
        registerState("entityIsChild", () -> entity != null && entity.isChild());
        registerState("entityIsRidingLiving", () -> entity != null && entity.getRidingEntity() instanceof EntityLivingBase);
        registerState("entityIsLeftHanded", () -> entity != null && entity.getPrimaryHand() == EnumHandSide.LEFT);
        registerState("entityIsSleeping", () -> entity != null && entity.isEntityAlive() && entity.isPlayerSleeping());
        registerState("entityIsElytraFlying", () -> entity != null && entity.isElytraFlying());

        // The yaw vanilla turns the body to, interpolated as the renderer does.
        registerVariable("entityBodyYaw", this::getBodyYaw);
        registerVariable("entityClimbingRenderYaw", () -> entity != null ? MathHelper.wrapDegrees(entity.rotationYaw - headYaw - getClimbingRotation()) : 0);
        registerVariable("entityClimbingBodyYaw", () -> entity != null
                ? MathHelper.wrapDegrees(headYaw + MathHelper.wrapDegrees(entity.rotationYaw - headYaw - getClimbingRotation())) : 0);
        registerVariable("entityClimbingHeadYaw", () -> {
            if (entity == null) return 0;
            float renderRotationY = MathHelper.wrapDegrees(entity.rotationYaw - headYaw - getClimbingRotation());
            return Math.max(-90F, Math.min(90F, MathHelper.wrapDegrees(headYaw + renderRotationY)));
        });
        registerVariable("entityRidingRelativeHeadYaw", () -> {
            if (entity == null || !(entity.getRidingEntity() instanceof EntityLivingBase)) return 0;
            return MathHelper.wrapDegrees(entity.rotationYaw - ((EntityLivingBase) entity.getRidingEntity()).renderYawOffset);
        });
        registerVariable("entityRidingRelativeYaw", () -> {
            if (entity == null || !(entity.getRidingEntity() instanceof EntityLivingBase)) return 0;
            return MathHelper.wrapDegrees(entity.rotationYaw - headYaw - ((EntityLivingBase) entity.getRidingEntity()).renderYawOffset);
        });
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

        if (this.entity.isSwingInProgress)
        {
            if (!this.alreadyAttacked || this.ticksAfterAttack > 5.0F)
            {
                this.onAttack();
                this.alreadyAttacked = true;
            }
        }
        else
        {
            this.alreadyAttacked = false;
        }
    }

    /**
     * The yaw vanilla turns the body to in the world, interpolated as the renderer does. Taken as
     * the frame begins: the inventory turns the player to face the mouse while it draws it (setting
     * {@code renderYawOffset}, not the previous one), so read then it is neither yaw, and the
     * animators that keep the feet in the world (core:step_turn) would see the body spin. Entities
     * not updated this frame (the previews in the Main Menu) read it as it is.
     */
    public float getBodyYaw()
    {
        if (entity == null)
        {
            return 0;
        }
        if (bodyYawFrame == DataUpdateHandler.frame)
        {
            return bodyYaw;
        }
        return GUtil.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, DataUpdateHandler.partialTicks);
    }

    @Override
    public boolean animate()
    {
        if (!super.animate())
        {
            return false;
        }
        animatedHeadYaw = headYaw;
        animatedHeadPitch = headPitch;
        return true;
    }

    /**
     * Degrees the head looks further than the animator posed it: 0 for the draw the animator ran
     * in, and in a draw after it in the same frame (the player in the inventory, turned to face the
     * mouse), how much more that draw has the head look.
     */
    public float getHeadYawSinceAnimated()
    {
        return MathHelper.wrapDegrees(headYaw - animatedHeadYaw);
    }

    /** See {@link #getHeadYawSinceAnimated}. */
    public float getHeadPitchSinceAnimated()
    {
        return MathHelper.wrapDegrees(headPitch - animatedHeadPitch);
    }

    @Override
    public void update(float partialTicks)
    {
        super.update(partialTicks);

        if (this.entity != null)
        {
            this.bodyYaw = GUtil.interpolateRotation(this.entity.prevRenderYawOffset, this.entity.renderYawOffset, partialTicks);
            this.bodyYawFrame = DataUpdateHandler.frame;
        }

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

    /**
     * Counts a swing: {@code entityTicksAfterAttack} goes back to 0, unless the swing continues a
     * combo (see {@link #attackComboTicks}).
     */
    public void onAttack()
    {
        if (attackComboTicks > 0 && ticksAfterAttack <= attackComboTicks
                && entity.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.AIR)
        {
            return;
        }
        this.ticksAfterAttack = 0.0F;
    }

    /**
     * A swing within this many ticks of the last counted one, while the main hand holds an item,
     * isn't counted: it continues the combo instead of starting a new one (the animator reacts to
     * {@code entityTicksAfterAttack} going back to 0). Punches always count; 0 counts every swing.
     */
    public void setAttackComboTicks(float attackComboTicks)
    {
        this.attackComboTicks = attackComboTicks;
    }

    public float getClimbingRotation()
    {
        return getLadderFacing().getHorizontalAngle() + 180.0F;
    }

    private static boolean isBlockClimbable(IBlockState state)
    {
        return state.getBlock() instanceof BlockLadder || state.getBlock() instanceof BlockVine;
    }

    private static EnumFacing getClimbableBlockFacing(IBlockState state)
    {
        if (state.getBlock() instanceof BlockLadder)
        {
            return state.getValue(BlockLadder.FACING);
        }
        else if (state.getBlock() instanceof BlockVine)
        {
            if (state.getValue(BlockVine.EAST))
                return EnumFacing.WEST;
            else if (state.getValue(BlockVine.WEST))
                return EnumFacing.EAST;
            else if (state.getValue(BlockVine.NORTH))
                return EnumFacing.SOUTH;
            else if (state.getValue(BlockVine.SOUTH))
                return EnumFacing.NORTH;
        }

        return EnumFacing.NORTH;
    }

    public EnumFacing getLadderFacing()
    {
        BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position);
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));

        EnumFacing facing = EnumFacing.NORTH;
        facing = getClimbableBlockFacing(block);
        if (facing == EnumFacing.NORTH)
            facing = getClimbableBlockFacing(blockBelow);
        if (facing == EnumFacing.NORTH)
            facing = getClimbableBlockFacing(blockBelow2);

        return facing;
    }

    public boolean calcClimbing()
    {
        if (entity == null || entity.world == null)
            return false;

        BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position);
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, -1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, -2, 0));

        return entity.isOnLadder() && !this.isOnGround() && (isBlockClimbable(block) || isBlockClimbable(blockBelow) || isBlockClimbable(blockBelow2));
    }

    public float getLedgeHeight()
    {
        float clientY = (float) (entity.posY + (entity.posY - entity.prevPosY) * DataUpdateHandler.partialTicks);

        final BlockPos position = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));

        IBlockState block = entity.world.getBlockState(position.add(0, 2, 0));
        IBlockState blockBelow = entity.world.getBlockState(position.add(0, 1, 0));
        IBlockState blockBelow2 = entity.world.getBlockState(position.add(0, 0, 0));
        if (!isBlockClimbable(block))
        {
            if (!isBlockClimbable(blockBelow))
            {
                if (!isBlockClimbable(blockBelow2))
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
