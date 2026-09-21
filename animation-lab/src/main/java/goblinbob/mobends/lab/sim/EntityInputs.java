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
    public boolean flying = false;
    public boolean sleeping = false;
    public boolean riding = false;
    public int elytraTicks = 0;

    /** True on the tick an arm swing (attack) starts. */
    public boolean attack = false;
    /** Number of ticks the active item has been in use (0 = not using). */
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
}
