package goblinbob.mobends.forge;

import net.minecraft.client.Minecraft;

public class GuiHelper
{

    public static void closeGui()
    {
        Minecraft.getInstance().displayGuiScreen(null);
    }

}