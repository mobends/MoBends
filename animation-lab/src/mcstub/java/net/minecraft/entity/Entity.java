package net.minecraft.entity;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Scriptable stand-in for the game's Entity. Public fields mirror the ones the mod reads; the
 * booleans are plain flags the lab sets from a scenario script.
 */
public class Entity
{
    private static int nextId = 1;

    public World world;
    public double posX, posY, posZ;
    public double prevPosX, prevPosY, prevPosZ;
    public double motionX, motionY, motionZ;
    public float rotationYaw, rotationPitch;
    public float prevRotationYaw, prevRotationPitch;
    public int ticksExisted;
    public float width = 0.6F, height = 1.8F;

    public boolean inWater, sneaking, sprinting, alive = true, onLadder;
    public Entity ridingEntity;
    public boolean riderShouldSit = true;

    private final int entityId;

    public Entity(World world)
    {
        this.world = world;
        this.entityId = nextId++;
        if (world != null) world.addEntity(this);
    }

    public int getEntityId() { return entityId; }

    /** Lab only: makes entity ids deterministic per scenario. */
    public static void resetIds() { nextId = 1; }

    public AxisAlignedBB getEntityBoundingBox()
    {
        double hw = width / 2.0D;
        return new AxisAlignedBB(posX - hw, posY, posZ - hw, posX + hw, posY + height, posZ + hw);
    }

    public boolean isInWater() { return inWater; }
    public boolean isSneaking() { return sneaking; }
    public boolean isSprinting() { return sprinting; }
    public boolean isEntityAlive() { return alive; }
    public boolean isOnLadder() { return onLadder; }
    public boolean isRiding() { return ridingEntity != null; }
    public Entity getRidingEntity() { return ridingEntity; }
    public boolean shouldRiderSit() { return riderShouldSit; }

    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch)
    {
        posX = prevPosX = x;
        posY = prevPosY = y;
        posZ = prevPosZ = z;
        rotationYaw = prevRotationYaw = yaw;
        rotationPitch = prevRotationPitch = pitch;
    }

    /** Same maths as the game's Entity.getLook(1.0F). */
    public Vec3d getLookVec()
    {
        float f = MathHelper.cos(-rotationYaw * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-rotationYaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-rotationPitch * 0.017453292F);
        float f3 = MathHelper.sin(-rotationPitch * 0.017453292F);
        return new Vec3d((double) (f1 * f2), (double) f3, (double) (f * f2));
    }
}
