package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.Supplier;

/** The mutated renderer of a defined mob: the default one with the definition's child scale and sneak offset. */
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

    @Override
    protected void transformLocally(E entity, EntityData<?> data, float partialTicks)
    {
        if (!entity.isSneaking())
        {
            return;
        }
        EntityModelDefinition.RendererSettings settings = definition.get().renderer;
        boolean flying = entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying;
        float offset = flying && settings.flyingSneakOffset != null ? settings.flyingSneakOffset : settings.sneakOffset;
        if (offset != 0)
        {
            GlStateManager.translate(0F, offset * scale, 0F);
        }
    }

}
