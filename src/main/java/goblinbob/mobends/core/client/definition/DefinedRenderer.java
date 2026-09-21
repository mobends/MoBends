package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.entity.EntityLivingBase;

/** The mutated renderer of a defined mob: the default one with the definition's child scale. */
public class DefinedRenderer<E extends EntityLivingBase> extends MutatedRenderer<E>
{

    private final EntityModelDefinition definition;

    public DefinedRenderer(EntityModelDefinition definition)
    {
        this.definition = definition;
    }

    @Override
    protected float getChildScale()
    {
        return definition.childScale;
    }

}
