package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.elements.GuiCustomButton;
import net.gobbob.mobends.core.pack.LocalBendsPack;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;

import java.util.Collection;

public class GuiLocalPacks
{

    private static final int BUTTON_OPEN_FOLDER = 2;

    private int x, y;
    private GuiPackList availablePacksList;
    private GuiPackList appliedPacksList;

    private final GuiCustomButton openFolderButton;

    public GuiLocalPacks()
    {
        this.availablePacksList = new GuiPackList();
        this.appliedPacksList = new GuiPackList();

        for (LocalBendsPack pack : PackManager.instance.getLocalPacks())
        {
            this.availablePacksList.addElement(new GuiPackEntry(pack));
        }

        this.openFolderButton = new GuiCustomButton(BUTTON_OPEN_FOLDER, GuiPacksWindow.EDITOR_WIDTH - 2, 20);
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

        this.availablePacksList.mouseClicked(mouseX, mouseY, button);
        this.appliedPacksList.mouseClicked(mouseX, mouseY, button);

        return eventHandled;
    }

    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        this.openFolderButton.mouseReleased(mouseX, mouseY);

        this.availablePacksList.mouseReleased(mouseX, mouseY, button);
        this.appliedPacksList.mouseReleased(mouseX, mouseY, button);
    }

    public void update(int mouseX, int mouseY)
    {
        this.availablePacksList.update(mouseX, mouseY);
        this.appliedPacksList.update(mouseX, mouseY);
    }

    public void draw()
    {
        this.availablePacksList.draw();
        this.appliedPacksList.draw();
    }

}
