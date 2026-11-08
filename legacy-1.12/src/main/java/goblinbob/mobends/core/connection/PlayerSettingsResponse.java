package goblinbob.mobends.core.connection;

import goblinbob.mobends.core.supporters.AccessorySettings;

import java.util.Map;

public class PlayerSettingsResponse
{
    private Map<String, AccessorySettings> settings;

    public Map<String, AccessorySettings> getSettings()
    {
        return settings;
    }
}
