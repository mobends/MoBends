package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.elements.GuiElement;
import net.gobbob.mobends.core.client.gui.elements.GuiScrollPanel;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiPartList extends GuiScrollPanel
{

    private IPartListListener changeListener = null;
    private List<Entry> parts;
    protected int elementHeight;
    protected int elementMargin;
    protected int hoveredElementIndex;
    protected int selectedEntryIndex;

    public GuiPartList(GuiElement parent, int x, int y, int width, int height, @Nullable IPartListListener listener)
    {
        super(parent, x, y, width, height);
        this.parts = new ArrayList<>();
        this.elementHeight = 11;
        this.elementMargin = 1;
        this.hoveredElementIndex = -1;
        this.selectedEntryIndex = -1;
        this.changeListener = listener;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);

        if (!this.hovered || this.scrollBarHovered)
        {
            this.hoveredElementIndex = -1;
        }
        else
        {
            this.hoveredElementIndex = (mouseY - y + scrollAmount) / (elementHeight + elementMargin);
            if (this.hoveredElementIndex < 0)
                this.hoveredElementIndex = -1;
            if (this.hoveredElementIndex >= this.parts.size())
            {
                this.hoveredElementIndex = this.parts.size() - 1;
            }
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int event)
    {
        boolean eventHandled = super.mouseClicked(mouseX, mouseY, event);

        if (!eventHandled)
        {
            if (this.hoveredElementIndex != -1)
            {
                this.selectEntry(this.hoveredElementIndex);
                eventHandled = true;
            }
        }

        return eventHandled;
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

        if (this.selectedEntryIndex != -1)
            Draw.rectangle(0, this.selectedEntryIndex * (this.elementHeight + this.elementMargin), width - scrollBarWidth, this.elementHeight, 0xffff0000);
        if (this.hoveredElementIndex != -1)
            Draw.rectangle(0, this.hoveredElementIndex * (this.elementHeight + this.elementMargin), width - scrollBarWidth, this.elementHeight, 0xff000000);
        for (int i = 0; i < this.parts.size(); ++i)
        {
            fontRenderer.drawString(this.parts.get(i).name, this.x, this.y + i * (this.elementHeight + this.elementMargin), 0xffffffff);
        }
    }

    public void selectEntry(int index)
    {
        if (index < 0 || index >= this.parts.size())
            return;

        this.selectedEntryIndex = index;
        Entry entry = this.parts.get(index);
        if (this.changeListener != null)
            this.changeListener.onPartSelectedFromList(entry.name, entry.bone);
    }

    public void selectBone(AlterEntryRig.Bone bone)
    {
        for (int i = 0; i < this.parts.size(); ++i)
        {
            if (this.parts.get(i).bone == bone)
            {
                this.selectEntry(i);
                return;
            }
        }
    }

    protected void updateContentSize()
    {
        this.contentSize = this.parts.size() * (this.elementHeight + this.elementMargin);
    }

    public void clearParts()
    {
        this.parts.clear();
        this.hoveredElementIndex = -1;
        this.selectedEntryIndex = -1;
        this.updateContentSize();
    }

    public void addPart(String name, AlterEntryRig.Bone bone)
    {
        this.parts.add(new Entry(name, bone));
        this.updateContentSize();
    }

    public void setParts(String[] names, AlterEntryRig rig)
    {
        this.parts.clear();
        this.hoveredElementIndex = -1;
        this.selectedEntryIndex = -1;

        for (String name : names)
        {
            AlterEntryRig.Bone bone = rig.getBone(name);
            if (bone != null)
            {
                this.parts.add(new Entry(name, bone));
            }
        }

        this.updateContentSize();
    }

    private class Entry
    {

        protected String name;
        protected AlterEntryRig.Bone bone;

        public Entry(String name, AlterEntryRig.Bone bone)
        {
            this.name = name;
            this.bone = bone;
        }

    }

    public interface IPartListListener
    {

        void onPartSelectedFromList(String name, AlterEntryRig.Bone bone);

    }

}
