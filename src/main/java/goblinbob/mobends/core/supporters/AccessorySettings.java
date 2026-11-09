package goblinbob.mobends.core.supporters;

import javax.annotation.Nullable;

/**
 * A player's settings of one accessory ({@code /api/accessory}): whether they have it, whether
 * they've hidden it, and the colour they picked for it.
 *
 * @param color {@code 0xRRGGBB}, or null for no colour picked (see {@link #colorOr})
 */
public record AccessorySettings(boolean unlocked, boolean hidden, @Nullable Integer color) {
    /** What a player who doesn't have an accessory has of it. */
    public static final AccessorySettings DEFAULT = new AccessorySettings(false, false, null);

    /** Whether the accessory is worn: unlocked and not hidden. */
    public boolean isShown() {
        return unlocked && !hidden;
    }

    /** The colour picked, {@code 0xRRGGBB}, or {@code fallback} (the accessory's default colour) if none was. */
    public int colorOr(int fallback) {
        return color != null ? color & 0xFFFFFF : fallback;
    }
}
