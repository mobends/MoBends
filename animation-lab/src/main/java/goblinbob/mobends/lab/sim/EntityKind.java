package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.standard.data.*;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
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
    PLAYER("mobends-minecraft:player", AbstractClientPlayer::new, e -> new PlayerData((AbstractClientPlayer) e)),
    ZOMBIE("mobends-minecraft:zombie", EntityZombie::new, e -> new ZombieData((EntityZombie) e)),
    SKELETON("mobends-minecraft:skeleton", EntitySkeleton::new, e -> new SkeletonData((EntitySkeleton) e)),
    PIG_ZOMBIE("mobends-minecraft:zombie_pigman", EntityPigZombie::new, e -> new PigZombieData((EntityPigZombie) e)),
    SPIDER("mobends-minecraft:spider", EntitySpider::new, e -> new SpiderData((EntitySpider) e)),
    SQUID("mobends-minecraft:squid", EntitySquid::new, e -> new SquidData((EntitySquid) e)),
    WOLF("mobends-minecraft:wolf", EntityWolf::new, e -> new WolfData((EntityWolf) e));

    /** The key the mod registers the entity bender under (bends packs target it). */
    public final String benderKey;
    private final Function<World, EntityLivingBase> entityFactory;
    private final Function<EntityLivingBase, LivingEntityData<?>> dataFactory;

    EntityKind(String benderKey, Function<World, EntityLivingBase> entityFactory, Function<EntityLivingBase, LivingEntityData<?>> dataFactory)
    {
        this.benderKey = benderKey;
        this.entityFactory = entityFactory;
        this.dataFactory = dataFactory;
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
