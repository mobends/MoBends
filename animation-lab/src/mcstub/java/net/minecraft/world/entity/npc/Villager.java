package net.minecraft.world.entity.npc;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Villager extends LivingEntity {
    public Villager(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    public Villager(Level level) {
        this(EntityType.VILLAGER, level);
    }
}
