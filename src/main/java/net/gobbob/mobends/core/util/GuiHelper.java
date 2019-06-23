package net.gobbob.mobends.core.util;

import net.minecraft.client.Minecraft;

public class GuiHelper
{

    public static void closeGui()
    {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

}
