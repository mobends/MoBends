package net.minecraft.world.entity.vehicle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Something to ride that isn't alive. */
public class Boat extends Entity {
    public Boat(EntityType<? extends Boat> type, Level level) {
        super(type, level);
    }

    public Boat(Level level) {
        this(EntityType.BOAT, level);
    }
}
