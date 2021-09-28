package goblinbob.mobends.standard.client.renderer.entity;

import net.minecraft.entity.projectile.EntityArrow;

import java.util.HashMap;

public class ArrowTrailManager
{
    private static HashMap<EntityArrow, ArrowTrail> trailMap = new HashMap<>();
    public static long time, lastTime;

    static
    {
        time = System.nanoTime() / 1000;
        lastTime = System.nanoTime() / 1000;
    }

    public static ArrowTrail getOrMake(EntityArrow arrow)
    {
        ArrowTrail trail;
        if (!trailMap.containsKey(arrow))
        {
            trail = new ArrowTrail(arrow);
            trailMap.put(arrow, trail);
        }
        else
        {
            trail = trailMap.get(arrow);
        }

        return trail;
    }

    public static void renderTrail(EntityArrow entity, double x, double y, double z, float partialTicks)
    {
        getOrMake(entity).render(x, y, z, partialTicks);
    }

    public static void cleanup()
    {
        trailMap.entrySet().removeIf(e -> e.getValue().shouldBeRemoved());
    }

    public static void onRenderTick()
    {
        for (final ArrowTrail trail : trailMap.values())
        {
            trail.onRenderTick();
        }

        cleanup();
    }
}
