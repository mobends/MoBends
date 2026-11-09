package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

import java.io.IOException;
import java.util.function.Function;

/**
 * The animated entity types of the mod, with factories for the stub entity and the data its model
 * definition makes.
 */
public enum EntityKind
{
    PLAYER("mobends:player", "player", LocalPlayer::new),
    ZOMBIE("mobends:zombie", "zombie", Zombie::new),
    ZOMBIE_VILLAGER("mobends:zombie_villager", "zombie_villager", ZombieVillager::new),
    SKELETON("mobends:skeleton", "skeleton", Skeleton::new),
    PIG_ZOMBIE("mobends:zombie_pigman", "pig_zombie", ZombifiedPiglin::new),
    SPIDER("mobends:spider", "spider", Spider::new),
    SQUID("mobends:squid", "squid", Squid::new),
    WOLF("mobends:wolf", "wolf", Wolf::new),
    IRON_GOLEM("mobends:villager_golem", "iron_golem", IronGolem::new);

    /** The key the mod registers the entity bender under. */
    public final String benderKey;
    /** The mob's model definition, in {@code bends/models/}. */
    public final String definition;
    private final Function<ClientLevel, LivingEntity> entityFactory;

    EntityKind(String benderKey, String definition, Function<ClientLevel, LivingEntity> entityFactory)
    {
        this.benderKey = benderKey;
        this.definition = definition;
        this.entityFactory = entityFactory;
    }

    /** A new entity of this kind, added to {@code level}. */
    public LivingEntity createEntity(ClientLevel level)
    {
        LivingEntity entity = entityFactory.apply(level);
        level.addEntity(entity);
        return entity;
    }

    public EntityModelDefinition loadDefinition()
    {
        LabBootstrap.ensure();
        try
        {
            return ModelDefinitions.INSTANCE.load(ResourceLocation.fromNamespaceAndPath("mobends", "bends/models/" + definition + ".json"));
        }
        catch (IOException | MalformedKumoTemplateException e)
        {
            throw new IllegalStateException("cannot load the model definition " + definition, e);
        }
    }

    public LivingEntityData<?> createData(LivingEntity entity, long seed)
    {
        LivingEntityData<?> data = newData(loadDefinition(), entity);
        Determinism.seedRandoms(data, seed);
        return data;
    }

    /** The data {@code definition} makes for {@code entity}, initialized, as the mod's entity database makes it. */
    public static <E extends LivingEntity> DefinedEntityData<E> newData(EntityModelDefinition definition, E entity)
    {
        DefinedEntityData<E> data = new DefinedEntityData<>(definition, entity);
        data.initialize();
        return data;
    }

    public String id()
    {
        return name().toLowerCase();
    }
}
