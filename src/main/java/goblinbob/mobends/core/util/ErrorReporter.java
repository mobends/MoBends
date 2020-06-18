package goblinbob.mobends.core.util;

import goblinbob.mobends.core.pack.InvalidPackFormatException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ErrorReporter
{

    public static void showErrorToPlayer(String error)
    {
        TextComponentString textComponent = new TextComponentString(error);
        textComponent.getStyle().setColor(TextFormatting.YELLOW);
        Minecraft.getMinecraft().player.sendMessage(textComponent);
    }

    public static void showErrorToPlayer(InvalidPackFormatException ex)
    {
        if (Minecraft.getMinecraft().player == null)
        {
            return;
        }

        TextComponentString base = new TextComponentString("");

        TextComponentString header = new TextComponentString("[Mo' Bends] ");
        header.getStyle().setColor(TextFormatting.YELLOW);
        base.appendSibling(header);

        TextComponentString textComponent = new TextComponentString("A pack has been disabled due to it's wrong format: ");
        textComponent.getStyle().setColor(TextFormatting.WHITE);
        header.appendSibling(textComponent);

        TextComponentString packName = new TextComponentString(ex.getPackName());
        packName.getStyle().setBold(true);
        base.appendSibling(packName);

        base.appendText(". Check the logs for more details...");

        Minecraft.getMinecraft().player.sendMessage(base);
    }

}
