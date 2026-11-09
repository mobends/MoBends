package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ZombifiedPiglin extends Zombie {
    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> type, Level level) {
        super(type, level);
    }

    public ZombifiedPiglin(Level level) {
        this(EntityType.ZOMBIFIED_PIGLIN, level);
    }
}
