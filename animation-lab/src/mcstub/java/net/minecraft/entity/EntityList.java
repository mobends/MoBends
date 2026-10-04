package net.minecraft.entity;

import net.minecraft.util.ResourceLocation;

/** The registry ids of the lab's entities. */
public class EntityList
{
    public static ResourceLocation getKey(Entity entity)
    {
        switch (entity.getClass().getSimpleName())
        {
            case "EntityZombie": return new ResourceLocation("minecraft", "zombie");
            case "EntityZombieVillager": return new ResourceLocation("minecraft", "zombie_villager");
            case "EntityPigZombie": return new ResourceLocation("minecraft", "zombie_pigman");
            case "EntitySkeleton": return new ResourceLocation("minecraft", "skeleton");
            case "EntitySpider": return new ResourceLocation("minecraft", "spider");
            case "EntitySquid": return new ResourceLocation("minecraft", "squid");
            case "EntityWolf": return new ResourceLocation("minecraft", "wolf");
            case "EntityChicken": return new ResourceLocation("minecraft", "chicken");
            case "EntityIronGolem": return new ResourceLocation("minecraft", "villager_golem");
            default: return null;
        }
    }
}
