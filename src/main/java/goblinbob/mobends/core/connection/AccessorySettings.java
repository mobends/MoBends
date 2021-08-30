package goblinbob.mobends.core.connection;

import goblinbob.mobends.core.util.Color;

public class AccessorySettings
{
    private String displayName;
    private boolean unlocked;
    private boolean hidden;
    private Color color;

    public AccessorySettings(String displayName, boolean unlocked, boolean hidden, Color color)
    {
        this.displayName = displayName;
        this.unlocked = unlocked;
        this.hidden = hidden;
        this.color = color;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isUnlocked()
    {
        return unlocked;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public Color getColor()
    {
        return color;
    }
}
