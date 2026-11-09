package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Creeper extends LivingEntity {
    public Creeper(EntityType<? extends Creeper> type, Level level) {
        super(type, level);
    }

    public Creeper(Level level) {
        this(EntityType.CREEPER, level);
    }
}
