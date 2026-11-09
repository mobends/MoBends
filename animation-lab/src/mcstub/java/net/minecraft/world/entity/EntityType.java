package net.minecraft.world.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;

/**
 * The lab's entity types, registered under the game's ids. Their sizes are the ones the lab's
 * entities have always had (and the goldens were recorded with): 0.6 by 1.8 blocks, but for the
 * iron golem's and the villagers' (the game's).
 */
public final class EntityType<T extends Entity> {
    public static final EntityType<Player> PLAYER = register("player", 0.6F, 1.8F);
    public static final EntityType<Zombie> ZOMBIE = register("zombie", 0.6F, 1.8F);
    public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = register("zombie_villager", 0.6F, 1.8F);
    public static final EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = register("zombified_piglin", 0.6F, 1.8F);
    public static final EntityType<Skeleton> SKELETON = register("skeleton", 0.6F, 1.8F);
    public static final EntityType<Spider> SPIDER = register("spider", 0.6F, 1.8F);
    public static final EntityType<Squid> SQUID = register("squid", 0.6F, 1.8F);
    public static final EntityType<Wolf> WOLF = register("wolf", 0.6F, 1.8F);
    public static final EntityType<IronGolem> IRON_GOLEM = register("iron_golem", 1.4F, 2.7F);
    public static final EntityType<Villager> VILLAGER = register("villager", 0.6F, 1.95F);
    public static final EntityType<Witch> WITCH = register("witch", 0.6F, 1.95F);
    public static final EntityType<Creeper> CREEPER = register("creeper", 0.6F, 1.8F);
    public static final EntityType<Chicken> CHICKEN = register("chicken", 0.6F, 1.8F);
    public static final EntityType<Cow> COW = register("cow", 0.6F, 1.8F);
    public static final EntityType<MushroomCow> MOOSHROOM = register("mooshroom", 0.6F, 1.8F);
    public static final EntityType<Pig> PIG = register("pig", 0.6F, 1.8F);
    public static final EntityType<Sheep> SHEEP = register("sheep", 0.6F, 1.8F);
    public static final EntityType<PolarBear> POLAR_BEAR = register("polar_bear", 0.6F, 1.8F);
    public static final EntityType<Boat> BOAT = register("boat", 1.375F, 0.5625F);

    private final float width, height;

    private EntityType(float width, float height) {
        this.width = width;
        this.height = height;
    }

    private static <T extends Entity> EntityType<T> register(String id, float width, float height) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, "minecraft:" + id, new EntityType<>(width, height));
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "entity." + BuiltInRegistries.ENTITY_TYPE.getKey(this);
    }
}
