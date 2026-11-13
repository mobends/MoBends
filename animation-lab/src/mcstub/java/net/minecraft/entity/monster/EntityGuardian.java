package net.minecraft.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** For EntityIds' variants; the lab doesn't animate it. */
public class EntityGuardian extends EntityLivingBase { public EntityGuardian(World world) { super(world); } public boolean isElder() { return false; } }
