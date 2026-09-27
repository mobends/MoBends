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

    public static void showErrorToPlayer(TextComponentString textComponent)
    {
        if (Minecraft.getMinecraft().player == null)
        {
            return;
        }

        TextComponentString base = new TextComponentString("");
        base.getStyle().setColor(TextFormatting.WHITE);
        base.appendSibling(createErrorHeader());
        base.appendSibling(textComponent);

        Minecraft.getMinecraft().player.sendMessage(base);
    }

    public static void showErrorToPlayer(String error)
    {
        showErrorToPlayer(new TextComponentString(error));
    }

}
