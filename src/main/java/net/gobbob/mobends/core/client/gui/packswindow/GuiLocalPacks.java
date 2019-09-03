package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiCustomButton;
import net.gobbob.mobends.core.pack.LocalBendsPack;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;

import java.util.Collection;

public class GuiLocalPacks extends Gui
{

    private int x, y;
    private GuiPackList availablePacksList;
    private GuiPackList appliedPacksList;

    private final GuiCustomButton openFolderButton;

    private final FontRenderer fontRenderer;

    public GuiLocalPacks()
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.availablePacksList = new GuiPackList();
        this.appliedPacksList = new GuiPackList();

        for (LocalBendsPack pack : PackManager.instance.getLocalPacks())
        {
            this.availablePacksList.addElement(new GuiPackEntry(pack));
        }

        this.openFolderButton = new GuiCustomButton(-1, GuiPacksWindow.EDITOR_WIDTH - 2, 20);
        this.openFolderButton.setText(I18n.format("mobends.gui.openpacksfolder"));
    }

    public void initGui(int x, int y, Collection<GuiButton> buttons)
    {
        this.x = x;
        this.y = y;

        this.availablePacksList.initGui(x + 9, y + 23);
        this.appliedPacksList.initGui(x + GuiPacksWindow.EDITOR_WIDTH - GuiPackList.WIDTH - 1, y + 23);

        this.openFolderButton.setPosition(this.x + 5, this.y + GuiPacksWindow.EDITOR_HEIGHT - 17);

        buttons.add(this.openFolderButton);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        boolean eventHandled = false;

        if (this.openFolderButton.mousePressed(mouseX, mouseY))
        {
            OpenGlHelper.openFile(PackManager.instance.getLocalDirectory());
            eventHandled = true;
        }

        this.availablePacksList.handleMouseClicked(mouseX, mouseY, button);
        this.appliedPacksList.handleMouseClicked(mouseX, mouseY, button);

        return eventHandled;
    }

    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.openFolderButton.mouseReleased(mouseX, mouseY);

        this.availablePacksList.handleMouseReleased(mouseX, mouseY, button);
        this.appliedPacksList.handleMouseReleased(mouseX, mouseY, button);
    }

    public boolean handleMouseInput()
    {
        boolean handled = false;
        handled |= this.availablePacksList.handleMouseInput();
        handled |= this.appliedPacksList.handleMouseInput();
        return handled;
    }

    public void update(int mouseX, int mouseY)
    {
        this.availablePacksList.update(mouseX, mouseY);
        this.appliedPacksList.update(mouseX, mouseY);
    }

    public void draw()
    {
        availablePacksList.draw();
        appliedPacksList.draw();

        drawCenteredString(fontRenderer, I18n.format("mobends.gui.unusedpacks"), x + GuiPacksWindow.EDITOR_WIDTH / 4, y + 8, 0xffffff);
        drawCenteredString(fontRenderer, I18n.format("mobends.gui.appliedpacks"), x + GuiPacksWindow.EDITOR_WIDTH * 3 / 4 + 6, y + 8, 0xffffff);
    }

}
