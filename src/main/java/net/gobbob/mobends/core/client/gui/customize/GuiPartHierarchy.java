package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.client.gui.elements.GuiPanel;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class GuiPartHierarchy extends GuiPanel
{

    private GuiPartList partList;

    public GuiPartHierarchy()
    {
        super(null, 0, 60, 100, 300, Direction.RIGHT);
        this.partList = new GuiPartList(this, 2, 2, 96, 90);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.addElement(this.partList);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int event)
    {
        return this.partList.mouseClicked(mouseX - x, mouseY - y, event);
    }

    public boolean mouseReleased(int mouseX, int mouseY, int event)
    {
        this.partList.mouseReleased(mouseX - x, mouseY - y, event);

        return false;
    }

    public boolean handleMouseInput()
    {
        return this.partList.handleMouseInput();
    }

    @Override
    protected void drawBackground()
    {
        Draw.rectangle(0, 0, this.getWidth(), this.getHeight() - 2, 0xff00528a);
        Draw.rectangle(0, this.getHeight() - 2, this.getWidth(), 2, 0xff00406b);
    }

    @Override
    protected void drawForeground()
    {

    }

    public void addPart(String part)
    {
        partList.addPart(part);
    }

    public void setParts(String[] parts)
    {
        this.partList.setParts(parts);
    }

}
