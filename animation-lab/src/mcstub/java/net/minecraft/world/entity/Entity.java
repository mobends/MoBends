package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scriptable stand-in for the game's Entity: its position and rotation (with the previous tick's),
 * and the state the mod reads. What the game keeps protected, or derives from its own state, is a
 * plain public field here for the lab to set from a scenario script.
 */
public class Entity {
    private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();

    private final EntityType<?> type;
    private int id = ENTITY_COUNTER.incrementAndGet();
    private final Level level;

    private double x, y, z;
    public double xo, yo, zo;
    private float yRot, xRot;
    public float yRotO, xRotO;
    public int tickCount;
    public float fallDistance;
    public float walkDistO, walkDist;

    @Nullable
    private Entity vehicle;
    private boolean removed;
    private boolean sprinting;
    private Pose pose = Pose.STANDING;
    /** Lab: set by the script (the game works it out from the water the entity touches). */
    public boolean wasTouchingWater;

    public Entity(EntityType<?> type, Level level) {
        this.type = type;
        this.level = level;
    }

    /** Lab only: makes entity ids deterministic per scenario (the next entity made gets id 1). */
    public static void resetIds() {
        ENTITY_COUNTER.set(0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EntityType<?> getType() {
        return type;
    }

    public Level level() {
        return level;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setPos(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Puts the entity at the position and rotation, the previous tick's included. */
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        setPos(x, y, z);
        setYRot(yRot);
        setXRot(xRot);
        xo = x;
        yo = y;
        zo = z;
        yRotO = yRot;
        xRotO = xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void setYRot(float yRot) {
        this.yRot = yRot;
    }

    public float getXRot() {
        return xRot;
    }

    public void setXRot(float xRot) {
        this.xRot = xRot;
    }

    public BlockPos blockPosition() {
        return BlockPos.containing(x, y, z);
    }

    public float getBbWidth() {
        return type.getWidth();
    }

    public float getBbHeight() {
        return type.getHeight();
    }

    public AABB getBoundingBox() {
        double halfWidth = getBbWidth() / 2.0;
        return new AABB(x - halfWidth, y, z - halfWidth, x + halfWidth, y + getBbHeight(), z + halfWidth);
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public boolean hasPose(Pose pose) {
        return this.pose == pose;
    }

    public boolean isCrouching() {
        return hasPose(Pose.CROUCHING);
    }

    public boolean isInWater() {
        return wasTouchingWater;
    }

    public boolean isInLava() {
        return false;
    }

    public boolean isAlive() {
        return !removed;
    }

    @Nullable
    public Entity getVehicle() {
        return vehicle;
    }

    public boolean isPassenger() {
        return vehicle != null;
    }

    public boolean startRiding(Entity vehicle) {
        this.vehicle = vehicle;
        return true;
    }

    public void stopRiding() {
        this.vehicle = null;
    }

    /** Where the entity looks. */
    public Vec3 getLookAngle() {
        return calculateViewVector(getXRot(), getYRot());
    }

    /** Where the entity looks, its rotation interpolated by {@code partialTicks}. */
    public Vec3 getViewVector(float partialTicks) {
        return calculateViewVector(getViewXRot(partialTicks), getViewYRot(partialTicks));
    }

    public float getViewXRot(float partialTicks) {
        return partialTicks == 1.0F ? getXRot() : Mth.lerp(partialTicks, xRotO, getXRot());
    }

    public float getViewYRot(float partialTicks) {
        return partialTicks == 1.0F ? getYRot() : Mth.lerp(partialTicks, yRotO, getYRot());
    }

    /** The same maths as the game's. */
    protected final Vec3 calculateViewVector(float xRot, float yRot) {
        float pitch = xRot * (float) (Math.PI / 180.0);
        float yaw = -yRot * (float) (Math.PI / 180.0);
        float cosYaw = Mth.cos(yaw);
        float sinYaw = Mth.sin(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = Mth.sin(pitch);
        return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }
}
