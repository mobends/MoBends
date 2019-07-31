package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.client.gui.customize.store.CustomizeStore;
import net.gobbob.mobends.core.client.gui.customize.viewport.AlterEntryRig;
import net.gobbob.mobends.core.client.gui.elements.GuiPanel;
import net.gobbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class GuiPartProperties extends GuiPanel
{

    public GuiPartProperties()
    {
        super(null, 0, 400, 100, 300, Direction.RIGHT);
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    public void cleanUp()
    {

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
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        AlterEntryRig.Bone selectedPart = CustomizeStore.getSelectedPart();
        if (selectedPart != null)
            fontRenderer.drawString(selectedPart.getName(), 0, 0, 0xffffffff);
    }

}
