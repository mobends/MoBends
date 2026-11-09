package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Sheep extends LivingEntity {
    public Sheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    public Sheep(Level level) {
        this(EntityType.SHEEP, level);
    }
}
