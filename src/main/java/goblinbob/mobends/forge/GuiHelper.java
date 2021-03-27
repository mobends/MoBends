package goblinbob.mobends.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class GuiHelper
{
    public static void closeGui()
    {
        Minecraft.getInstance().setScreen(null);
    }

    public static String[] wrapText(FontRenderer fontRenderer, String text, int maxWidth)
    {
        if (maxWidth <= 0) return new String[] {};
        if (!text.contains(" "))
            return new String[] { text };

        List<String> lines = new ArrayList<>();
        String leftover = text + "";
        String line = "";

        boolean endOfString = false;
        do
        {
            String leftoverToNextSpace;

            if (leftover.contains(" "))
            {
                leftoverToNextSpace = leftover.substring(0, leftover.indexOf(" "));
            }
            else
            {
                leftoverToNextSpace = leftover;
                endOfString = true;
            }

            int currentWidth = fontRenderer.width(line + leftoverToNextSpace);
            if (currentWidth > maxWidth)
            {
                lines.add(line.trim());
                line = leftoverToNextSpace + " ";
            }
            else
            {
                line += leftoverToNextSpace + " ";
            }

            if (!endOfString)
                leftover = leftover.substring(leftover.indexOf(" ") + 1);
            else
                lines.add(line.trim());
        } while (!endOfString);

        return lines.toArray(new String[] {});
    }
}