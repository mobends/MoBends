package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** As in the game, not a {@link Skeleton}: both are skeletons of their own. */
public class WitherSkeleton extends LivingEntity {
    public WitherSkeleton(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
    }

    public WitherSkeleton(Level level) {
        this(EntityType.WITHER_SKELETON, level);
    }
}
