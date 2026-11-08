package goblinbob.mobends.core.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

public class SharedFloatProp extends SharedProperty<Float>
{

    public SharedFloatProp(String key, Float value, String description)
    {
        super(key, value, description);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setFloat(key, value);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        value = tag.hasKey(key) ? tag.getFloat(key) : defaultValue;
    }

    @Override
    public void updateWithConfig(Configuration configuration, String category)
    {
        value = (float) configuration.get(category, key, defaultValue, description).getDouble();
    }

}
