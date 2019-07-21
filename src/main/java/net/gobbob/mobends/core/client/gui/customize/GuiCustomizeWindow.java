package net.gobbob.mobends.core.client.gui.customize;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.core.client.gui.customize.store.CustomizeStore;
import net.gobbob.mobends.core.client.gui.customize.viewport.ViewportLayer;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
import net.gobbob.mobends.core.pack.PackManager;
import net.gobbob.mobends.core.store.ISubscriber;
import net.gobbob.mobends.core.store.Subscription;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static net.gobbob.mobends.core.client.gui.customize.store.CustomizeMutations.SHOW_ALTER_ENTRY;
import static net.gobbob.mobends.core.client.gui.customize.store.CustomizeMutations.TRACK_EDITOR_ACTION;

public class GuiCustomizeWindow extends GuiScreen implements ISubscriber
{

    /*
     * Used to remember which AlterEntry was used last, so
     * the GUI can show that as default the next time it
     * opens up.
     */
    protected static AlterEntry lastAlterEntryViewed = null;

    final List<AlterEntry<?>> alterEntries = new ArrayList<>();

    private final ViewportLayer viewportLayer;
    private final OverlayLayer overlayLayer;
    private final LinkedList<IGuiLayer> layers = new LinkedList<>();

    public GuiCustomizeWindow()
    {
        for (AnimatedEntity<?> animatedEntity : AnimatedEntityRegistry.getRegistered())
        {
            this.alterEntries.addAll(animatedEntity.getAlterEntries());
        }

        this.viewportLayer = new ViewportLayer(this);
        this.overlayLayer = new OverlayLayer(this);
        this.layers.add(this.viewportLayer);
        this.layers.add(this.overlayLayer);

        this.trackSubscription(CustomizeStore.observeAlterEntry((AlterEntry<?> alterEntry) -> {
            lastAlterEntryViewed = alterEntry;
        }));

        // Showing the AlterEntry viewed the last time
        // this gui was open.
        CustomizeStore.instance.commit(SHOW_ALTER_ENTRY, lastAlterEntryViewed != null ? lastAlterEntryViewed : this.alterEntries.get(0));
    }

    /**
     * Subscriber implementation
     */
    private final List<Subscription<?>> subscriptions = new LinkedList<>();

    @Override
    public List<Subscription<?>> getSubscriptions() { return this.subscriptions; }

    @Override
    public void onGuiClosed()
    {
        for (IGuiLayer layer : this.layers)
        {
            layer.cleanUp();
        }
        this.removeSubscriptions();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.overlayLayer.initGui();
        this.viewportLayer.initGui();

        for (IGuiLayer layer : this.layers)
        {
            layer.handleResize(this.width, this.height);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        for (IGuiLayer layer : this.layers)
        {
            layer.draw();
        }

		/*if (!PackManager.isCurrentPackLocal())
		{
			this.drawCenteredString(fontRenderer, I18n.format("mobends.gui.chooseapacktoedit"),
					this.x + WIDTH / 2, this.y + 135, 0xffffff);
			
			return;
		}*/
    }

    @Override
    public void updateScreen()
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        for (IGuiLayer layer : this.layers)
        {
            layer.update(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button)
    {
        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseClicked(mouseX, mouseY, button))
                break;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button)
    {
        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseReleased(mouseX, mouseY, button))
                break;
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleMouseInput())
                break;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        boolean eventHandled = false;

        Iterator<IGuiLayer> it = this.layers.descendingIterator();
        while (it.hasNext())
        {
            if (it.next().handleKeyTyped(typedChar, keyCode))
            {
                eventHandled |= true;
                break;
            }
        }

        if (!eventHandled && keyCode == Keyboard.KEY_ESCAPE)
        {
            goBack();
            eventHandled = true;
        }
		
		/*if (!PackManager.isCurrentPackLocal())
			return;*/
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void goBack()
    {
        if (PackManager.getCurrentPack() != null)
        {
            try
            {
                PackManager.getCurrentPack().save();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        this.mc.displayGuiScreen(new GuiBendsMenu());
    }

    public void performAction(IEditorAction<GuiCustomizeWindow> action)
    {
        action.perform(this);
        CustomizeStore.instance.commit(TRACK_EDITOR_ACTION, action);
    }

}
