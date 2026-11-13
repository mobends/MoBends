package net.minecraft.entity.passive;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/** For EntityIds' variants; the lab doesn't animate it. */
public class EntityHorse extends EntityLivingBase { public EntityHorse(World world) { super(world); } public int getHorseType() { return 0; } }
