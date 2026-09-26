package goblinbob.mobends.lab.sim;

import net.minecraft.item.ItemStack;

/**
 * The per-tick control inputs of a scripted entity. A scenario fills one of these for every tick;
 * {@link ScriptedEntity} integrates them into the vanilla entity fields the mod reads.
 */
public class EntityInputs
{
    /** Horizontal speed in blocks per tick along the body yaw (positive = forward). */
    public double forwardSpeed = 0;
    /** Horizontal speed in blocks per tick to the right of the body yaw. */
    public double strafeSpeed = 0;
    /** True on the tick the entity should leave the ground with the vanilla jump impulse. */
    public boolean jump = false;
    /** Direct vertical velocity override (blocks per tick), e.g. for flying or swimming. */
    public Double verticalSpeed = null;
    /** Disables gravity and floor collision (flying, swimming, riding). */
    public boolean noGravity = false;

    public float bodyYaw = 0;
    public float headYaw = 0;
    public float headPitch = 0;

    public boolean sprinting = false;
    public boolean sneaking = false;
    public boolean inWater = false;
    public boolean onLadder = false;
    /** Ladder blocks in the entity's column (needed for the mod's climb / ledge queries). */
    public boolean ladderColumn = false;
    /** Height of that ladder in blocks. */
    public int ladderHeight = 40;
    /** Water blocks around the entity up to this height (0 = none); makes isUnderwater() true. */
    public int waterHeight = 0;
    public boolean flying = false;
    public boolean sleeping = false;
    public boolean riding = false;
    /** Whether the mount is a living entity (horse) or not (boat / minecart). */
    public boolean ridingLiving = true;
    public int elytraTicks = 0;

    /** True on the tick an arm swing (attack) starts. */
    public boolean attack = false;
    /** Number of ticks the active item has been in use (0 = not using). */
    /** A left-handed entity (the attack bits mirror on it). */
    public boolean leftHanded = false;
    /** The hand the item in use is held in. */
    public net.minecraft.util.EnumHand activeHand = net.minecraft.util.EnumHand.MAIN_HAND;
    public int itemUseCount = 0;
    public int itemUseMaxCount = 0;

    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;

    // Species specific.
    public boolean wolfSitting = false;
    public float wolfInterested = 0;
    public float wolfShaking = 0;
    public boolean spiderClimbing = false;
    public float squidRotation = 0;
    public float health = 20;

    /** Puts every input back to its default (scripts fill them in again each tick). */
    public void reset()
    {
        EntityInputs fresh = new EntityInputs();
        // Keep object identity so scripts can hold onto it; copy defaults over.
        this.forwardSpeed = fresh.forwardSpeed;
        this.strafeSpeed = fresh.strafeSpeed;
        this.jump = fresh.jump;
        this.verticalSpeed = fresh.verticalSpeed;
        this.noGravity = fresh.noGravity;
        this.bodyYaw = fresh.bodyYaw;
        this.headYaw = fresh.headYaw;
        this.headPitch = fresh.headPitch;
        this.sprinting = fresh.sprinting;
        this.sneaking = fresh.sneaking;
        this.inWater = fresh.inWater;
        this.onLadder = fresh.onLadder;
        this.ladderColumn = fresh.ladderColumn;
        this.waterHeight = fresh.waterHeight;
        this.flying = fresh.flying;
        this.sleeping = fresh.sleeping;
        this.riding = fresh.riding;
        this.ridingLiving = fresh.ridingLiving;
        this.elytraTicks = fresh.elytraTicks;
        this.attack = fresh.attack;
        this.itemUseCount = fresh.itemUseCount;
        this.itemUseMaxCount = fresh.itemUseMaxCount;
        this.mainHand = fresh.mainHand;
        this.offHand = fresh.offHand;
        this.wolfSitting = fresh.wolfSitting;
        this.wolfInterested = fresh.wolfInterested;
        this.wolfShaking = fresh.wolfShaking;
        this.spiderClimbing = fresh.spiderClimbing;
        this.squidRotation = fresh.squidRotation;
        this.health = fresh.health;
    }

}
