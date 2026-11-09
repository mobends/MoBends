package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class IronGolem extends LivingEntity {
    public IronGolem(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    public IronGolem(Level level) {
        this(EntityType.IRON_GOLEM, level);
    }

    private int attackAnimationTick;

    public int getAttackAnimationTick() {
        return attackAnimationTick;
    }
}
