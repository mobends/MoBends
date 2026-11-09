package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Zombie extends LivingEntity {
    public Zombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public Zombie(Level level) {
        this(EntityType.ZOMBIE, level);
    }
}
