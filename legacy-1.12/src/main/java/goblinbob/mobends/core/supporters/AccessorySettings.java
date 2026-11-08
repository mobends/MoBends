package goblinbob.mobends.core.supporters;

import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.IColorRead;

public class AccessorySettings
{
    public static final AccessorySettings DEFAULT = new AccessorySettings(false, false, Color.WHITE);

    private boolean unlocked;
    private boolean hidden;
    private Color color;

    public AccessorySettings(boolean unlocked, boolean hidden, IColorRead color)
    {
        this.unlocked = unlocked;
        this.hidden = hidden;
        this.color = new Color(color);
    }

    public boolean isUnlocked()
    {
        return unlocked;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public IColorRead getColor()
    {
        return color;
    }
}
