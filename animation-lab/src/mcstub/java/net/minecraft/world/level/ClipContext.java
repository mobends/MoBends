package net.minecraft.world.level;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/** A ray to cast through the level ({@link Level#clip}): from where to where, and what it stops at. */
public class ClipContext {
    public enum Block {
        COLLIDER,
        OUTLINE,
        VISUAL,
        FALLDAMAGE_RESETTING
    }

    public enum Fluid {
        NONE,
        SOURCE_ONLY,
        ANY,
        WATER
    }

    private final Vec3 from;
    private final Vec3 to;

    public ClipContext(Vec3 from, Vec3 to, Block block, Fluid fluid, CollisionContext collisionContext) {
        this.from = from;
        this.to = to;
    }

    public Vec3 getFrom() {
        return from;
    }

    public Vec3 getTo() {
        return to;
    }
}
