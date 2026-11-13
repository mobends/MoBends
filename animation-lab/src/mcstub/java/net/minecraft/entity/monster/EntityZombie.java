package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** A zombie, or in 1.8.9 a zombie villager too ({@link #isVillager}). */
public class EntityZombie extends EntityLivingBase
{
    private boolean villager;

    public EntityZombie(World world) { super(world); }

    public boolean isVillager() { return villager; }

    public void setVillager(boolean villager) { this.villager = villager; }
}
