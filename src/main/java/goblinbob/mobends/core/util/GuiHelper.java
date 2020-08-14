package goblinbob.mobends.core.util;

import goblinbob.mobends.core.Core;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;

import java.net.URI;

public class GuiHelper
{

    public static void closeGui()
    {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    public static void playButtonSound(SoundHandler soundHandler)
    {
        soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static boolean openUrlInBrowser(String url)
    {
        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object)null);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(url));
            return true;
        }
        catch (Throwable throwable)
        {
            Core.LOG.warning(String.format("Couldn't open link %s", url));
            return false;
        }
    }

}
