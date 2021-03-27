package goblinbob.mobends.forge;

import goblinbob.mobends.core.error.IReportOutput;
import goblinbob.mobends.core.error.TextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ReportOutput implements IReportOutput
{
    private StringTextComponent mainComponent = null;

    public ITextComponent createErrorHeader()
    {
        StringTextComponent header = new StringTextComponent("[Mo' Bends] ");
        header.withStyle(header.getStyle().withColor(TextFormatting.YELLOW));

        return header;
    }

    public void beginMessage()
    {
        this.mainComponent = new StringTextComponent("");
        this.mainComponent.setStyle(this.mainComponent.getStyle().withColor(TextFormatting.WHITE));
        this.mainComponent.append(createErrorHeader());
    }

    @Override
    public void print(String text)
    {
        this.print(text, 0);
    }

    @Override
    public void print(String text, int flags)
    {
        StringTextComponent textComponent = new StringTextComponent(text);

        if ((flags & TextStyle.BOLD) != 0)
            textComponent.setStyle(textComponent.getStyle().withBold(true));

        if ((flags & TextStyle.ITALICS) != 0)
            textComponent.setStyle(textComponent.getStyle().withItalic(true));

        if ((flags & TextStyle.UNDERLINED) != 0)
            textComponent.setStyle(textComponent.getStyle().withUnderlined(true));

        this.mainComponent.append(textComponent);
    }

    public void finishMessage()
    {
        Minecraft.getInstance().player.sendMessage(this.mainComponent, Util.NIL_UUID);
    }
}
