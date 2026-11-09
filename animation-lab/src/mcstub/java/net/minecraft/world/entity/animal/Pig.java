package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Pig extends LivingEntity {
    public Pig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    public Pig(Level level) {
        this(EntityType.PIG, level);
    }
}
