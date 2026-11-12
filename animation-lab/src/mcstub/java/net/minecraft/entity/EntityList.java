package net.minecraft.entity;


/** The registry ids of the lab's entities. */
public class EntityList
{
    /** 1.8.9's names of the entity classes the lab makes (the game's own, for these). */
    public static final java.util.Map<Class<? extends Entity>, String> classToStringMapping = new java.util.HashMap<>();

    static
    {
        classToStringMapping.put(net.minecraft.entity.monster.EntityZombie.class, "Zombie");
        classToStringMapping.put(net.minecraft.entity.monster.EntityPigZombie.class, "PigZombie");
        classToStringMapping.put(net.minecraft.entity.monster.EntitySkeleton.class, "Skeleton");
        classToStringMapping.put(net.minecraft.entity.monster.EntitySpider.class, "Spider");
        classToStringMapping.put(net.minecraft.entity.passive.EntitySquid.class, "Squid");
        classToStringMapping.put(net.minecraft.entity.passive.EntityWolf.class, "Wolf");
        classToStringMapping.put(net.minecraft.entity.passive.EntityChicken.class, "Chicken");
        classToStringMapping.put(net.minecraft.entity.monster.EntityIronGolem.class, "VillagerGolem");
        classToStringMapping.put(net.minecraft.entity.passive.EntityVillager.class, "Villager");
    }
}
