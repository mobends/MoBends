package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Cow extends LivingEntity {
    public Cow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    public Cow(Level level) {
        this(EntityType.COW, level);
    }
}
