package net.gobbob.mobends.core.client.gui.packswindow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class GuiTabNavigation
{

    private int x;
    private int y;
    private List<GuiPackTab> tabs;
    private GuiPackTab selectedTab;

    public GuiTabNavigation()
    {
        this.tabs = new ArrayList<>();
        this.selectedTab = null;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        for (int i = 0; i < this.tabs.size(); ++i)
        {
            this.tabs.get(i).initGui(x + i * (GuiPackTab.WIDTH - 2), y);
        }
    }

    public void addTab(String tabName, int textureIndex)
    {
        this.tabs.add(new GuiPackTab(tabName, textureIndex));
    }

    public void draw(int mouseX, int mouseY)
    {
        Minecraft mc = Minecraft.getMinecraft();

        for (GuiPackTab tab : tabs)
        {
            tab.draw(mouseX, mouseY, tab == this.selectedTab);
        }

        GuiPackTab selectedTab = this.getSelectedTab();
        if (selectedTab != null)
        {
            // Drawing the selected tab again, so it's on top.
            selectedTab.draw(mouseX, mouseY, true);

            mc.fontRenderer.drawStringWithShadow(I18n.format(selectedTab.titleKey), x + (GuiPackTab.WIDTH - 2) * this.tabs.size() + 10, y - 13, 0xffffff);
        }

    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        for (GuiPackTab tab : tabs)
        {
            if (tab.mouseClicked(mouseX, mouseY, button))
            {
                selectTab(tab);
                return true;
            }
        }
        return false;
    }

    public void selectTab(int index)
    {
        this.selectTab(this.tabs.get(index));
    }

    public void selectTab(GuiPackTab tab)
    {
        this.selectedTab = tab;
    }

    public GuiPackTab getSelectedTab()
    {
        return selectedTab;
    }

}
