package goblinbob.mobends.core.network;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;

public class SharedBooleanProp extends SharedProperty<Boolean>
{

    public SharedBooleanProp(String key, Boolean value, String description)
    {
        super(key, value, description);
    }

    @Override
    public Boolean getValue()
    {
        return super.getValue();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setBoolean(key, value);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        value = tag.getBoolean(key);
    }

    @Override
    public void updateWithConfig(Configuration configuration, String category)
    {
        value = configuration.get(category, key, defaultValue, description).getBoolean();
    }

}
