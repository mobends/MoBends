package goblinbob.mobends.core.types;

import goblinbob.mobends.core.bender.DefaultEntityBender;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MutatedRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;
import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Issue #144: AbyssalCraft's abyssal zombie (an EntityMob, not an EntityZombie) crashed with a
 * ClassCastException, given the zombie's model. Entities compare by id, and a new entity can take
 * the id of one that is gone: it must not get the model (or data) of the entity that had it.
 */
public class EntityIdReuseTest
{
    /** Stands in for AbyssalCraft's EntityAbyssalZombie: a mob that isn't a zombie. */
    public static class NotAZombie extends EntityMob
    {
        public NotAZombie(World world)
        {
            super(world);
        }
    }

    public static class StubRenderer extends MutatedRenderer<EntityZombie>
    {
    }

    @Test
    public void anEntityReusingAZombiesIdIsNotAnimatedAsAZombie() throws Exception
    {
        EntityBender<EntityZombie> zombieBender = new DefaultEntityBender<>("mobends", "zombie", "entity.Zombie.name",
                EntityZombie.class, entity -> null, () -> null, allocate(StubRenderer.class));
        EntityBenderRegistry.instance.registerBender(zombieBender);
        zombieBender.setAnimate(true);
        useOnlyBuiltInTypes();

        EntityZombie zombie = withId(allocate(EntityZombie.class), 42);
        NotAZombie notAZombie = withId(allocate(NotAZombie.class), 42);
        assertEquals("entities compare by id", zombie, notAZombie);

        EntityTypeRegistry.Selection zombieSelection = EntityBenderRegistry.instance.getSelection(zombie);
        assertNotNull(zombieSelection);
        assertSame(zombieBender, zombieSelection.bender);

        // The zombie is gone, and the new entity got its id.
        assertNull(EntityBenderRegistry.instance.getSelection(notAZombie));
        assertNull(EntityBenderRegistry.instance.getForEntity(notAZombie));
    }

    /** The built-in types of the registered benders, without the type files (which need the game). */
    @SuppressWarnings("unchecked")
    private static void useOnlyBuiltInTypes() throws Exception
    {
        EntityTypeRegistry registry = EntityTypeRegistry.INSTANCE;
        Field typesField = EntityTypeRegistry.class.getDeclaredField("types");
        typesField.setAccessible(true);
        Map<String, EntityType> types = (Map<String, EntityType>) typesField.get(registry);
        types.clear();
        for (EntityBender<?> bender : EntityBenderRegistry.instance.getDefaultBenders())
        {
            EntityType type = EntityType.builtIn(bender);
            types.put(type.getId(), type);
        }
        Field loadedField = EntityTypeRegistry.class.getDeclaredField("loaded");
        loadedField.setAccessible(true);
        loadedField.setBoolean(registry, true);
        EntityBenderRegistry.instance.clearCache();
    }

    private static <E extends Entity> E withId(E entity, int id)
    {
        entity.setEntityId(id);
        return entity;
    }

    /** An instance made without its constructor, which needs a running game. */
    private static <T> T allocate(Class<T> c) throws Exception
    {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object instance = ((Unsafe) unsafeField.get(null)).allocateInstance(c);
        return c.cast(instance);
    }
}
