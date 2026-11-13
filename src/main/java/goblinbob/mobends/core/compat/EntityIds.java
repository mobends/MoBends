package goblinbob.mobends.core.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Entity type ids as 1.11 and later name them ({@code minecraft:zombie_villager}), which type files, settings and
 * packs use. 1.8.9 names entity classes differently ({@code PigZombie}), and some types of later versions are a
 * variant of another's class there (a zombie villager is a zombie that {@link EntityZombie#isVillager is a villager}),
 * so an entity's id depends on the entity, not only its class.
 */
public final class EntityIds
{

    /** Players have no entity registry id; this one stands for them. */
    public static final ResourceLocation PLAYER = new ResourceLocation("minecraft", "player");

    /** 1.8.9's names of the vanilla entities (EntityList) to their ids in later versions. */
    private static final Map<String, String> VANILLA = new HashMap<>();

    static
    {
        String[] pairs = {
                "Item", "item", "XPOrb", "xp_orb", "LeashKnot", "leash_knot", "Painting", "painting", "Arrow", "arrow",
                "Snowball", "snowball", "Fireball", "fireball", "SmallFireball", "small_fireball",
                "ThrownEnderpearl", "ender_pearl", "EyeOfEnderSignal", "eye_of_ender_signal", "ThrownPotion", "potion",
                "ThrownExpBottle", "xp_bottle", "ItemFrame", "item_frame", "WitherSkull", "wither_skull",
                "PrimedTnt", "tnt", "FallingSand", "falling_block", "FireworksRocketEntity", "fireworks_rocket",
                "ArmorStand", "armor_stand", "Boat", "boat", "MinecartRideable", "minecart",
                "MinecartChest", "chest_minecart", "MinecartFurnace", "furnace_minecart", "MinecartTNT", "tnt_minecart",
                "MinecartHopper", "hopper_minecart", "MinecartSpawner", "spawner_minecart",
                "MinecartCommandBlock", "commandblock_minecart", "Creeper", "creeper", "Skeleton", "skeleton",
                "Spider", "spider", "Giant", "giant", "Zombie", "zombie", "Slime", "slime", "Ghast", "ghast",
                "PigZombie", "zombie_pigman", "Enderman", "enderman", "CaveSpider", "cave_spider",
                "Silverfish", "silverfish", "Blaze", "blaze", "LavaSlime", "magma_cube", "EnderDragon", "ender_dragon",
                "WitherBoss", "wither", "Bat", "bat", "Witch", "witch", "Endermite", "endermite", "Guardian", "guardian",
                "Pig", "pig", "Sheep", "sheep", "Cow", "cow", "Chicken", "chicken", "Squid", "squid", "Wolf", "wolf",
                "MushroomCow", "mooshroom", "SnowMan", "snowman", "Ozelot", "ocelot", "VillagerGolem", "villager_golem",
                "EntityHorse", "horse", "Rabbit", "rabbit", "Villager", "villager", "EnderCrystal", "ender_crystal",
        };
        for (int i = 0; i < pairs.length; i += 2)
        {
            VANILLA.put(pairs[i], pairs[i + 1]);
        }
    }

    private EntityIds()
    {
    }

    /** The id of the entity's type, its variant included, or null for an unregistered entity. */
    @Nullable
    public static ResourceLocation of(Entity entity)
    {
        if (entity instanceof EntityPlayer) return PLAYER;
        if (entity instanceof EntityZombie && entity.getClass() == EntityZombie.class && ((EntityZombie) entity).isVillager())
            return new ResourceLocation("minecraft", "zombie_villager");
        if (entity instanceof EntitySkeleton && entity.getClass() == EntitySkeleton.class && ((EntitySkeleton) entity).getSkeletonType() == 1)
            return new ResourceLocation("minecraft", "wither_skeleton");
        if (entity instanceof EntityGuardian && entity.getClass() == EntityGuardian.class && ((EntityGuardian) entity).isElder())
            return new ResourceLocation("minecraft", "elder_guardian");
        if (entity instanceof EntityHorse && entity.getClass() == EntityHorse.class)
        {
            switch (((EntityHorse) entity).getHorseType())
            {
                case 1: return new ResourceLocation("minecraft", "donkey");
                case 2: return new ResourceLocation("minecraft", "mule");
                case 3: return new ResourceLocation("minecraft", "zombie_horse");
                case 4: return new ResourceLocation("minecraft", "skeleton_horse");
                default: break;
            }
        }
        return ofClass(entity.getClass());
    }

    /** The id of the entity class's type (its first variant where 1.8.9 has several in one class), or null. */
    @Nullable
    public static ResourceLocation ofClass(Class<? extends Entity> entityClass)
    {
        String name = EntityList.classToStringMapping.get(entityClass);
        return name == null ? null : fromName(name);
    }

    /** The id of a 1.8.9 EntityList name ({@code PigZombie}, or {@code modid.Name} for a mod's entity). */
    public static ResourceLocation fromName(String name)
    {
        String vanilla = VANILLA.get(name);
        if (vanilla != null) return new ResourceLocation("minecraft", vanilla);
        int dot = name.indexOf('.');
        if (dot > 0) return new ResourceLocation(name.substring(0, dot).toLowerCase(Locale.ROOT), name.substring(dot + 1).toLowerCase(Locale.ROOT));
        return new ResourceLocation("minecraft", name.toLowerCase(Locale.ROOT));
    }

    /** The translation key of the entity class's name ({@code entity.PigZombie.name}), or null. */
    @Nullable
    public static String translationKey(Class<? extends Entity> entityClass)
    {
        String name = EntityList.classToStringMapping.get(entityClass);
        return name == null ? null : "entity." + name + ".name";
    }
}
