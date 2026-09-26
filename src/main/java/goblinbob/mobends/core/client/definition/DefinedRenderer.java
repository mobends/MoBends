package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Supplier;

/** The mutated renderer of a defined mob: the default one with the definition's child scale. */
public class DefinedRenderer<E extends EntityLivingBase> extends MutatedRenderer<E>
{

    private final Supplier<EntityModelDefinition> definition;

    public DefinedRenderer(Supplier<EntityModelDefinition> definition)
    {
        this.definition = definition;
    }

    @Override
    protected float getChildScale()
    {
        return definition.get().childScale;
    }

}
