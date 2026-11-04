package goblinbob.mobends.core.supporters;

import com.google.gson.JsonParseException;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.connection.PlayerSettingsResponse;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.http.conn.HttpHostConnectException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static goblinbob.mobends.core.util.ConnectionHelper.sendGetRequest;

public class SupporterContent
{
    private static SupporterContent INSTANCE;
    private static final Color DEFAULT_TRAIL_COLOR = new Color(Color.WHITE);

    private final String apiUrl;
    /** Replaced on the client thread, so readers can keep iterating the previous catalogue. */
    private Map<String, AccessoryDetails> accessoryDetailsMap = Collections.emptyMap();
    private int accessoryDetailsRevision;
    private final AtomicBoolean fetchingAccessoryDetails = new AtomicBoolean();
    private final Map<String, PlayerSettingsResponse> accessorySettingsPerPlayer = new HashMap<>();
    /** What the previews of the Supporters window wear, whatever their player has unlocked. */
    private final Map<EntityLivingBase, Map<String, AccessorySettings>> previewAccessories = new WeakHashMap<>();

    private SupporterContent()
    {
        this.apiUrl = EnvironmentModule.getConfig().getApiUrl();
    }

    /** Downloads the catalogue off-thread, keeping the last good one if the request fails. */
    public static void fetchAccessoryDetails()
    {
        final SupporterContent content = INSTANCE;
        if (!content.fetchingAccessoryDetails.compareAndSet(false, true))
        {
            return;
        }

        Thread thread = new Thread(() ->
        {
            Map<String, AccessoryDetails> details = null;
            try
            {
                details = content.downloadAccessoryDetails();
            }
            finally
            {
                final Map<String, AccessoryDetails> downloaded = details;
                Minecraft.getMinecraft().addScheduledTask(() ->
                {
                    if (downloaded != null)
                    {
                        content.accessoryDetailsMap = downloaded;
                        ++content.accessoryDetailsRevision;
                    }
                    content.fetchingAccessoryDetails.set(false);
                });
            }
        }, "Mo' Bends accessory download");
        thread.setDaemon(true);
        thread.start();
    }

    private Map<String, AccessoryDetails> downloadAccessoryDetails()
    {
        try
        {
            AccessoryDetailsResponse response = sendGetRequest(new URL(apiUrl + "/api/accessory/details"), new HashMap<>(), AccessoryDetailsResponse.class);

            Map<String, AccessoryDetails> details = Objects.requireNonNull(response).getDetails();
            return Collections.unmodifiableMap(new HashMap<>(details));
        }
        catch (HttpHostConnectException e)
        {
            // No internet, do nothing.
        }
        catch (JsonParseException e)
        {
            Core.LOG.warning("Failed to parse accessory details map.");
            e.printStackTrace();
        }
        catch (NullPointerException | IOException | URISyntaxException e)
        {
            Core.LOG.warning("Failed to get accessory details map.");
            e.printStackTrace();
        }
        return null;
    }

    /** Read on the client thread, like the catalogue itself. */
    public static int getAccessoryDetailsRevision()
    {
        return INSTANCE.accessoryDetailsRevision;
    }

    private void clearCache()
    {
        synchronized (accessorySettingsPerPlayer)
        {
            this.accessorySettingsPerPlayer.clear();
        }
    }

    public static void registerPlayerAccessorySettings(String playerName, PlayerSettingsResponse settings)
    {
        synchronized (INSTANCE.accessorySettingsPerPlayer)
        {
            INSTANCE.accessorySettingsPerPlayer.put(playerName, settings);
        }
    }

    /** Drops the player's settings, so they are fetched anew (after becoming a supporter, say). */
    public static void forgetPlayer(String playerName)
    {
        synchronized (INSTANCE.accessorySettingsPerPlayer)
        {
            INSTANCE.accessorySettingsPerPlayer.remove(playerName);
        }
    }

    /** Dresses a preview entity in {@code settings} instead of what its player has; null undresses it. */
    public static void setPreviewAccessories(EntityLivingBase entity, @Nullable Map<String, AccessorySettings> settings)
    {
        if (settings == null)
            INSTANCE.previewAccessories.remove(entity);
        else
            INSTANCE.previewAccessories.put(entity, settings);
    }

    public static Set<Map.Entry<String, AccessoryDetails>> getAccessories()
    {
        return INSTANCE.accessoryDetailsMap.entrySet();
    }

    public static Map<String, AccessorySettings> getAccessorySettingsMapFor(EntityLivingBase entity)
    {
        final Map<String, AccessorySettings> previewSettings = INSTANCE.previewAccessories.get(entity);
        if (previewSettings != null)
        {
            return previewSettings;
        }

        final String name = entity.getName();

        PlayerSettingsResponse settings;

        synchronized (INSTANCE.accessorySettingsPerPlayer)
        {
            settings = INSTANCE.accessorySettingsPerPlayer.get(name);
        }

        if (settings == null)
        {
            // The profile's UUID finds an account linked on the dashboard even after it's been renamed.
            UUID uuid = entity instanceof EntityPlayer ? ((EntityPlayer) entity).getGameProfile().getId() : null;
            ConnectionManager.INSTANCE.fetchSettingsForPlayer(name, uuid);
            return Collections.emptyMap();
        }

        // The downloader only registers responses with settings; this keeps the renderer safe whatever is cached.
        Map<String, AccessorySettings> settingsMap = settings.getSettings();
        return settingsMap != null ? settingsMap : Collections.emptyMap();
    }

    public static IColorRead getTrailColorFor(EntityLivingBase entity)
    {
        Map<String, AccessorySettings> settingsMap = getAccessorySettingsMapFor(entity);
        AccessorySettings swordTrailSettings = settingsMap.get("sword_trail");

        if (swordTrailSettings == null)
        {
            return DEFAULT_TRAIL_COLOR;
        }

        return swordTrailSettings.getColor();
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            // Doing it here so that we know the module's been registered.
            // Otherwise, this module wouldn't get refreshed and we wouldn't know.
            INSTANCE = new SupporterContent();
            fetchAccessoryDetails();
        }

        @Override
        public void onRefresh()
        {
            fetchAccessoryDetails();
            INSTANCE.clearCache();
        }
    }
}
