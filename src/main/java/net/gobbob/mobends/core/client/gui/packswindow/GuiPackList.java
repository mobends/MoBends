package net.gobbob.mobends.core.client.gui.packswindow;

import net.gobbob.mobends.core.client.gui.GuiHelper;
import net.gobbob.mobends.core.pack.BendsPack;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.util.Draw;
import net.gobbob.mobends.core.util.GUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiPackList
{

    public static final int OFFSET_X = 5;
    public static final int OFFSET_Y = 5;
    public static final int WIDTH = 102;
    public static final int HEIGHT = 153;
    public static final int TAB_PUBLIC = 0;
    public static final int TAB_LOCAL = 1;
    protected int x, y;
    protected int width, height;
    protected int tabId;
    protected int scrollAmount;
    protected int maxScrollAmount;
    protected boolean hovered = false;

    protected GuiPacksWindow packEditor;
    protected List<GuiPackTab> tabs;
    protected GuiPackEntry selectedEntry;
    protected GuiAddPackButton addPackButton;

    public GuiPackList(GuiPacksWindow packEditor)
    {
        this.packEditor = packEditor;
        this.x = 0;
        this.y = 0;
        this.scrollAmount = this.maxScrollAmount = 0;
        this.width = 104;
        this.height = 155;
        this.tabs = new ArrayList<GuiPackTab>();
        this.tabs.add(new GuiPackTab(TAB_PUBLIC, "mobends.gui.publicpacks"));
        this.tabs.add(new GuiPackTab(TAB_LOCAL, "mobends.gui.localpacks"));
        this.tabId = TAB_PUBLIC;
        this.selectedEntry = null;
        this.addPackButton = new GuiAddPackButton(null, x + 5, y + 5);
        this.addPackButton.disable();
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        updatePositions();
    }

    public void updatePositions()
    {
        for (GuiPackTab tab : tabs)
        {
            tab.initGui(x, y + OFFSET_Y + 1);
        }
        this.addPackButton.setPosition(x + 5, y + 5 + getSelectedTab().getEntries().size() * (GuiPackEntry.HEIGHT + GuiPackEntry.MARGIN));
        maxScrollAmount = getSelectedTab().getEntries().size() * (GuiPackEntry.HEIGHT + GuiPackEntry.MARGIN) + 20 - HEIGHT;
        scroll(0);
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x + 4 && mouseX <= x + WIDTH - 8 &&
                mouseY >= y + 4 && mouseY <= y + HEIGHT + 2;
    }

    public void display(int mouseX, int mouseY)
    {
        this.update(mouseX, mouseY);

        for (GuiPackTab tab : tabs)
        {
            tab.display(mouseX, mouseY, tab.index == tabId);
        }
        getSelectedTab().display(mouseX, mouseY, true);
        Draw.borderBox(x + 4, y + 4, WIDTH, HEIGHT, 4, 36, 117);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(I18n.format(getSelectedTab().getTitle(), new Object[0]), x + 40, y - 11, 0xffffff);

        int[] position = GuiHelper.getDeScaledCoords(x + 4, y + 4 + HEIGHT);
        int[] size = GuiHelper.getDeScaledVector(WIDTH + 4, HEIGHT);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollAmount, 0);
        int offsetY = y;
        for (GuiPackEntry entry : getSelectedTab().getEntries())
        {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(position[0], position[1], size[0], size[1]);
            offsetY += entry.display(mouseX, mouseY + scrollAmount);
        }
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(position[0], position[1], size[0], size[1]);
        addPackButton.draw();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    public void mouseClicked(int mouseX, int mouseY, int button)
    {
        for (GuiPackTab tab : tabs)
        {
            if (tab.mouseClicked(mouseX, mouseY, button))
            {
                selectTab(tab.index, true);
            }
        }

        if (this.hovered && button == 0)
        {
            for (GuiPackEntry entry : getSelectedTab().getEntries())
            {
                if (entry.mouseClicked(mouseX, mouseY + scrollAmount, button))
                {
                    select(entry, true);
                }
            }

            if (addPackButton.mouseClicked(mouseX, mouseY + scrollAmount, button))
            {
                getSelectedTab().addEntry(new GuiPackEntry(this));
                updatePositions();
            }
        }
    }

    public void select(GuiPackEntry selected, boolean update)
    {
        for (GuiPackTab tab : tabs)
        {
            for (GuiPackEntry entry : tab.getEntries())
            {
                entry.deselect();
            }
        }

        selectedEntry = selected;
        packEditor.onEntrySelected(selectedEntry, update);

        if (selectedEntry != null)
        {
            selectedEntry.markSelected();
        }
    }

    public void selectTab(int index, boolean update)
    {
        tabId = index;
        if (update) updatePositions();
        select(null, update);
        switch (tabId)
        {
            case TAB_PUBLIC:
                addPackButton.disable();
                break;
            case TAB_LOCAL:
                addPackButton.enable();
                break;
        }
    }

    public GuiPackTab getTab(int id)
    {
        return tabs.get(id);
    }

    public GuiPackTab getSelectedTab()
    {
        int tabAmount = Math.max(0, tabs.size() - 1);
        tabId = GUtil.clamp(tabId, 0, tabAmount);
        return tabs.get(tabId);
    }

    public void populate()
    {
        for (BendsPack pack : PackManager.getPublicPacks())
        {
            getTab(0).addEntry(new GuiPackEntry(this, pack));
        }

        for (BendsPack pack : PackManager.getLocalPacks())
        {
            getTab(1).addEntry(new GuiPackEntry(this, pack));
        }
    }

    public GuiPackEntry getSelectedEntry()
    {
        return selectedEntry;
    }

    public void applySelectedEntry()
    {
        for (GuiPackTab tab : tabs)
            for (GuiPackEntry entry : tab.entries)
                entry.setApplied(false);

        this.selectedEntry.setApplied(true);
    }

    public void handleMouseInput()
    {
        if (hovered)
        {
            int i2 = Mouse.getEventDWheel();

            if (i2 != 0)
            {
                if (i2 > 0)
                {
                    i2 = -1;
                }
                else if (i2 < 0)
                {
                    i2 = 1;
                }

                scroll((float) (i2 * 15));
            }
        }

        if (Mouse.isButtonDown(1) || Mouse.isButtonDown(2))
        {
            scroll(Mouse.getDY());
        }
    }

    public void scroll(double amount)
    {
        int maxValue = Math.max(0, maxScrollAmount);
        this.scrollAmount = GUtil.clamp(this.scrollAmount + (int) amount, 0, maxValue);
    }

    public void apply()
    {
        PackManager.choose(null);

        for (GuiPackEntry entry : tabs.get(TAB_LOCAL).getEntries())
        {
            if (PackManager.getLocal(entry.getOriginalName()) == null)
            {
                PackManager.addLocal(new BendsPack(entry.getOriginalName(), entry.getDisplayName(), entry.author, entry.description));
            }
            BendsPack pack = PackManager.getLocal(entry.getOriginalName());
            pack.setDisplayName(entry.getDisplayName());
            pack.setAuthor(entry.author);
            pack.setDescription(entry.description);
            if (!entry.getOriginalName().equals(entry.name))
            {
                PackManager.renamePack(entry.getOriginalName(), entry.name);
            }
            if (entry.isApplied())
                PackManager.choose(pack);
            pack.saveBasicInfo();
        }

        for (GuiPackEntry entry : tabs.get(TAB_PUBLIC).getEntries())
        {
            if (entry.isApplied())
            {
                PackManager.choose(PackManager.getPublic(entry.name));
                break;
            }
        }
    }

}
