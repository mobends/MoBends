package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PolarBear extends LivingEntity {
    public PolarBear(EntityType<? extends PolarBear> type, Level level) {
        super(type, level);
    }

    public PolarBear(Level level) {
        this(EntityType.POLAR_BEAR, level);
    }
}
