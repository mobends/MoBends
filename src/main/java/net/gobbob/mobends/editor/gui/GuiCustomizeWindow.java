package net.gobbob.mobends.editor.gui;

import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.client.gui.GuiBendsMenu;
import net.gobbob.mobends.editor.IEditorAction;
import net.gobbob.mobends.editor.OverlayLayer;
import net.gobbob.mobends.editor.store.CustomizeStore;
import net.gobbob.mobends.editor.viewport.ViewportLayer;
import net.gobbob.mobends.core.client.gui.elements.IGuiLayer;
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

import static net.gobbob.mobends.editor.store.CustomizeMutations.SHOW_ANIMATED_ENTITY;
import static net.gobbob.mobends.editor.store.CustomizeMutations.TRACK_EDITOR_ACTION;

public class GuiCustomizeWindow extends GuiScreen implements ISubscriber
{

    /*
     * Used to remember which AnimatedEntity was used last, so
     * the GUI can show that as default the next time it
     * opens up.
     */
    protected static AnimatedEntity lastAnimatedEntityViewed = null;

    public final List<AnimatedEntity<?>> animatedEntities = new ArrayList<>();

    private final ViewportLayer viewportLayer;
    private final OverlayLayer overlayLayer;
    private final LinkedList<IGuiLayer> layers = new LinkedList<>();

    public GuiCustomizeWindow()
    {
        this.animatedEntities.addAll(AnimatedEntityRegistry.instance.getRegistered());

        this.viewportLayer = new ViewportLayer(this);
        this.overlayLayer = new OverlayLayer(this);
        this.layers.add(this.viewportLayer);
        this.layers.add(this.overlayLayer);

        this.trackSubscription(CustomizeStore.observeAnimatedEntity((AnimatedEntity<?> animatedEntity) -> {
            lastAnimatedEntityViewed = animatedEntity;
        }));

        // Showing the AnimatedEntity viewed the last time
        // this gui was open.
        CustomizeStore.instance.commit(SHOW_ANIMATED_ENTITY, lastAnimatedEntityViewed != null ? lastAnimatedEntityViewed : this.animatedEntities.get(0));
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
            eventHandled |= true;
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void goBack()
    {
        this.mc.displayGuiScreen(new GuiBendsMenu());
    }

    public void performAction(IEditorAction<GuiCustomizeWindow> action)
    {
        action.perform(this);
        CustomizeStore.instance.commit(TRACK_EDITOR_ACTION, action);
    }

}
