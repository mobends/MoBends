package goblinbob.mobends.standard.client.renderer.entity;

import goblinbob.mobends.core.data.EntityComponent;

/**
 * LAB SHIM. The real trail notes the hand as the bent model is drawn and draws a streak from it; it
 * never feeds back into bone transforms, so the lab only counts what the driver asks of it.
 */
public class SwordTrail implements EntityComponent {
    public int resets;
    public int samples;

    public void reset() {
        resets++;
    }

    public void add(float velocityX, float velocityY, float velocityZ) {
        samples++;
    }

    @Override
    public void update(float ticksPerFrame) {
    }
}
