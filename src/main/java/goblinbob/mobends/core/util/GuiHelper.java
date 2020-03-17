package goblinbob.mobends.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;

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

}
