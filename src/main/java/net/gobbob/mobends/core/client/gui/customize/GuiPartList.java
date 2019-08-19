package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.client.gui.customize.store.CustomizeStore;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.elements.GuiElement;
import net.gobbob.mobends.core.client.gui.elements.GuiScrollPanel;
import net.gobbob.mobends.core.store.ISubscriber;
import net.gobbob.mobends.core.store.Subscription;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.gobbob.mobends.core.client.gui.customize.store.CustomizeMutations.SELECT_PART;

public class GuiPartList extends GuiScrollPanel implements ISubscriber
{

    private List<Entry> parts;
    private int elementHeight;
    private int elementMargin;
    private int hoveredElementIndex;
    private int selectedEntryIndex;

    public GuiPartList(GuiElement parent, int x, int y, int width, int height)
    {
        super(parent, x, y, width, height);
        this.parts = new ArrayList<>();
        this.elementHeight = 11;
        this.elementMargin = 1;
        this.hoveredElementIndex = -1;
        this.selectedEntryIndex = -1;

        this.trackSubscription(CustomizeStore.observeAlterEntryRig((AlterEntryRig rig) ->
        {
            this.parts.clear();
            this.hoveredElementIndex = -1;
            this.selectedEntryIndex = -1;

            if (rig != null)
            {
                for (String name : rig.getAnimatedEntity().getAlterableParts())
                {
                    AlterEntryRig.Bone bone = rig.getBone(name);
                    if (bone != null)
                    {
                        this.parts.add(new Entry(name, bone));
                    }
                }
            }

            this.updateContentSize();
        }));

        this.trackSubscription(CustomizeStore.observeSelectedPart((AlterEntryRig.Bone bone) ->
        {
            for (int i = 0; i < this.parts.size(); ++i)
            {
                if (this.parts.get(i).bone == bone)
                {
                    this.selectedEntryIndex = i;
                    return;
                }
            }
        }));
    }

    /**
     * Subscriber implementation
     */
    private final List<Subscription<?>> subscriptions = new LinkedList<>();

    @Override
    public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

    public void cleanUp()
    {
        this.removeSubscriptions();
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
                AlterEntryRig.Bone bone = this.parts.get(this.hoveredElementIndex).bone;
                CustomizeStore.instance.commit(SELECT_PART, bone);
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

    protected void updateContentSize()
    {
        this.contentSize = this.parts.size() * (this.elementHeight + this.elementMargin);
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

}
