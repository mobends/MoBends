package net.minecraft.world.entity;

import net.minecraft.util.Mth;

/** How far a living entity's legs have swung (the limb swing), and how fast they swing (its amount), as the game has it. */
public class WalkAnimationState {
    private float speedOld;
    private float speed;
    private float position;

    /** Once a tick: the speed moves toward {@code movementSpeed} by {@code multiplier}, and the legs swing on by it. */
    public void update(float movementSpeed, float multiplier) {
        this.speedOld = this.speed;
        this.speed += (movementSpeed - this.speed) * multiplier;
        this.position += this.speed;
    }

    public float speed(float partialTick) {
        return Mth.lerp(partialTick, speedOld, speed);
    }

    public float position(float partialTick) {
        return position - speed * (1.0F - partialTick);
    }
}
