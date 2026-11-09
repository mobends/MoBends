package net.minecraft.world.phys;

public abstract class HitResult {
    public enum Type {
        MISS,
        BLOCK,
        ENTITY
    }

    protected final Vec3 location;

    protected HitResult(Vec3 location) {
        this.location = location;
    }

    public Vec3 getLocation() {
        return location;
    }

    public abstract Type getType();
}
