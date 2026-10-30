package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityVillager extends EntityLivingBase
{
    public EntityVillager(World world)
    {
        super(world);
        width = 0.6F;
        height = 1.95F;
    }
}
