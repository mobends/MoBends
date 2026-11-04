package goblinbob.mobends.core.connection;

import java.util.UUID;

public class PlayerSettingsTask
{
    public final String playerName;
    /** The player's profile UUID, or null if it isn't known. */
    public final UUID uuid;

    public PlayerSettingsTask(String playerName, UUID uuid)
    {
        this.playerName = playerName;
        this.uuid = uuid;
    }
}
