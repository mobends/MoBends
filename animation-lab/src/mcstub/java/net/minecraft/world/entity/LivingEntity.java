package net.minecraft.world.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Scriptable stand-in for the game's LivingEntity (see {@link Entity}). */
public class LivingEntity extends Entity {
    public float yBodyRot, yBodyRotO;
    public float yHeadRot, yHeadRotO;
    public final WalkAnimationState walkAnimation = new WalkAnimationState();
    public boolean swinging;
    public int swingTime;
    public float oAttackAnim, attackAnim;
    public int hurtTime;
    public int deathTime;

    private float health = 20.0F;
    private boolean baby;
    private HumanoidArm mainArm = HumanoidArm.RIGHT;
    private ItemStack mainHandItem = ItemStack.EMPTY;
    private ItemStack offhandItem = ItemStack.EMPTY;
    private InteractionHand usedItemHand = InteractionHand.MAIN_HAND;
    /** Lab: set by the script, as the game counts them down while an item is used. */
    public int useItemRemaining;
    /** Lab: set by the script (the game works it out from the item's use duration and {@link #useItemRemaining}). */
    public int ticksUsingItem;
    /** Lab: set by the script, as the game counts them while the entity glides; it glides while they count. */
    public int fallFlyTicks;
    /** Lab: set by the script (the game looks for a ladder or a vine at the feet). */
    public boolean onLadder;

    public LivingEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    @Override
    public boolean isAlive() {
        return super.isAlive() && getHealth() > 0.0F;
    }

    public boolean isBaby() {
        return baby;
    }

    /** Lab: only some of the game's living entities can be babies. */
    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public HumanoidArm getMainArm() {
        return mainArm;
    }

    /** Lab: the game's players have it; a mob is left-handed instead. */
    public void setMainArm(HumanoidArm mainArm) {
        this.mainArm = mainArm;
    }

    public ItemStack getItemInHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? mainHandItem : offhandItem;
    }

    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            mainHandItem = stack;
        } else {
            offhandItem = stack;
        }
    }

    public ItemStack getMainHandItem() {
        return mainHandItem;
    }

    public ItemStack getOffhandItem() {
        return offhandItem;
    }

    public InteractionHand getUsedItemHand() {
        return usedItemHand;
    }

    /** Lab: the game sets it as the entity starts using an item. */
    public void setUsedItemHand(InteractionHand hand) {
        this.usedItemHand = hand;
    }

    public boolean isUsingItem() {
        return useItemRemaining > 0;
    }

    /** The item being used, or nothing. */
    public ItemStack getUseItem() {
        return isUsingItem() ? getItemInHand(usedItemHand) : ItemStack.EMPTY;
    }

    public int getUseItemRemainingTicks() {
        return useItemRemaining;
    }

    public int getTicksUsingItem() {
        return isUsingItem() ? ticksUsingItem : 0;
    }

    public boolean isSleeping() {
        return hasPose(Pose.SLEEPING);
    }

    public boolean isFallFlying() {
        return fallFlyTicks > 0;
    }

    public int getFallFlyingTicks() {
        return fallFlyTicks;
    }

    public boolean onClimbable() {
        return onLadder;
    }

    /** The swing's progress, interpolated: 0 to 1 over a swing, as the game has it. */
    public float getAttackAnim(float partialTick) {
        float f = attackAnim - oAttackAnim;
        if (f < 0.0F) {
            ++f;
        }
        return oAttackAnim + f * partialTick;
    }
}
