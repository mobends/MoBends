package goblinbob.mobends.core.supporters;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An accessory of the catalogue ({@code /api/accessory/details}): its name, the colour it's shown
 * in until the player picks one, and its parts. An accessory with no parts (the coloured sword
 * trail) is drawn by whatever uses it.
 *
 * @param defaultColor {@code 0xRRGGBB}, or null if the API doesn't say (see {@link #defaultColorOrWhite})
 */
public record AccessoryDetails(String displayName, @Nullable Integer defaultColor, List<AccessoryPart> parts) {
    public AccessoryDetails {
        parts = parts == null ? List.of() : List.copyOf(parts.stream().filter(p -> p != null).toList());
    }

    /** The colour it's shown in until the player picks one, {@code 0xRRGGBB}; white if the API doesn't say. */
    public int defaultColorOrWhite() {
        return defaultColor != null ? defaultColor & 0xFFFFFF : 0xFFFFFF;
    }
}
