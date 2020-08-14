package goblinbob.mobends.core.util;

import goblinbob.mobends.core.pack.InvalidPackFormatException;
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

    public static void showErrorToPlayer(InvalidPackFormatException ex)
    {
        TextComponentString textComponent = new TextComponentString("A pack has been disabled due to it's wrong format: ");

        TextComponentString packName = new TextComponentString(ex.getPackName());
        packName.getStyle().setBold(true);
        textComponent.appendSibling(packName);

        textComponent.appendText(". Check the logs for more details...");

        showErrorToPlayer(textComponent);
    }

}
