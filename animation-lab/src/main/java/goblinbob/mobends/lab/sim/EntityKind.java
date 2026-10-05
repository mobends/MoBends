package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.function.Function;

/**
 * The animated entity types of the mod, with factories for the stub entity and the data its model
 * definition makes.
 */
public enum EntityKind
{
    PLAYER("mobends:player", "player", AbstractClientPlayer::new),
    ZOMBIE("mobends:zombie", "zombie", EntityZombie::new),
    ZOMBIE_VILLAGER("mobends:zombie_villager", "zombie_villager", EntityZombieVillager::new),
    SKELETON("mobends:skeleton", "skeleton", EntitySkeleton::new),
    PIG_ZOMBIE("mobends:zombie_pigman", "pig_zombie", EntityPigZombie::new),
    SPIDER("mobends:spider", "spider", EntitySpider::new),
    SQUID("mobends:squid", "squid", EntitySquid::new),
    WOLF("mobends:wolf", "wolf", EntityWolf::new),
    IRON_GOLEM("mobends:villager_golem", "iron_golem", EntityIronGolem::new);

    /** The key the mod registers the entity bender under. */
    public final String benderKey;
    /** The mob's model definition, in {@code bends/models/}. */
    public final String definition;
    private final Function<World, EntityLivingBase> entityFactory;

    EntityKind(String benderKey, String definition, Function<World, EntityLivingBase> entityFactory)
    {
        this.benderKey = benderKey;
        this.definition = definition;
        this.entityFactory = entityFactory;
    }

    public EntityLivingBase createEntity(World world)
    {
        return entityFactory.apply(world);
    }

    public EntityModelDefinition loadDefinition()
    {
        LabBootstrap.ensure();
        try
        {
            return ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/" + definition + ".json"));
        }
        catch (IOException | MalformedKumoTemplateException e)
        {
            throw new IllegalStateException("cannot load the model definition " + definition, e);
        }
    }

    public LivingEntityData<?> createData(EntityLivingBase entity, long seed)
    {
        LivingEntityData<?> data = DefinedEntityData.create(loadDefinition(), entity);
        Determinism.seedRandoms(data, seed);
        return data;
    }

    public String id()
    {
        return name().toLowerCase();
    }
}
