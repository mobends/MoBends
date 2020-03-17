package goblinbob.mobends.core.util;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.mutators.Mutator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;

public class BenderHelper
{

    /**
     * Returns true of the passed entity has been both registered and is currently enabled.
     * @param entity
     * @return
     */
    public static boolean isEntityAnimated(EntityLivingBase entity)
    {
        final EntityBender<?> bender = EntityBenderRegistry.instance.getForEntity(entity);
        return bender != null && bender.isAnimated();
    }

    public static <T extends EntityLivingBase> Mutator<?, ?, ?> getMutatorForRenderer(Class<T> entityClass, RenderLivingBase<T> renderer)
    {
        final EntityBender<?> bender = EntityBenderRegistry.instance.getForEntityClass(entityClass);
        return bender != null ? bender.getMutator(renderer) : null;
    }

}
