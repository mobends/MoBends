package goblinbob.mobends.core.client.preview;

import net.minecraft.world.entity.LivingEntity;

/** LAB SHIM. The Mo' Bends menu's previews, which the entity data tells apart. The lab has none. */
public final class PreviewWorld {
    private PreviewWorld() {
    }

    public static boolean isPreview(LivingEntity entity) {
        return false;
    }
}
