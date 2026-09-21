package goblinbob.mobends.standard.client.renderer.entity;

import goblinbob.mobends.core.util.IColorRead;
import goblinbob.mobends.standard.data.BipedEntityData;

import java.util.function.Supplier;

/**
 * LAB SHIM. The real trail is a GL effect derived from the arm pose; it never feeds back into
 * bone transforms, so the lab only counts the calls.
 */
public class SwordTrail
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

    public void add(BipedEntityData<?> entityData, float velocityX, float velocityY, float velocityZ)
    {
        samples++;
    }

    public void add(BipedEntityData<?> entityData)
    {
        samples++;
    }

    public void update(float ticksPerFrame)
    {
    }

    public void render()
    {
    }
}
