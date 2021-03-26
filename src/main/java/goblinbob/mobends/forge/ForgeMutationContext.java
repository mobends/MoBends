package goblinbob.mobends.forge;

import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.driver.IDriverBooleanFunction;
import goblinbob.mobends.core.kumo.driver.IDriverFunctionProvider;
import goblinbob.mobends.core.kumo.driver.IDriverNumberFunction;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;

import java.io.IOException;

public class ForgeMutationContext implements IKumoInstancingContext<EntityData>
{
    private final IDriverFunctionProvider<EntityData> driverFunctionProvider;
    private LivingEntity entity;
    private LivingRenderer<?, ?> renderer;
    private float partialTicks = 0;
    private float ticksPassed = 0;

    public ForgeMutationContext(IDriverFunctionProvider<EntityData> driverFunctionProvider, LivingEntity entity, LivingRenderer<?, ?> renderer, float partialTicks, float ticksPassed)
    {
        this.driverFunctionProvider = driverFunctionProvider;
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

    @Override
    public KeyframeAnimation getAnimation(String key)
    {
        try
        {
            return AnimationLoader.loadFromPath(key);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IDriverNumberFunction<EntityData> getDriverNumberFunction(String name)
    {
        return driverFunctionProvider.getDriverNumberFunction(name);
    }

    @Override
    public IDriverBooleanFunction<EntityData> getDriverBooleanFunction(String name)
    {
        return driverFunctionProvider.getDriverBooleanFunction(name);
    }
}
