package goblinbob.mobends.core.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The data of a living entity: what the renderer passes its model (captured each frame, see
 * {@code BendsRenderer}), and the counters the animators time things by (in the air, since the
 * last attack, ...).
 */
public abstract class LivingEntityData<E extends LivingEntity> extends EntityData<E> {
    protected float ticksInAir;
    protected float ticksAfterTouchdown;
    protected float ticksAfterAttack;
    /** See {@link #setAttackComboTicks}. */
    protected float attackComboTicks;
    protected float ticksFalling;
    protected float climbingCycle = 0F;
    protected float prevClimbingCycle = 0F;
    protected boolean alreadyAttacked = false;
    protected boolean climbing = false;
    /** Whether another player flies, which the client isn't told (see {@link RemoteFlight}). */
    private final RemoteFlight remoteFlight = new RemoteFlight();

    public float limbSwing;
    public float limbSwingAmount;
    public float swingProgress;
    public float headYaw;
    public float headPitch;
    /**
     * The body yaw, interpolated, as the frame begins (see {@link #getBodyYaw}), and the frame it
     * was taken in.
     */
    private float bodyYaw;
    private int bodyYawFrame = -1;
    /** The head's yaw and pitch the animator last ran with (see {@link #getHeadYawSinceAnimated}). */
    private float animatedHeadYaw, animatedHeadPitch;

    public LivingEntityData(E entity) {
        super(entity);

        // High values, so nothing that reacts to these going back to 0 (the attack stance) plays as
        // the data is made.
        this.ticksInAir = 100F;
        this.ticksAfterTouchdown = 100F;
        this.ticksAfterAttack = 100F;
        this.ticksFalling = 100F;
    }

    @Override
    protected void registerKumoBindings() {
        super.registerKumoBindings();

        // The arguments the renderer passes the model, captured by BendsRenderer (the swing progress
        // interpolated by partialTicks, as vanilla's getAttackAnim does).
        registerVariable("entityLimbSwing", () -> limbSwing);
        registerVariable("entityLimbSwingAmount", () -> limbSwingAmount);
        registerVariable("entitySwingProgress", () -> swingProgress);
        registerVariable("entityHeadYaw", () -> headYaw);
        registerVariable("entityHeadPitch", () -> headPitch);
        registerVariable("entityTicksInAir", () -> ticksInAir);
        registerVariable("entityTicksAfterTouchdown", () -> ticksAfterTouchdown);
        registerVariable("entityTicksAfterAttack", () -> ticksAfterAttack);
        registerVariable("entityTicksFalling", () -> ticksFalling);
        registerVariable("entityClimbingCycle", () -> Mth.lerp(DataUpdateHandler.partialTicks, prevClimbingCycle, climbingCycle));
        registerVariable("entityHealth", entity::getHealth);
        registerVariable("entityLedgeHeight", this::getLedgeHeight);
        registerVariable("entityClimbingRotation", this::getClimbingRotation);
        // Whole ticks, so they go on by partialTicks while they count.
        registerVariable("entityItemUseTicks", () -> entity.getUseItemRemainingTicks() > 0
                ? entity.getTicksUsingItem() + DataUpdateHandler.partialTicks : 0);
        registerVariable("entityItemUseTicksLeft", () -> entity.getUseItemRemainingTicks() > 0
                ? Math.max(0, entity.getUseItemRemainingTicks() - DataUpdateHandler.partialTicks) : 0);
        registerVariable("entityTicksElytraFlying", () -> entity.getFallFlyingTicks() > 0
                ? entity.getFallFlyingTicks() + DataUpdateHandler.partialTicks : 0);

        registerState("entityIsClimbing", this::isClimbing);
        registerState("entityIsDrawingBow", this::isDrawingBow);
        registerState("entityIsSwinging", () -> entity.swinging);
        registerState("entityIsChild", entity::isBaby);
        registerState("entityIsRidingLiving", () -> entity.getVehicle() instanceof LivingEntity);
        registerState("entityIsLeftHanded", () -> entity.getMainArm() == HumanoidArm.LEFT);
        registerState("entityIsSleeping", () -> entity.isAlive() && entity.isSleeping());
        registerState("entityIsElytraFlying", entity::isFallFlying);

        // The yaw vanilla turns the body to, interpolated as the renderer does.
        registerVariable("entityBodyYaw", this::getBodyYaw);
        registerVariable("entityClimbingRenderYaw", () -> Mth.wrapDegrees(getYaw() - headYaw - getClimbingRotation()));
        registerVariable("entityClimbingBodyYaw", () -> Mth.wrapDegrees(headYaw + Mth.wrapDegrees(getYaw() - headYaw - getClimbingRotation())));
        registerVariable("entityClimbingHeadYaw", () -> {
            float turnY = Mth.wrapDegrees(getYaw() - headYaw - getClimbingRotation());
            return Math.max(-90F, Math.min(90F, Mth.wrapDegrees(headYaw + turnY)));
        });
        registerVariable("entityRidingRelativeHeadYaw", () -> entity.getVehicle() instanceof LivingEntity mount
                ? Mth.wrapDegrees(getYaw() - getBodyYaw(mount)) : 0);
        registerVariable("entityRidingRelativeYaw", () -> entity.getVehicle() instanceof LivingEntity mount
                ? Mth.wrapDegrees(getYaw() - headYaw - getBodyYaw(mount)) : 0);
    }

