package goblinbob.mobends.core.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

public class SharedStringProp extends SharedProperty<String>
{

    public SharedStringProp(String key, String value, String description)
    {
        super(key, value, description);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setString(key, value);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        value = tag.hasKey(key) ? tag.getString(key) : defaultValue;
    }

    @Override
    public void updateWithConfig(Configuration configuration, String category)
    {
        value = configuration.get(category, key, defaultValue, description).getString();
    }

}
