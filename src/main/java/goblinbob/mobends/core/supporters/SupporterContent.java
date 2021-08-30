package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.connection.AccessorySettings;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.connection.PlayerSettingsResponse;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.entity.EntityLivingBase;

import java.util.HashMap;
import java.util.Map;

public class SupporterContent
{
    public static SupporterContent INSTANCE = new SupporterContent();

    private Color DEFAULT_TRAIL_COLOR = new Color(Color.WHITE);

    private final Map<String, PlayerSettingsResponse> accessorySettingsPerPlayer = new HashMap<>();

    private SupporterContent()
    {

    }

    public void clearCache()
    {
        synchronized (accessorySettingsPerPlayer)
        {
            this.accessorySettingsPerPlayer.clear();
        }
    }

    public void registerPlayerAccessorySettings(String playerName, PlayerSettingsResponse settings)
    {
        synchronized (accessorySettingsPerPlayer)
        {
            accessorySettingsPerPlayer.put(playerName, settings);
        }
    }

    public IColorRead getTrailColorFor(EntityLivingBase entity)
    {
        final String name = entity.getName();

        PlayerSettingsResponse settings;

        synchronized (accessorySettingsPerPlayer)
        {
            settings = accessorySettingsPerPlayer.get(name);
        }

        if (settings == null)
        {
            ConnectionManager.INSTANCE.fetchSettingsForPlayer(name);
            return DEFAULT_TRAIL_COLOR;
        }

        AccessorySettings accessorySettings = settings.getSettings().get("sword_trail");

        return accessorySettings != null ? accessorySettings.getColor() : DEFAULT_TRAIL_COLOR;
    }
}
