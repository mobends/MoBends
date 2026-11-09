package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Skeleton extends LivingEntity {
    public Skeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }

    public Skeleton(Level level) {
        this(EntityType.SKELETON, level);
    }
}
