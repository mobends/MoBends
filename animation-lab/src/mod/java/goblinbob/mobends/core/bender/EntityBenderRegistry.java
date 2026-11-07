package goblinbob.mobends.core.bender;

import net.minecraft.entity.Entity;

/** LAB SHIM. Only what the entity database calls: there are no benders, so nothing is cached. */
public class EntityBenderRegistry
{
    public static final EntityBenderRegistry instance = new EntityBenderRegistry();

    public void clearCache(Entity entity)
    {
    }
}
