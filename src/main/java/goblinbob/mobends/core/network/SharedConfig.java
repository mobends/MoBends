package goblinbob.mobends.core.network;

import net.minecraft.nbt.NBTTagCompound;
import java.util.LinkedList;

/**
 * Holds properties that are shared by the server with the client on world join.
 * These are usually permissions and restrictions.
 */
public class SharedConfig
{

    private LinkedList<SharedProperty<?>> properties = new LinkedList<>();

    public SharedConfig()
    {
    }

    public void addProperty(SharedProperty<?> property)
    {
        properties.add(property);
    }

    public Iterable<SharedProperty<?>> getProperties()
    {
        return properties;
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        for (SharedProperty<?> property : properties)
        {
            property.writeToNBT(tag);
        }
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        for (SharedProperty<?> property : properties)
        {
            property.readFromNBT(tag);
        }
    }

}
