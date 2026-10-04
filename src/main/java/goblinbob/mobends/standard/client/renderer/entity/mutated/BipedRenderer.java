package goblinbob.mobends.standard.client.renderer.entity.mutated;

import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.data.EntityData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

public class BipedRenderer<T extends EntityLivingBase> extends MutatedRenderer<T>
{

    @Override
    protected void transformLocally(T entity, EntityData<?> data, float partialTicks)
    {
        if (entity.isSneaking())
        {
            GlStateManager.translate(0F, 5F * scale, 0F);
        }
    }

}
