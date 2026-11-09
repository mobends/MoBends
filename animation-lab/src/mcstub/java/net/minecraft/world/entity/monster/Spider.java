package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Spider extends LivingEntity {
    public Spider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    public Spider(Level level) {
        this(EntityType.SPIDER, level);
    }

    private boolean climbing;

    /** Whether it climbs a wall (the game sets it as the spider bumps into one). */
    public boolean isClimbing() {
        return climbing;
    }

    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    @Override
    public boolean onClimbable() {
        return isClimbing();
    }
}
