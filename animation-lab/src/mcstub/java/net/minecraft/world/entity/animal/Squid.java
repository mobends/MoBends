package net.minecraft.world.entity.animal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class Squid extends LivingEntity {
    public Squid(EntityType<? extends Squid> type, Level level) {
        super(type, level);
    }

    public Squid(Level level) {
        this(EntityType.SQUID, level);
    }

    public float xBodyRot, xBodyRotO;
    public float zBodyRot, zBodyRotO;
    public float tentacleMovement, oldTentacleMovement;
    public float tentacleAngle, oldTentacleAngle;
}
