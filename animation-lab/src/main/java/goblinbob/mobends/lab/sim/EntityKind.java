package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.data.*;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.standard.animation.controller.*;
import goblinbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * The animated entity types of the mod, with factories for the stub entity and the mod's data
 * class (which owns the animation controller).
 */
public enum EntityKind
{
    PLAYER("mobends-minecraft:player", AbstractClientPlayer::new, e -> new PlayerData((AbstractClientPlayer) e), PlayerController::new),
    ZOMBIE("mobends-minecraft:zombie", EntityZombie::new, e -> new ZombieData((EntityZombie) e), ZombieController::new),
    ZOMBIE_VILLAGER("mobends-minecraft:zombie_villager", EntityZombieVillager::new, e -> new ZombieVillagerData((EntityZombieVillager) e), ZombieVillagerController::new),
    SKELETON("mobends-minecraft:skeleton", EntitySkeleton::new, e -> new SkeletonData((EntitySkeleton) e), SkeletonController::new),
    PIG_ZOMBIE("mobends-minecraft:zombie_pigman", EntityPigZombie::new, e -> new PigZombieData((EntityPigZombie) e), PigZombieController::new),
    SPIDER("mobends-minecraft:spider", EntitySpider::new, e -> new SpiderData((EntitySpider) e), SpiderController::new),
    SQUID("mobends-minecraft:squid", EntitySquid::new, e -> new SquidData((EntitySquid) e), SquidController::new),
    WOLF("mobends-minecraft:wolf", EntityWolf::new, e -> new WolfData((EntityWolf) e), WolfController::new);

    /** The key the mod registers the entity bender under (bends packs target it). */
    public final String benderKey;
    private final Function<World, EntityLivingBase> entityFactory;
    private final Function<EntityLivingBase, LivingEntityData<?>> dataFactory;
    private final java.util.function.Supplier<IAnimationController<?>> legacyController;

    EntityKind(String benderKey, Function<World, EntityLivingBase> entityFactory, Function<EntityLivingBase, LivingEntityData<?>> dataFactory,
               java.util.function.Supplier<IAnimationController<?>> legacyController)
    {
        this.benderKey = benderKey;
        this.entityFactory = entityFactory;
        this.dataFactory = dataFactory;
        this.legacyController = legacyController;
    }

    /** The procedural controller this entity animated with before KUMO: the reference the goldens come from. */
    public IAnimationController<?> createLegacyController()
    {
        return legacyController.get();
    }

    public EntityLivingBase createEntity(World world)
    {
        return entityFactory.apply(world);
    }

    public LivingEntityData<?> createData(EntityLivingBase entity, long seed)
    {
        LivingEntityData<?> data = dataFactory.apply(entity);
        Determinism.seedRandoms(data, seed);
        return data;
    }

    public String id()
    {
        return name().toLowerCase();
    }
}
