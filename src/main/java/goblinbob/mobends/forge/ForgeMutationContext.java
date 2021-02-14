package goblinbob.mobends.forge;

import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;

public class ForgeMutationContext
{
    private LivingEntity entity;
    private LivingRenderer<?, ?> renderer;
    private float partialTicks = 0;
    private float ticksPassed = 0;

    public ForgeMutationContext(LivingEntity entity, LivingRenderer<?, ?> renderer, float partialTicks, float ticksPassed)
    {
        this.entity = entity;
        this.renderer = renderer;
        this.partialTicks = partialTicks;
        this.ticksPassed = ticksPassed;
    }

    public LivingEntity getEntity()
    {
        return entity;
    }

    public LivingRenderer<?, ?> getRenderer()
    {
        return renderer;
    }

    public float getPartialTicks()
    {
        return partialTicks;
    }

    public float getTicksPassed()
    {
        return ticksPassed;
    }
}
