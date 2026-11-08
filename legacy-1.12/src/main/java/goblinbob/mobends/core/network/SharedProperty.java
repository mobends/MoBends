package goblinbob.mobends.core.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

/**
 * Represents a value that is stored in the server's config, then shared with clients
 * once they join.
 */
public abstract class SharedProperty<T>
{

    protected final String key;
    protected final String description;
    protected final T defaultValue;
    /** Written by the client thread (the server's answer) and read by the render thread. */
    protected volatile T value;

    public SharedProperty(String key, T defaultValue, String description)
    {
        this.key = key;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getKey()
    {
        return key;
    }

    public String getDescription()
    {
        return description;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    public void reset()
    {
        this.value = defaultValue;
    }

    public abstract void writeToNBT(NBTTagCompound tag);

    public abstract void readFromNBT(NBTTagCompound tag);

    public abstract void updateWithConfig(Configuration configuration, String category);

}
