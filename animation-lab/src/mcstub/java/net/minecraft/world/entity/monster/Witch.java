package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Witch extends LivingEntity {
    public Witch(EntityType<? extends Witch> type, Level level) {
        super(type, level);
    }

    public Witch(Level level) {
        this(EntityType.WITCH, level);
    }
}
