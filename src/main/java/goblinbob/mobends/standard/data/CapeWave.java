package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.data.EntityComponent;
import goblinbob.mobends.core.data.RemoteFlight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * {@code mobends:cape_wave}: the ripple running down a cape, as a phase the cape layer draws
 * ({@link goblinbob.mobends.standard.client.renderer.entity.BendsCapeRenderer}). It runs four times
 * as fast while a player flies and sprints.
 */
public class CapeWave implements EntityComponent {
    private final LivingEntity entity;
    private float phase;

    public CapeWave(LivingEntity entity) {
        this.entity = entity;
    }

    public float getPhase() {
        return phase;
    }

    @Override
    public void update(float ticksPerFrame) {
        boolean flyingFast = entity instanceof Player player && RemoteFlight.isFlying(player) && entity.isSprinting();
        phase += (flyingFast ? 4.0F : 1.0F) * ticksPerFrame;
        if (phase > 380.0F) {
            phase -= 380.0F;
        }
    }
}
