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
        header.func_230530_a_(header.getStyle().func_240712_a_(TextFormatting.YELLOW));

        return header;
    }

    public void beginMessage()
    {
        this.mainComponent = new StringTextComponent("");
        this.mainComponent.func_230530_a_(this.mainComponent.getStyle().func_240712_a_(TextFormatting.WHITE));
        this.mainComponent.func_230529_a_(createErrorHeader());
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
            textComponent.func_230530_a_(textComponent.getStyle().func_240713_a_(true));

        if ((flags & TextStyle.ITALICS) != 0)
            textComponent.func_230530_a_(textComponent.getStyle().func_240722_b_(true));

        if ((flags & TextStyle.UNDERLINED) != 0)
            textComponent.func_230530_a_(textComponent.getStyle().func_244282_c(true));

        this.mainComponent.func_230529_a_(textComponent);
    }

    public void finishMessage()
    {
        Minecraft.getInstance().player.sendMessage(this.mainComponent, Util.field_240973_b_);
    }

}
