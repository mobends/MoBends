package goblinbob.mobends.core.supporters;

import goblinbob.mobends.MoBends;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.util.ConnectionHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * What supporters get (1.12's {@code SupporterContent}): the catalogue of accessories, fetched from
 * the API as the game starts, and each player's settings of them, fetched as the player is first
 * drawn ({@link ConnectionManager#fetchSettingsForPlayer}). The accessories are drawn on the bent
 * player ({@code PlayerAccessoriesLayer}); the {@code sword_trail} accessory colours the sword trail.
 * <p>
 * Render thread only: what's fetched in the background is handed over on the render thread.
 */
public final class SupporterContent {
    /** The accessory that colours the sword trail; it has no parts. */
    public static final String SWORD_TRAIL = "sword_trail";
    private static final int DEFAULT_TRAIL_COLOR = 0xFFFFFF;

    /** The catalogue, by key, in the API's order; replaced whole, so readers can keep iterating the previous one. */
    private static Map<String, AccessoryDetails> accessories = Collections.emptyMap();
    private static int accessoryDetailsRevision;
    private static boolean fetchingAccessories;
    /** Each player's settings, by name. */
    private static final Map<String, Map<String, AccessorySettings>> settingsPerPlayer = new HashMap<>();
    /** What the previews of the Supporters window wear, whatever their player has unlocked. */
    private static final Map<LivingEntity, Map<String, AccessorySettings>> previewAccessories = new WeakHashMap<>();

    private SupporterContent() {
    }

    /**
     * Downloads the catalogue in the background, keeping the last good one if that fails; it
     * changes on the render thread, which {@link #getAccessoryDetailsRevision} tells. Does nothing
     * while a download is under way.
     */
    public static void fetchAccessoryDetails() {
        if (fetchingAccessories) {
            return;
        }
        fetchingAccessories = true;
        ConnectionHelper.runInBackground("accessory details", () -> {
            Map<String, AccessoryDetails> downloaded = downloadAccessoryDetails();
            ConnectionHelper.onRenderThread(() -> {
                if (downloaded != null) {
                    accessories = downloaded;
                    ++accessoryDetailsRevision;
                }
                fetchingAccessories = false;
            });
        });
    }

    @Nullable
    private static Map<String, AccessoryDetails> downloadAccessoryDetails() {
        try {
            DetailsResponse response = ConnectionHelper.get("/api/accessory/details", Map.of(), DetailsResponse.class);
            if (response == null || response.details == null) {
                MoBends.LOGGER.warn("The Mo' Bends API's accessory details have no details");
                return null;
            }
            Map<String, AccessoryDetails> details = new LinkedHashMap<>(response.details);
            details.values().removeIf(Objects::isNull);
            MoBends.LOGGER.info("Fetched the supporter accessories: {}", details.keySet());
            return Collections.unmodifiableMap(details);
        } catch (IOException e) {
            if (ConnectionHelper.isOffline(e)) {
                MoBends.LOGGER.info("Couldn't reach the Mo' Bends API for the supporter accessories ({})", e.toString());
            } else {
                MoBends.LOGGER.warn("Couldn't fetch the supporter accessories", e);
            }
            return null;
        }
    }

    /** Fetches the catalogue again, and forgets every player's settings, which are fetched again as they're drawn. */
    public static void refresh() {
        fetchAccessoryDetails();
        settingsPerPlayer.clear();
    }

    /** The catalogue, by accessory key, in the API's order; empty until it's been fetched. */
    public static Map<String, AccessoryDetails> getAccessories() {
        return accessories;
    }

    /** Goes up each time the catalogue changes, for a screen that lists it to notice. */
    public static int getAccessoryDetailsRevision() {
        return accessoryDetailsRevision;
    }

    /** Whether the catalogue is being downloaded. */
    public static boolean isFetchingAccessories() {
        return fetchingAccessories;
    }

    /** Called with the settings a player's fetch brought. */
    public static void registerPlayerAccessorySettings(String playerName, Map<String, AccessorySettings> settings) {
        Map<String, AccessorySettings> copy = new HashMap<>(settings);
        copy.values().removeIf(Objects::isNull);
        settingsPerPlayer.put(playerName, Collections.unmodifiableMap(copy));
    }

    /** Drops the player's settings, so they are fetched anew (after linking an account, or becoming a supporter). */
    public static void forgetPlayer(String playerName) {
        settingsPerPlayer.remove(playerName);
    }

    /**
     * Dresses a preview entity (the Supporters window's) in {@code settings} instead of what its
     * player has; null undresses it. Held weakly: a preview entity that's gone is forgotten.
     */
    public static void setPreviewAccessories(LivingEntity entity, @Nullable Map<String, AccessorySettings> settings) {
        if (settings == null) {
            previewAccessories.remove(entity);
        } else {
            previewAccessories.put(entity, settings);
        }
    }

    /**
     * What {@code entity} wears, by accessory key: a preview's settings, else its player's, which
     * are fetched in the background the first time they're asked for (an empty map until they
     * arrive). Only players have settings; any other entity wears nothing.
     */
    public static Map<String, AccessorySettings> getAccessorySettingsMapFor(LivingEntity entity) {
        Map<String, AccessorySettings> preview = previewAccessories.get(entity);
        if (preview != null) {
            return preview;
        }
        if (!(entity instanceof Player player)) {
            return Collections.emptyMap();
        }
        String name = player.getGameProfile().getName();
        Map<String, AccessorySettings> settings = settingsPerPlayer.get(name);
        if (settings == null) {
            ConnectionManager.fetchSettingsForPlayer(name, player.getGameProfile().getId());
            return Collections.emptyMap();
        }
        return settings;
    }

    /** {@code entity}'s settings of accessory {@code key}, {@link AccessorySettings#DEFAULT} if it has none. */
    public static AccessorySettings getSettingsFor(LivingEntity entity, String key) {
        return getAccessorySettingsMapFor(entity).getOrDefault(key, AccessorySettings.DEFAULT);
    }

    /**
     * The colour of {@code entity}'s sword trail, {@code 0xRRGGBB}: the one its player picked for
     * the {@code sword_trail} accessory, if they wear it; else white.
     */
    public static int getTrailColorFor(LivingEntity entity) {
        AccessorySettings settings = getAccessorySettingsMapFor(entity).get(SWORD_TRAIL);
        if (settings == null || !settings.isShown()) {
            return DEFAULT_TRAIL_COLOR;
        }
        AccessoryDetails details = accessories.get(SWORD_TRAIL);
        return settings.colorOr(details != null ? details.defaultColorOrWhite() : DEFAULT_TRAIL_COLOR);
    }

    private static final class DetailsResponse {
        Map<String, AccessoryDetails> details;
    }
}