    public boolean isClimbing() { return this.climbing; }

    /** Whether another player flies, as far as can be told from how they move (see {@link RemoteFlight}). */
    public boolean isFlyingRemotely() { return this.remoteFlight.isFlying(); }

    @Override
    public void updateClient() {
        super.updateClient();

        boolean onGroundNow = this.calcOnGround();
        if (onGroundNow && !this.onGround) {
            this.onTouchdown();
            this.onGround = true;
        }
        if ((!onGroundNow && this.onGround) || (this.prevMotionY <= 0 && this.motionY - this.prevMotionY > 0.4D && this.ticksInAir > 2.0F)) {
            this.onLiftoff();
            this.onGround = false;
        }

        this.prevClimbingCycle = this.climbingCycle;
        if (this.calcClimbing()) {
            this.climbingCycle += (float) (this.motionY * 2.6F);
            this.climbing = true;
        } else {
            this.climbing = false;
        }

        if (this.entity instanceof RemotePlayer player) {
            this.remoteFlight.update(player.isCreative() || player.isSpectator(),
                    !this.onGround && !this.climbing && !player.isPassenger() && !player.isFallFlying()
                            && !player.isInWater() && !player.isInLava(),
                    this.motionY);
        }

        if (this.entity.swinging) {
            if (!this.alreadyAttacked || this.ticksAfterAttack > 5.0F) {
                this.onAttack();
                this.alreadyAttacked = true;
            }
        } else {
            this.alreadyAttacked = false;
        }
    }

    /**
     * The yaw vanilla turns the body to in the world, interpolated as the renderer does. Taken as
     * the frame begins: the inventory turns the player to face the mouse while it draws it (setting
     * the body yaw, not the previous one), so read then it is neither yaw, and the animators that
     * keep the feet in the world (core:step_turn) would see the body spin.
     */
    public float getBodyYaw() {
        if (bodyYawFrame == DataUpdateHandler.frame) {
            return bodyYaw;
        }
        return getBodyYaw(entity);
    }

    private static float getBodyYaw(LivingEntity entity) {
        return Mth.rotLerp(DataUpdateHandler.partialTicks, entity.yBodyRotO, entity.yBodyRot);
    }

    /** The entity's yaw, interpolated as the renderer does. */
    private float getYaw() {
        return Mth.rotLerp(DataUpdateHandler.partialTicks, entity.yRotO, entity.getYRot());
    }

