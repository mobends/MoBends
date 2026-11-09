package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class ZombieVillager extends Zombie {
    public ZombieVillager(EntityType<? extends ZombieVillager> type, Level level) {
        super(type, level);
    }

    public ZombieVillager(Level level) {
        this(EntityType.ZOMBIE_VILLAGER, level);
    }
}
