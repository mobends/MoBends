package goblinbob.mobends.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ErrorReporter
{

    public static TextComponentString createErrorHeader()
    {
        TextComponentString header = new TextComponentString("[Mo' Bends] ");
        header.getStyle().setColor(TextFormatting.YELLOW);

        return header;
    }

    /** Shows {@code textComponent} in the chat; false if there is no player to show it to (e.g. on the title screen). */
    public static boolean showErrorToPlayer(TextComponentString textComponent)
    {
        if (Minecraft.getMinecraft().player == null)
        {
            return false;
        }

        TextComponentString base = new TextComponentString("");
        base.getStyle().setColor(TextFormatting.WHITE);
        base.appendSibling(createErrorHeader());
        base.appendSibling(textComponent);

        Minecraft.getMinecraft().player.sendMessage(base);
        return true;
    }

    public static boolean showErrorToPlayer(String error)
    {
        return showErrorToPlayer(new TextComponentString(error));
    }

}
