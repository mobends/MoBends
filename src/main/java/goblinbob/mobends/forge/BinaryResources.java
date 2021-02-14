package goblinbob.mobends.forge;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.StreamSerialInput;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;

public class BinaryResources
{
    public static ISerialInput getStream(ResourceLocation location) throws IOException
    {
        final InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();

        return new StreamSerialInput(stream);
    }
}
