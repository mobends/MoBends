package goblinbob.mobends.core.bender;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.mutators.IMutatorFactory;
import net.minecraft.entity.EntityLivingBase;

public class DefaultEntityBender<T extends EntityLivingBase> extends EntityBender<T>
{

    private final IEntityDataFactory<T> entityDataFactory;
    private final IMutatorFactory<T> mutatorFactory;
    private final String[] alterableParts;

    public DefaultEntityBender(String modId, String key, String unlocalizedName, Class<T> entityClass,
                               IEntityDataFactory<T> entityDataFactory, IMutatorFactory<T> mutatorFactory,
                               MutatedRenderer<T> renderer, String... alterableParts)
    {
        super(modId, key, unlocalizedName, entityClass, renderer);

        this.entityDataFactory = entityDataFactory;
        this.mutatorFactory = mutatorFactory;
        this.alterableParts = alterableParts;
    }

    public String[] getAlterableParts()
    {
        return alterableParts;
    }

    @Override
    public IEntityDataFactory<T> getDataFactory()
    {
        return entityDataFactory;
    }

    @Override
    public IMutatorFactory<T> getMutatorFactory()
    {
        return mutatorFactory;
    }

}
