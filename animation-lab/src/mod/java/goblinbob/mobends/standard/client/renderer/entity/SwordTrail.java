package goblinbob.mobends.standard.client.renderer.entity;

import goblinbob.mobends.core.data.EntityComponent;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.IColorRead;

import java.util.function.Supplier;

/**
 * LAB SHIM. The real trail is a GL effect derived from the arm pose; it never feeds back into
 * bone transforms, so the lab only counts the calls.
 */
public class SwordTrail implements EntityComponent
{
    private final Supplier<IColorRead> baseColor;
    public int resets;
    public int samples;

    public SwordTrail(Supplier<IColorRead> baseColor)
    {
        this.baseColor = baseColor;
    }

    public void reset()
    {
        resets++;
    }

    public void add(LivingEntityData<?> entityData, float velocityX, float velocityY, float velocityZ)
    {
        samples++;
    }

    public void add(LivingEntityData<?> entityData)
    {
        samples++;
    }

    @Override
    public void update(float ticksPerFrame)
    {
    }

    public void render()
    {
    }
}
