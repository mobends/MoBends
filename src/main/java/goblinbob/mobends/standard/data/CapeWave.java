package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.data.EntityComponent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * {@code mobends:cape_wave}: the ripple running down a cape, as a phase the cape layer draws. It
 * runs four times as fast while a player flies and sprints.
 */
public class CapeWave implements EntityComponent
{

    private final EntityLivingBase entity;
    private float phase;

    public CapeWave(EntityLivingBase entity)
    {
        this.entity = entity;
    }

    public float getPhase()
    {
        return phase;
    }

    @Override
    public void update(float ticksPerFrame)
    {
        boolean flyingFast = entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying && entity.isSprinting();
        phase += (flyingFast ? 4.0F : 1.0F) * ticksPerFrame;
        if (phase > 380.0F)
        {
            phase -= 380.0F;
        }
    }

}
