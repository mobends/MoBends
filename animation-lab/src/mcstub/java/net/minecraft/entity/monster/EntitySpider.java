package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntitySpider extends EntityLivingBase
{
    public boolean besideClimbableBlock;
    public EntitySpider(World world) { super(world); }
    public boolean isBesideClimbableBlock() { return besideClimbableBlock; }
}
