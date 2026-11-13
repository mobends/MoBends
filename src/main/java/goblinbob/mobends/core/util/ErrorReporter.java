package goblinbob.mobends.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ErrorReporter
{

    public static ChatComponentText createErrorHeader()
    {
        ChatComponentText header = new ChatComponentText("[Mo' Bends] ");
        header.getChatStyle().setColor(EnumChatFormatting.YELLOW);

        return header;
    }

    /** Shows {@code textComponent} in the chat; false if there is no player to show it to (e.g. on the title screen). */
    public static boolean showErrorToPlayer(ChatComponentText textComponent)
    {
        if (Minecraft.getMinecraft().thePlayer == null)
        {
            return false;
        }

        ChatComponentText base = new ChatComponentText("");
        base.getChatStyle().setColor(EnumChatFormatting.WHITE);
        base.appendSibling(createErrorHeader());
        base.appendSibling(textComponent);

        Minecraft.getMinecraft().thePlayer.addChatMessage(base);
        return true;
    }

    public static boolean showErrorToPlayer(String error)
    {
        return showErrorToPlayer(new ChatComponentText(error));
    }

}
