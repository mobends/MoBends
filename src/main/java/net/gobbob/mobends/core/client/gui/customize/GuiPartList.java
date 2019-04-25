package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.client.gui.elements.GuiElement;
import net.gobbob.mobends.core.client.gui.elements.GuiScrollPanel;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.List;

public class GuiPartList extends GuiScrollPanel
{

    private List<String> partNames;
    protected int elementHeight;
    protected int elementMargin;
    protected int hoveredElementId;

    public GuiPartList(GuiElement parent, int x, int y, int width, int height)
    {
        super(parent, x, y, width, height);
        this.partNames = new ArrayList<>();
        this.elementHeight = 11;
        this.elementMargin = 1;
        this.hoveredElementId = -1;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);

        if (!this.hovered || this.scrollBarHovered)
        {
            this.hoveredElementId = -1;
        }
        else
        {
            this.hoveredElementId = (mouseY - y + scrollAmount) / (elementHeight + elementMargin);
            if (this.hoveredElementId < 0)
                this.hoveredElementId = -1;
            if (this.hoveredElementId >= this.partNames.size())
            {
                this.hoveredElementId = this.partNames.size() - 1;
            }
        }
    }

    @Override
    protected void drawBackground()
    {
        //Draw.rectangle(0, 0, this.getWidth(), this.getHeight(), this.hovered ? 0xff000000 : 0xff00528a);
    }

    @Override
    protected void drawContent()
    {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        if (this.hoveredElementId != -1)
            Draw.rectangle(0, this.hoveredElementId * (this.elementHeight + this.elementMargin), width - scrollBarWidth, this.elementHeight, 0xff000000);
        for (int i = 0; i < this.partNames.size(); ++i)
        {
            fontRenderer.drawString(this.partNames.get(i), this.x, this.y + i * (this.elementHeight + this.elementMargin), 0xffffffff);
        }
    }

    protected void updateContentSize()
    {
        this.contentSize = this.partNames.size() * (this.elementHeight + this.elementMargin);
    }

    public void addPart(String part)
    {
        this.partNames.add(part);

        this.updateContentSize();
    }

    public void setParts(String[] parts)
    {
        this.partNames.clear();

        for (String part : parts)
        {
            this.partNames.add(part);
        }

        this.updateContentSize();
    }

}
