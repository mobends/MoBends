package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Chicken extends LivingEntity {
    public Chicken(EntityType<? extends Chicken> type, Level level) {
        super(type, level);
    }

    public Chicken(Level level) {
        this(EntityType.CHICKEN, level);
    }

    public float flap, flapSpeed, oFlapSpeed, oFlap;
    public float flapping = 1.0F;
    public boolean isChickenJockey;
}
