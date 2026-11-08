package goblinbob.mobends.core.client.definition;

import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
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
        EntityModelDefinition.RendererSettings settings = definition.get().renderer;
        if (entity.isSneaking())
        {
            boolean flying = entity instanceof EntityPlayer && RemoteFlight.isFlying((EntityPlayer) entity);
            float offset = flying && settings.flyingSneakOffset != null ? settings.flyingSneakOffset : settings.sneakOffset;
            if (offset != 0)
            {
                GlStateManager.translate(0F, offset * scale, 0F);
            }
        }
        if (settings.undoVanillaRock && entity instanceof EntityIronGolem)
        {
            undoIronGolemRock(entity, partialTicks);
        }
    }

    /**
     * Turns the model back from the rock RenderIronGolem gives it while it walks (applyRotations:
     * 6.5 degrees about Z on a triangle wave of the limb swing), right before vanilla applies it.
     * Between here and there vanilla turns the model by 180 degrees about Y (this renderer's turn by
     * the body's yaw and vanilla's by 180 minus it), which turns a rotation about Z into one the
     * other way: so the same angle, here, undoes it.
     */
    private static void undoIronGolemRock(EntityLivingBase entity, float partialTicks)
    {
        if (entity.limbSwingAmount < 0.01D)
        {
            return;
        }
        float swing = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
        float wave = (Math.abs(swing % 13.0F - 6.5F) - 3.25F) / 3.25F;
        GlStateManager.rotate(6.5F * wave, 0.0F, 0.0F, 1.0F);
    }

}
