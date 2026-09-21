package goblinbob.mobends.core.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
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
    protected float ticksFalling;
    protected float climbingCycle = 0F;
    protected boolean alreadyAttacked = false;
    protected boolean climbing = false;

    public OverridableProperty<Float> limbSwing = new OverridableProperty<>(0F);
    public OverridableProperty<Float> limbSwingAmount = new OverridableProperty<>(0F);
    public OverridableProperty<Float> swingProgress = new OverridableProperty<>(0F);
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

    @Override
    protected void registerKumoBindings()
    {
        super.registerKumoBindings();

        registerVariable("limbSwing", () -> limbSwing.get());
        registerVariable("limbSwingAmount", () -> limbSwingAmount.get());
        registerVariable("swingProgress", () -> swingProgress.get());
        registerVariable("entitySwingProgress", () -> entity != null ? entity.swingProgress : 0);
        registerVariable("headYaw", () -> headYaw.get());
        registerVariable("headPitch", () -> headPitch.get());
        registerVariable("ticksInAir", () -> ticksInAir);
        registerVariable("ticksAfterTouchdown", () -> ticksAfterTouchdown);
        registerVariable("ticksAfterAttack", () -> ticksAfterAttack);
        registerVariable("ticksAfterPunch", () -> ticksAfterAttack);
        registerVariable("ticksFalling", () -> ticksFalling);
        registerVariable("climbingCycle", () -> climbingCycle);
        registerVariable("health", () -> entity != null ? entity.getHealth() : 0);
        registerVariable("ledgeHeight", this::getLedgeHeight);
        registerVariable("climbingRotation", this::getClimbingRotation);
        registerVariable("itemUseCount", () -> entity != null ? entity.getItemInUseCount() : 0);
        registerVariable("itemUseMaxCount", () -> entity != null ? entity.getItemInUseMaxCount() : 0);

        registerState("CLIMBING", this::isClimbing);
        registerState("DRAWING_BOW", this::isDrawingBow);
        registerState("SWINGING", () -> entity != null && entity.isSwingInProgress);
        registerState("CHILD", () -> entity != null && entity.isChild());
        registerState("RIDING_LIVING", () -> entity != null && entity.getRidingEntity() instanceof EntityLivingBase);
        registerState("LEFT_HANDED", () -> entity != null && entity.getPrimaryHand() == EnumHandSide.LEFT);

        // Derived inputs the procedural bits compute inline; exposed so animators stay data.
        registerVariable("rotationYaw", () -> entity != null ? entity.rotationYaw : 0);
        registerVariable("headYawAbs", () -> Math.abs(headYaw.get()));
        registerVariable("climbingRenderYaw", () -> entity != null ? MathHelper.wrapDegrees(entity.rotationYaw - headYaw.get() - getClimbingRotation()) : 0);
        registerVariable("climbingHeadYaw", () -> {
            if (entity == null) return 0;
            float renderRotationY = MathHelper.wrapDegrees(entity.rotationYaw - headYaw.get() - getClimbingRotation());
            return Math.max(-90F, Math.min(90F, MathHelper.wrapDegrees(headYaw.get() + renderRotationY)));
        });
        registerVariable("ridingRelativeHeadYaw", () -> {
            if (entity == null || !(entity.getRidingEntity() instanceof EntityLivingBase)) return 0;
            return MathHelper.wrapDegrees(entity.rotationYaw - ((EntityLivingBase) entity.getRidingEntity()).renderYawOffset);
        });
        registerVariable("ridingRelativeYaw", () -> {
            if (entity == null || !(entity.getRidingEntity() instanceof EntityLivingBase)) return 0;
            return MathHelper.wrapDegrees(entity.rotationYaw - headYaw.get() - ((EntityLivingBase) entity.getRidingEntity()).renderYawOffset);
        });
        registerVariable("entityXZSpeed", () -> entity != null ? Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ) : 0);
        registerVariable("aimedBowTicks", () -> entity != null ? Math.min(entity.getItemInUseMaxCount(), 15) : 0);
    }

    // --- string-valued inputs -----------------------------------------------------------------

    @Override
    public String getProperty(String name)
    {
        if (entity == null) return null;
        switch (name)
        {
            case "mainHandItem": return itemName(entity.getHeldItemMainhand());
            case "offHandItem": return itemName(entity.getHeldItemOffhand());
            case "activeItem": return itemName(entity.getActiveItemStack());
            case "mainHandUseAction": return useAction(entity.getHeldItemMainhand());
            case "offHandUseAction": return useAction(entity.getHeldItemOffhand());
            case "activeHand": return entity.getActiveHand() == EnumHand.MAIN_HAND ? "MAIN_HAND" : "OFF_HAND";
            case "primaryHand": return entity.getPrimaryHand().name();
            case "activeHandSide": return (entity.getActiveHand() == EnumHand.MAIN_HAND ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite()).name();
            default: return null;
        }
    }

    private static String itemName(ItemStack stack)
    {
        if (stack == null || stack.isEmpty()) return null;
        net.minecraft.util.ResourceLocation key = Item.REGISTRY.getNameForObject(stack.getItem());
        return key == null ? null : key.toString();
    }

    private static String useAction(ItemStack stack)
    {
        if (stack == null || stack.isEmpty()) return null;
        return stack.getItemUseAction().name();
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

    public void onAttack()
    {
        this.ticksAfterAttack = 0.0F;
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
