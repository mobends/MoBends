package goblinbob.mobends.core.client.gui.packswindow;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.client.gui.GuiDragger;
import goblinbob.mobends.core.flux.ISubscriber;
import goblinbob.mobends.core.flux.Subscription;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.pack.LocalBendsPack;
import goblinbob.mobends.core.pack.PackManager;
import goblinbob.mobends.core.util.IDisposable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class GuiLocalPacks extends Gui implements ISubscriber, IDisposable
{

    private final GuiPackList availablePacksList;
    private final GuiPackList appliedPacksList;
    private final LinkedList<GuiPackEntry> availablePacks;
    private final LinkedList<GuiPackEntry> appliedPacks;

    private final GuiDragger<GuiPackEntry> dragger;
    private final FontRenderer fontRenderer;
    private int x, y;

    private List<Subscription<?>> subscriptions = new LinkedList<>();

    public GuiLocalPacks()
    {
        this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.availablePacks = new LinkedList<>();
        this.appliedPacks = new LinkedList<>();
        this.availablePacksList = new GuiPackList(this.availablePacks);
        this.appliedPacksList = new GuiPackList(this.appliedPacks);
        this.dragger = new GuiDragger<>();

        final Collection<IBendsPack> appliedPacks = PackManager.INSTANCE.getAppliedPacks();

        for (LocalBendsPack pack : PackManager.INSTANCE.getLocalPacks())
        {
            if (appliedPacks.contains(pack))
            {
                continue;
            }

            final GuiPackEntry entry = new GuiPackEntry(pack);
            entry.setParentList(availablePacksList);
            availablePacksList.addElement(entry);
        }

        for (IBendsPack pack : appliedPacks)
        {
            final GuiPackEntry entry = new GuiPackEntry(pack);
            entry.setParentList(appliedPacksList);
            appliedPacksList.addElement(entry);
        }

        trackSubscription(availablePacksList.elementClickedObservable.subscribe(element -> {
            appliedPacksList.getListElements().forEach(e -> e.setSelected(false));
            dragger.setDraggedElement(element);
        }));

        trackSubscription(appliedPacksList.elementClickedObservable.subscribe(element -> {
            availablePacksList.getListElements().forEach(e -> e.setSelected(false));
            dragger.setDraggedElement(element);
        }));
    }

    @Override
    public void dispose()
    {
        removeSubscriptions();
    }

    @Override
    public List<Subscription<?>> getSubscriptions()
    {
        return subscriptions;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        availablePacksList.initGui(x + 9, y + 23);
        appliedPacksList.initGui(x + GuiPacksWindow.EDITOR_WIDTH - GuiPackList.WIDTH - 1, y + 23);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        boolean eventHandled = false;

        eventHandled |= availablePacksList.handleMouseClicked(mouseX, mouseY, button);
        eventHandled |= appliedPacksList.handleMouseClicked(mouseX, mouseY, button);

        return eventHandled;
    }

    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        availablePacksList.handleMouseReleased(mouseX, mouseY, button);
        appliedPacksList.handleMouseReleased(mouseX, mouseY, button);
        dragger.stopDragging();

        resolveAppliedPacks();
    }

    public boolean handleMouseInput()
    {
        boolean handled = availablePacksList.handleMouseInput();
        handled |= appliedPacksList.handleMouseInput();
        return handled;
    }

    public void update(int mouseX, int mouseY)
    {
        availablePacksList.update(mouseX, mouseY);
        appliedPacksList.update(mouseX, mouseY);
        dragger.update(mouseX, mouseY);

        final GuiPackEntry element = dragger.getDraggedElement();
        if (element != null)
        {
            final GuiPackList list = mouseX < x + GuiPacksWindow.EDITOR_WIDTH / 2 ? availablePacksList : appliedPacksList;
            final int y = element.getDragY() - element.getDragPivotY() - list.getY() + list.getScrollAmount() + element.getHeight() / 2;
            final int order = y / (element.getHeight() + list.getSpacing());
            if (list != element.getParentList())
            {
                element.getParentList().removeElement(element);
                list.insertOrMoveElement(element, order);
                element.setParentList(list);
            }
            else if (order != element.getOrder())
            {
                list.insertOrMoveElement(element, order);
            }
        }
    }

    public void draw(float partialTicks)
    {
        availablePacksList.draw(partialTicks);
        appliedPacksList.draw(partialTicks);

        drawCenteredString(fontRenderer, I18n.format("mobends.gui.unusedpacks"), x + GuiPacksWindow.EDITOR_WIDTH / 4, y + 8, 0xffffff);
        drawCenteredString(fontRenderer, I18n.format("mobends.gui.appliedpacks"), x + GuiPacksWindow.EDITOR_WIDTH * 3 / 4 + 6, y + 8, 0xffffff);

        final GuiPackEntry element = dragger.getDraggedElement();
        if (element != null)
        {
            element.draw(partialTicks);
        }
    }

    private void resolveAppliedPacks()
    {
        List<String> packNames = new ArrayList<>();
        for (GuiPackEntry entry : appliedPacks)
        {
            packNames.add(entry.name);
        }

        CoreClient.getInstance().getConfiguration().setAppliedPacks(packNames);
    }

}