    @Override
    public boolean animate() {
        if (!super.animate()) {
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
    public float getHeadYawSinceAnimated() {
        return Mth.wrapDegrees(headYaw - animatedHeadYaw);
    }

    /** See {@link #getHeadYawSinceAnimated}. */
    public float getHeadPitchSinceAnimated() {
        return Mth.wrapDegrees(headPitch - animatedHeadPitch);
    }

    @Override
    public void update(float partialTicks) {
        super.update(partialTicks);

        this.bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        this.bodyYawFrame = DataUpdateHandler.frame;

        if (this.isOnGround()) {
            this.ticksAfterTouchdown += DataUpdateHandler.ticksPerFrame;
        } else {
            this.ticksInAir += DataUpdateHandler.ticksPerFrame;
            if (this.motionY < 0.0D) {
                this.ticksFalling += DataUpdateHandler.ticksPerFrame;
            } else {
                this.ticksFalling = 0.0F;
            }
        }

        this.ticksAfterAttack += DataUpdateHandler.ticksPerFrame;
    }

    public void onTouchdown() {
        this.ticksAfterTouchdown = 0.0F;
        this.ticksFalling = 0.0F;
    }

    public void onLiftoff() {
        this.ticksInAir = 0.0F;
    }

    /**
     * Counts a swing: {@code entityTicksAfterAttack} goes back to 0, unless the swing continues a
     * combo (see {@link #attackComboTicks}).
     */
    public void onAttack() {
        if (attackComboTicks > 0 && ticksAfterAttack <= attackComboTicks && !entity.getMainHandItem().isEmpty()) {
            return;
        }
        this.ticksAfterAttack = 0.0F;
    }

    /**
     * A swing within this many ticks of the last counted one, while the main hand holds an item,
     * isn't counted: it continues the combo instead of starting a new one (the animator reacts to
     * {@code entityTicksAfterAttack} going back to 0). Punches always count; 0 counts every swing.
     */
    public void setAttackComboTicks(float attackComboTicks) {
        this.attackComboTicks = attackComboTicks;
    }

    public float getClimbingRotation() {
        return getLadderFacing().toYRot() + 180.0F;
    }

    private static boolean isBlockClimbable(BlockState state) {
        return state.getBlock() instanceof LadderBlock || state.getBlock() instanceof VineBlock;
    }

    /** The way the climber faces on a ladder or a vine; north for neither. */
    private static Direction getClimbableBlockFacing(BlockState state) {
        if (state.getBlock() instanceof LadderBlock) {
            return state.getValue(LadderBlock.FACING);
        }
        if (state.getBlock() instanceof VineBlock) {
            if (state.getValue(VineBlock.EAST)) return Direction.WEST;
            if (state.getValue(VineBlock.WEST)) return Direction.EAST;
            if (state.getValue(VineBlock.NORTH)) return Direction.SOUTH;
            if (state.getValue(VineBlock.SOUTH)) return Direction.NORTH;
        }
        return Direction.NORTH;
    }

    public Direction getLadderFacing() {
        Level level = entity.level();
        BlockPos position = entity.blockPosition();
        Direction facing = getClimbableBlockFacing(level.getBlockState(position));
        if (facing == Direction.NORTH) facing = getClimbableBlockFacing(level.getBlockState(position.below()));
        if (facing == Direction.NORTH) facing = getClimbableBlockFacing(level.getBlockState(position.below(2)));
        return facing;
    }

    public boolean calcClimbing() {
        Level level = entity.level();
        BlockPos position = entity.blockPosition();
        return entity.onClimbable() && !this.isOnGround()
                && (isBlockClimbable(level.getBlockState(position))
                || isBlockClimbable(level.getBlockState(position.below()))
                || isBlockClimbable(level.getBlockState(position.below(2))));
    }

    /**
     * Where the ladder or vine climbed ends, for the clips that take the climber over its top: the
     * feet's height within their block, plus 1 or 2 when the climbable blocks stop that much lower
     * than two above the feet; -2 while they reach that high.
     */
    public float getLedgeHeight() {
        float clientY = (float) Mth.lerp(DataUpdateHandler.partialTicks, entity.yo, entity.getY());
        Level level = entity.level();
        BlockPos position = entity.blockPosition();
        if (!isBlockClimbable(level.getBlockState(position.above(2)))) {
            if (!isBlockClimbable(level.getBlockState(position.above()))) {
                if (!isBlockClimbable(level.getBlockState(position))) {
                    return (clientY - (int) clientY) + 2;
                }
                return (clientY - (int) clientY) + 1;
            }
            return clientY - (int) clientY;
        }
        return -2.0F;
    }

    public boolean isDrawingBow() {
        if (entity.getUseItemRemainingTicks() > 0) {
            return isBow(entity.getMainHandItem()) || isBow(entity.getOffhandItem());
        }
        return false;
    }

    private static boolean isBow(ItemStack stack) {
        return !stack.isEmpty() && stack.getUseAnimation() == UseAnim.BOW;
    }
}
